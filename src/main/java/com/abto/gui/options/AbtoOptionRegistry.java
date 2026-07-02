package com.abto.gui.options;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import com.abto.render.RenderToggles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single source of truth for ABTO's render feature toggles. Both the
 * Minecraft-style and Sodium-style screens read this list, so a toggle is
 * defined exactly once and appears identically in every UI. Categories are
 * emitted in a stable order (declaration order) so the screens can group by
 * heading or tab without deciding the order themselves.
 */
public final class AbtoOptionRegistry {

    public static final String CAT_PERF = "Performance";
    public static final String CAT_SKY = "Sky and Fog";
    public static final String CAT_ANIM = "Animations and Weather";
    public static final String CAT_PARTICLES = "Particles";
    public static final String CAT_ENTITIES = "Entities and Blocks";

    private static final List<ToggleOption> TOGGLES = List.of(
        // Performance (real optimizations that keep visuals intact)
        new ToggleOption(CAT_PERF, "Entity culling",
            "Skip drawing entities fully hidden behind solid blocks. No visual change; "
                + "saves GPU work in walled-off areas like mob farms.",
            ft -> ft.entityCulling, (ft, v) -> ft.entityCulling = v),

        // Sky and Fog
        new ToggleOption(CAT_SKY, "Hide clouds", "Skip cloud rendering entirely.",
            ft -> ft.hideClouds, (ft, v) -> ft.hideClouds = v),
        new ToggleOption(CAT_SKY, "Hide stars", "Skip star rendering at night.",
            ft -> ft.hideStars, (ft, v) -> ft.hideStars = v),
        new ToggleOption(CAT_SKY, "Hide sun and moon", "Skip rendering the sun and moon.",
            ft -> ft.hideSunMoon, (ft, v) -> ft.hideSunMoon = v),
        new ToggleOption(CAT_SKY, "Hide sky", "Skip the sky gradient. Leaves a plain background.",
            ft -> ft.hideSky, (ft, v) -> ft.hideSky = v),
        new ToggleOption(CAT_SKY, "Disable fog",
            "Remove all fog (Nether, water, lava, and blindness/darkness fog too).",
            ft -> ft.disableFog, (ft, v) -> ft.disableFog = v),

        // Animations and Weather
        new ToggleOption(CAT_ANIM, "Disable block animations",
            "Freeze animated textures (water, lava, fire, portal) to save FPS.",
            ft -> ft.disableBlockAnimations, (ft, v) -> ft.disableBlockAnimations = v),
        new ToggleOption(CAT_ANIM, "Disable weather",
            "Do not render rain or snow. A solid FPS win in storms.",
            ft -> ft.disableWeatherRendering, (ft, v) -> ft.disableWeatherRendering = v),

        // Particles
        new ToggleOption(CAT_PARTICLES, "Disable all particles",
            "Create no particles at all (a hard off beyond the vanilla Minimal setting).",
            ft -> ft.disableAllParticles, (ft, v) -> ft.disableAllParticles = v),
        new ToggleOption(CAT_PARTICLES, "Disable block particles",
            "Skip block break, mining, and dust particles (kept when 'all particles' is off).",
            ft -> ft.disableBlockParticles, (ft, v) -> ft.disableBlockParticles = v),
        new ToggleOption(CAT_PARTICLES, "Disable rain splash particles",
            "Skip the splash particles rain makes on the ground.",
            ft -> ft.disableRainSplashParticles, (ft, v) -> ft.disableRainSplashParticles = v),
        new ToggleOption(CAT_PARTICLES, "Disable weather particles",
            "Skip the rain splash particles spawned by weather.",
            ft -> ft.disableWeatherParticles, (ft, v) -> ft.disableWeatherParticles = v),

        // Entities and Blocks
        new ToggleOption(CAT_ENTITIES, "Hide item frames",
            "Do not render item frames or the items inside them. Big win in storage rooms.",
            ft -> ft.hideItemFrames, (ft, v) -> ft.hideItemFrames = v),
        new ToggleOption(CAT_ENTITIES, "Hide armor stands",
            "Do not render armor stands or the gear on them.",
            ft -> ft.hideArmorStands, (ft, v) -> ft.hideArmorStands = v),
        new ToggleOption(CAT_ENTITIES, "Hide paintings", "Do not render paintings hung on walls.",
            ft -> ft.hidePaintings, (ft, v) -> ft.hidePaintings = v),
        new ToggleOption(CAT_ENTITIES, "Hide beacon beams",
            "Do not render the beam of light shot up by beacons.",
            ft -> ft.hideBeaconBeams, (ft, v) -> ft.hideBeaconBeams = v),
        new ToggleOption(CAT_ENTITIES, "Hide moving pistons",
            "Skip the animated render of pistons while they extend or retract.",
            ft -> ft.hideMovingPistons, (ft, v) -> ft.hideMovingPistons = v),
        new ToggleOption(CAT_ENTITIES, "Hide enchant table book",
            "Do not render the floating book above enchanting tables.",
            ft -> ft.hideEnchantTableBook, (ft, v) -> ft.hideEnchantTableBook = v),
        new ToggleOption(CAT_ENTITIES, "Hide name tags",
            "Hide the floating names above players, mobs, and named entities.",
            ft -> ft.hideNameTags, (ft, v) -> ft.hideNameTags = v),
        new ToggleOption(CAT_ENTITIES, "Hide sign text",
            "Skip rendering the text on signs. The sign board itself stays.",
            ft -> ft.hideSignText, (ft, v) -> ft.hideSignText = v)
    );

    private AbtoOptionRegistry() {
    }

    /** All toggles in declaration order. */
    public static List<ToggleOption> all() {
        return TOGGLES;
    }

    /** Category names in first-seen order. */
    public static List<String> categories() {
        List<String> out = new ArrayList<>();
        for (ToggleOption t : TOGGLES) {
            if (!out.contains(t.category())) {
                out.add(t.category());
            }
        }
        return out;
    }

    /** Toggles grouped by category, preserving declaration order within each group. */
    public static Map<String, List<ToggleOption>> byCategory() {
        Map<String, List<ToggleOption>> out = new LinkedHashMap<>();
        for (ToggleOption t : TOGGLES) {
            out.computeIfAbsent(t.category(), k -> new ArrayList<>()).add(t);
        }
        return out;
    }

    /** Current persisted value of a toggle. */
    public static boolean current(Path configPath, ToggleOption option) {
        return option.getter().test(ConfigStore.load(configPath).featureToggles);
    }

    /**
     * Flip a toggle: load config, invert the value, save, and refresh RenderToggles
     * so the render mixins see the change live. Returns the new value.
     */
    public static boolean flip(Path configPath, ToggleOption option) {
        AbtoConfig c = ConfigStore.load(configPath);
        boolean next = !option.getter().test(c.featureToggles);
        option.setter().accept(c.featureToggles, next);
        ConfigStore.save(configPath, c);
        RenderToggles.apply(c.featureToggles);
        return next;
    }
}
