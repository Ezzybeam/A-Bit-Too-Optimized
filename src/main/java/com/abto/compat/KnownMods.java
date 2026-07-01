package com.abto.compat;

import java.util.List;

/**
 * Mod ids ABTO knows about. Presence is a soft signal only; none are required.
 * Ids are Fabric mod ids as published on Modrinth. Two groups:
 * PERFORMANCE - mods ABTO tunes/defers to (the Milestone 5 config adapters).
 * COMPAT - mods ABTO must not break (render/culling tweaks must respect them).
 * Some COMPAT ids are best-effort and must be verified against Modrinth during
 * implementation; a wrong id simply never matches, it does not crash anything.
 */
public final class KnownMods {

    public static final List<String> PERFORMANCE = List.of(
        "sodium",
        "sodium-extra",
        "reeses-sodium-options",
        "lithium",
        "iris",
        "ferritecore",
        "immediatelyfast",
        "modernfix",
        "entityculling",
        "dynamic_fps",
        "bobby",
        "voxy",
        "xaerominimap",
        "xaeroworldmap",
        "journeymap"
    );

    public static final List<String> COMPAT = List.of(
        "essential",
        "litematica",
        "minecraftcapes",
        "hypixelplus"
    );

    public static final List<String> IDS = concat(PERFORMANCE, COMPAT);

    private KnownMods() {
    }

    private static List<String> concat(List<String> a, List<String> b) {
        java.util.List<String> all = new java.util.ArrayList<>(a);
        all.addAll(b);
        return List.copyOf(all);
    }
}
