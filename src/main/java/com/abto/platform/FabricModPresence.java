package com.abto.platform;

import net.fabricmc.loader.api.FabricLoader;
import java.util.function.Predicate;

/** Supplies the real "is this mod loaded" predicate for the detector. */
public final class FabricModPresence {

    private FabricModPresence() {
    }

    public static Predicate<String> isLoaded() {
        return id -> FabricLoader.getInstance().isModLoaded(id);
    }
}
