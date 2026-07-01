package com.abto.hardware;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Detected hardware values. Values that could not be read are represented as
 * empty Optionals rather than guesses, so the recommendation engine can be
 * conservative about what it does not know.
 */
public record HardwareInfo(
    long allocatedRamMb,
    int cpuCores,
    OptionalLong totalRamMb,
    Optional<String> gpuName
) {
}
