package com.abto.hardware;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Builds a HardwareInfo from injected suppliers. Takes suppliers rather than
 * calling Runtime or OpenGL directly so it can be unit tested without a JVM
 * runtime or render context. The real suppliers live in com.abto.platform.
 */
public final class HardwareProbe {

    private HardwareProbe() {
    }

    public static HardwareInfo detect(
            LongSupplier maxMemoryBytes,
            IntSupplier cpuCores,
            Supplier<OptionalLong> totalRamMb,
            Supplier<Optional<String>> gpuName) {
        long allocatedMb = maxMemoryBytes.getAsLong() / (1024 * 1024);
        return new HardwareInfo(allocatedMb, cpuCores.getAsInt(), totalRamMb.get(), gpuName.get());
    }
}
