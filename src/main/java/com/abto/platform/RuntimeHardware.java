package com.abto.platform;

import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Real hardware values for the HardwareProbe. Allocated RAM and core count are
 * reliable; total system RAM is read via the JVM OperatingSystemMXBean and may
 * be unavailable on some JVMs (returned empty). GPU name is filled in separately
 * from the OpenGL renderer string once the render context exists.
 */
public final class RuntimeHardware {

    private RuntimeHardware() {
    }

    public static long maxMemoryBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    public static int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static OptionalLong totalRamMb() {
        try {
            java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
            if (base instanceof com.sun.management.OperatingSystemMXBean os) {
                long bytes = os.getTotalMemorySize();
                if (bytes > 0) {
                    return OptionalLong.of(bytes / (1024 * 1024));
                }
            }
        } catch (Throwable ignored) {
            // Some JVMs do not expose com.sun.management; treat as unknown.
        }
        return OptionalLong.empty();
    }

    public static Optional<String> gpuName() {
        // Filled in elsewhere once the GL context exists; unknown at init time.
        return Optional.empty();
    }
}
