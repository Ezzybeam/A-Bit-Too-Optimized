package com.abto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AbtoSmokeTest {
    @Test
    void modIdIsAbto() {
        assertEquals("abto", Abto.MOD_ID);
    }
}
