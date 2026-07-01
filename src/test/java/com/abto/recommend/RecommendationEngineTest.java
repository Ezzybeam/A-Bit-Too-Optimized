package com.abto.recommend;

import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.GpuTier;
import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineTest {
    private static EffectiveHardware hw(long allocMb, int cores, GpuTier tier) {
        return new EffectiveHardware(allocMb, cores, OptionalLong.of(allocMb * 2), Optional.of("x"), tier);
    }

    @Test
    void strongMachineGetsUltra() {
        assertEquals(Preset.ULTRA, RecommendationEngine.recommend(hw(10000, 12, GpuTier.HIGH)));
    }

    @Test
    void midMachineGetsNormal() {
        assertEquals(Preset.NORMAL, RecommendationEngine.recommend(hw(4096, 4, GpuTier.MEDIUM)));
    }

    @Test
    void weakLaptopGetsPotato() {
        assertEquals(Preset.POTATO, RecommendationEngine.recommend(hw(1800, 2, GpuTier.LOW)));
    }

    @Test
    void lowGpuTierNudgesDownOneStep() {
        Preset withMedium = RecommendationEngine.recommend(hw(6000, 6, GpuTier.MEDIUM));
        Preset withLow = RecommendationEngine.recommend(hw(6000, 6, GpuTier.LOW));
        assertTrue(withLow.ordinal() > withMedium.ordinal(),
            "LOW gpu tier should recommend a lighter preset than MEDIUM at the same RAM and cores");
    }

    @Test
    void neverReturnsCustom() {
        assertNotEquals(Preset.CUSTOM, RecommendationEngine.recommend(hw(512, 1, GpuTier.UNKNOWN)));
    }
}
