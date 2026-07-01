package com.abto.config;

/**
 * User corrections for hardware values. A null field means "no override, use the
 * detected value". Stored in the config file.
 */
public record HardwareOverrides(
    Long totalRamMb,
    Long allocatedRamMb,
    Integer cpuCores,
    String gpuName
) {
    public static HardwareOverrides none() {
        return new HardwareOverrides(null, null, null, null);
    }
}
