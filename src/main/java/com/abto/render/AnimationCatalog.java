package com.abto.render;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The set of animated textures the game has actually ticked, and the config that
 * disables them by sprite id. Unlike particles, animated textures are not a static
 * registry - they come from the loaded resource packs - so this catalog is
 * populated as {@link SpriteAnimationFilter} sees each sprite tick. Once in a
 * world the block atlas ticks every animated sprite each client tick, so the list
 * fills out completely. Ids are sprite names, e.g. "minecraft:block/water_still".
 */
public final class AnimationCatalog {

    private static final Set<String> KNOWN = ConcurrentHashMap.newKeySet();

    private AnimationCatalog() {
    }

    /** Records a sprite id seen ticking, so it can appear in the per-type list. */
    public static void record(String id) {
        KNOWN.add(id);
    }

    /** Every animated sprite id seen so far, sorted for a stable list. */
    public static List<String> knownIds() {
        List<String> ids = new ArrayList<>(KNOWN);
        ids.sort(String::compareTo);
        return ids;
    }

    public static boolean isDisabled(Path configPath, String id) {
        List<String> list = ConfigStore.load(configPath).featureToggles.disabledSprites;
        return list != null && list.contains(id);
    }

    /** Toggle whether this sprite's animation is frozen; returns the new state. */
    public static boolean flip(Path configPath, String id) {
        AbtoConfig config = ConfigStore.load(configPath);
        List<String> list = config.featureToggles.disabledSprites;
        if (list == null) {
            list = new ArrayList<>();
            config.featureToggles.disabledSprites = list;
        }
        boolean nowDisabled;
        if (list.contains(id)) {
            list.remove(id);
            nowDisabled = false;
        } else {
            list.add(id);
            nowDisabled = true;
        }
        ConfigStore.save(configPath, config);
        RenderToggles.apply(config.featureToggles);
        return nowDisabled;
    }
}
