package com.abto.compat;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ModPresenceDetectorTest {
    @Test
    void reportsOnlyKnownLoadedMods() {
        Set<String> loaded = Set.of("sodium", "iris", "some-unknown-mod");
        Set<String> present = ModPresenceDetector.detect(loaded::contains);
        assertTrue(present.contains("sodium"));
        assertTrue(present.contains("iris"));
        assertFalse(present.contains("some-unknown-mod"), "unknown mods are not reported");
        assertFalse(present.contains("lithium"), "lithium was not loaded");
    }

    @Test
    void emptyWhenNothingLoaded() {
        assertTrue(ModPresenceDetector.detect(id -> false).isEmpty());
    }

    @Test
    void knownIdsIncludePerformanceAndCompatMods() {
        assertTrue(KnownMods.IDS.containsAll(Set.of(
            "sodium", "lithium", "iris", "bobby", "voxy")), "performance mods");
        assertTrue(KnownMods.IDS.containsAll(Set.of(
            "essential", "litematica", "minecraftcapes")), "compat mods");
    }
}
