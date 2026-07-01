package com.abto.recommend;

import com.abto.hardware.EffectiveHardware;
import com.abto.hardware.GpuTier;
import com.abto.preset.Preset;
import com.abto.preset.Presets;

/**
 * Maps effective hardware to a recommended preset. Thresholds are documented
 * constants so they are easy to tune. RAM and cores pick a base tier; GPU tier
 * nudges it one step. The base tier uses the weaker of the RAM signal and the
 * core signal, so a machine with lots of RAM but few cores is not over-rated.
 */
public final class RecommendationEngine {

    // Allocated-RAM thresholds in megabytes, from strongest to weakest.
    private static final long ULTRA_RAM_MB = 8000;
    private static final long HIGH_RAM_MB = 6000;
    private static final long NORMAL_RAM_MB = 4000;
    private static final long LOW_RAM_MB = 3000;
    private static final long VERY_LOW_RAM_MB = 2000;

    // CPU-core thresholds, from strongest to weakest.
    private static final int ULTRA_CORES = 8;
    private static final int HIGH_CORES = 6;
    private static final int NORMAL_CORES = 4;
    private static final int LOW_CORES = 4;
    private static final int VERY_LOW_CORES = 2;

    private RecommendationEngine() {
    }

    public static Preset recommend(EffectiveHardware hw) {
        int ramIndex = ramTierIndex(hw.allocatedRamMb());
        int coreIndex = coreTierIndex(hw.cpuCores());
        // Higher index means lighter preset; take the weaker (higher) of the two.
        int base = Math.max(ramIndex, coreIndex);
        base += gpuNudge(hw.gpuTier());
        base = Math.max(0, Math.min(Presets.ORDER.size() - 1, base));
        return Presets.ORDER.get(base);
    }

    private static int ramTierIndex(long ramMb) {
        if (ramMb >= ULTRA_RAM_MB) return 0;   // ULTRA
        if (ramMb >= HIGH_RAM_MB) return 1;     // HIGH
        if (ramMb >= NORMAL_RAM_MB) return 2;   // NORMAL
        if (ramMb >= LOW_RAM_MB) return 3;      // LOW
        if (ramMb >= VERY_LOW_RAM_MB) return 4; // VERY_LOW
        return 5;                                // POTATO
    }

    private static int coreTierIndex(int cores) {
        if (cores >= ULTRA_CORES) return 0;
        if (cores >= HIGH_CORES) return 1;
        if (cores >= NORMAL_CORES) return 2;
        if (cores >= LOW_CORES) return 3;
        if (cores >= VERY_LOW_CORES) return 4;
        return 5;
    }

    private static int gpuNudge(GpuTier tier) {
        return switch (tier) {
            case HIGH -> -1;   // one step stronger
            case LOW -> 1;     // one step lighter
            case MEDIUM, UNKNOWN -> 0;
        };
    }
}
