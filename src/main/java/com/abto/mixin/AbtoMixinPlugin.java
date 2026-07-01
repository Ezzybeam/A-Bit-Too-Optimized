package com.abto.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates version-specific mixins by class presence so a single jar runs on
 * Minecraft versions whose internals differ. LevelExtractorMixin targets a class
 * (net.minecraft.client.renderer.extract.LevelExtractor) that exists on 26.2+ but
 * not on 26.1.2; without this gate, applying it on 26.1.2 would crash at startup
 * on the missing target. The check uses getResource so it never loads or
 * initializes the class.
 */
public class AbtoMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("LevelExtractorMixin")) {
            return classPresent("net.minecraft.client.renderer.extract.LevelExtractor");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
            String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
            String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean classPresent(String className) {
        String path = className.replace('.', '/') + ".class";
        return AbtoMixinPlugin.class.getClassLoader().getResource(path) != null;
    }
}
