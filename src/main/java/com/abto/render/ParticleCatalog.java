package com.abto.render;

import com.abto.config.AbtoConfig;
import com.abto.config.ConfigStore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Access to every registered particle type and the config that disables them by
 * id. The particle registry is static (all vanilla and mod particle types), so
 * this drives the per-type "disable any particle" list. Ids are the registry key
 * strings, e.g. "minecraft:flame".
 */
public final class ParticleCatalog {

    private ParticleCatalog() {
    }

    /** Every registered particle type id, sorted for a stable list. */
    public static List<String> allTypeIds() {
        List<String> ids = new ArrayList<>();
        for (Identifier id : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
            ids.add(id.toString());
        }
        ids.sort(String::compareTo);
        return ids;
    }

    /** Whether this particle type is currently set to be skipped. */
    public static boolean isDisabled(Path configPath, String id) {
        List<String> list = ConfigStore.load(configPath).featureToggles.disabledParticleTypes;
        return list != null && list.contains(id);
    }

    /** Toggle whether this particle type is skipped; returns the new disabled state. */
    public static boolean flip(Path configPath, String id) {
        AbtoConfig config = ConfigStore.load(configPath);
        List<String> list = config.featureToggles.disabledParticleTypes;
        if (list == null) {
            list = new ArrayList<>();
            config.featureToggles.disabledParticleTypes = list;
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
