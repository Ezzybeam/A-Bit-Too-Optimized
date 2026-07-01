package com.abto.hardware;

import com.abto.config.HardwareOverrides;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * The hardware values the recommendation engine actually uses: detected values
 * unless the user corrected them. Also classifies the GPU into a coarse tier
 * from its name string, which is the only signal we have for GPU power.
 */
public record EffectiveHardware(
    long allocatedRamMb,
    int cpuCores,
    OptionalLong totalRamMb,
    Optional<String> gpuName,
    GpuTier gpuTier
) {
    public static EffectiveHardware resolve(HardwareInfo detected, HardwareOverrides overrides) {
        long allocated = overrides.allocatedRamMb() != null
            ? overrides.allocatedRamMb() : detected.allocatedRamMb();
        int cores = overrides.cpuCores() != null
            ? overrides.cpuCores() : detected.cpuCores();
        OptionalLong total = overrides.totalRamMb() != null
            ? OptionalLong.of(overrides.totalRamMb()) : detected.totalRamMb();
        Optional<String> gpu = overrides.gpuName() != null
            ? Optional.of(overrides.gpuName()) : detected.gpuName();
        return new EffectiveHardware(allocated, cores, total, gpu, classifyGpu(gpu));
    }

    public static GpuTier classifyGpu(Optional<String> name) {
        if (name.isEmpty()) {
            return GpuTier.UNKNOWN;
        }
        String n = name.get().toLowerCase(Locale.ROOT);
        if (n.contains("rtx") || n.contains("rx 6") || n.contains("rx 7") || n.contains("rx 9")
                || n.contains("arc a7")) {
            return GpuTier.HIGH;
        }
        if (n.contains("gtx") || n.contains("rx 5") || n.contains("arc a3") || n.contains("iris xe")) {
            return GpuTier.MEDIUM;
        }
        if (n.contains("intel") || n.contains("uhd") || n.contains("hd graphics") || n.contains("vega")) {
            return GpuTier.LOW;
        }
        return GpuTier.UNKNOWN;
    }
}
