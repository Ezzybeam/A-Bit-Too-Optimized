package com.abto.hardware;

import com.abto.config.HardwareOverrides;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class EffectiveHardwareTest {
    private static final HardwareOverrides NONE = new HardwareOverrides(null, null, null, null);

    @Test
    void detectedValuesUsedWhenNoOverride() {
        HardwareInfo detected = new HardwareInfo(4096, 8, OptionalLong.of(16384), Optional.of("Radeon RX 7800"));
        EffectiveHardware e = EffectiveHardware.resolve(detected, NONE);
        assertEquals(4096, e.allocatedRamMb());
        assertEquals(8, e.cpuCores());
        assertEquals(OptionalLong.of(16384), e.totalRamMb());
        assertEquals(Optional.of("Radeon RX 7800"), e.gpuName());
    }

    @Test
    void overrideWins() {
        HardwareInfo detected = new HardwareInfo(2048, 4, OptionalLong.empty(), Optional.empty());
        HardwareOverrides ov = new HardwareOverrides(32768L, 8192L, 12, "NVIDIA RTX 4090");
        EffectiveHardware e = EffectiveHardware.resolve(detected, ov);
        assertEquals(8192, e.allocatedRamMb());
        assertEquals(12, e.cpuCores());
        assertEquals(OptionalLong.of(32768), e.totalRamMb());
        assertEquals(Optional.of("NVIDIA RTX 4090"), e.gpuName());
    }

    @Test
    void gpuTierClassification() {
        assertEquals(GpuTier.HIGH, EffectiveHardware.classifyGpu(Optional.of("NVIDIA GeForce RTX 4070")));
        assertEquals(GpuTier.HIGH, EffectiveHardware.classifyGpu(Optional.of("AMD Radeon RX 7800 XT")));
        assertEquals(GpuTier.MEDIUM, EffectiveHardware.classifyGpu(Optional.of("NVIDIA GeForce GTX 1660")));
        assertEquals(GpuTier.LOW, EffectiveHardware.classifyGpu(Optional.of("Intel UHD Graphics 620")));
        assertEquals(GpuTier.UNKNOWN, EffectiveHardware.classifyGpu(Optional.empty()));
        assertEquals(GpuTier.UNKNOWN, EffectiveHardware.classifyGpu(Optional.of("Some Mystery GPU")));
    }
}
