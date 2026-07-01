package com.abto.wizard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OverrideInputTest {
    @Test
    void parsesValidPositiveLong() {
        assertEquals(16384L, OverrideInput.parsePositiveLong("16384").getAsLong());
    }

    @Test
    void blankIsEmptyNotError() {
        assertTrue(OverrideInput.parsePositiveLong("").isEmpty());
        assertTrue(OverrideInput.parsePositiveLong("   ").isEmpty());
    }

    @Test
    void invalidOrNonPositiveIsEmpty() {
        assertTrue(OverrideInput.parsePositiveLong("abc").isEmpty());
        assertTrue(OverrideInput.parsePositiveLong("-5").isEmpty());
        assertTrue(OverrideInput.parsePositiveLong("0").isEmpty());
    }

    @Test
    void parsesIntAndTrimsWhitespace() {
        assertEquals(8, OverrideInput.parsePositiveInt(" 8 ").getAsInt());
        assertTrue(OverrideInput.parsePositiveInt("nope").isEmpty());
    }

    @Test
    void cleanGpuNameTrimsAndRejectsBlank() {
        assertEquals("NVIDIA RTX 4070", OverrideInput.cleanGpuName("  NVIDIA RTX 4070 ").get());
        assertTrue(OverrideInput.cleanGpuName("   ").isEmpty());
    }
}
