package com.abto.compat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Reports which of the known mods are loaded. Takes an "is this id loaded"
 * predicate so it can be unit tested without a Fabric runtime; the real
 * predicate (FabricLoader::isModLoaded) is supplied in com.abto.platform.
 */
public final class ModPresenceDetector {

    private ModPresenceDetector() {
    }

    public static Set<String> detect(Predicate<String> isLoaded) {
        Set<String> present = new LinkedHashSet<>();
        for (String id : KnownMods.IDS) {
            if (isLoaded.test(id)) {
                present.add(id);
            }
        }
        return present;
    }

    public static boolean isPresent(Set<String> present, String id) {
        return present.contains(id);
    }
}
