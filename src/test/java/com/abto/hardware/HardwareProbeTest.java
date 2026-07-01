package com.abto.hardware;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalLong;
import static org.junit.jupiter.api.Assertions.*;

class HardwareProbeTest {
    @Test
    void convertsBytesToMegabytesAndPassesValuesThrough() {
        HardwareInfo info = HardwareProbe.detect(
            () -> 4096L * 1024 * 1024,   // 4096 MB allocated
            () -> 8,
            () -> OptionalLong.of(16384),
            () -> Optional.of("NVIDIA GeForce RTX 4070"));
        assertEquals(4096, info.allocatedRamMb());
        assertEquals(8, info.cpuCores());
        assertEquals(OptionalLong.of(16384), info.totalRamMb());
        assertEquals(Optional.of("NVIDIA GeForce RTX 4070"), info.gpuName());
    }

    @Test
    void unknownTotalRamAndGpuAreEmpty() {
        HardwareInfo info = HardwareProbe.detect(
            () -> 2048L * 1024 * 1024,
            () -> 4,
            OptionalLong::empty,
            Optional::empty);
        assertTrue(info.totalRamMb().isEmpty());
        assertTrue(info.gpuName().isEmpty());
    }
}
