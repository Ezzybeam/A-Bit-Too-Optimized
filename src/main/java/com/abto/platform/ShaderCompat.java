package com.abto.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/**
 * Reports whether an Iris shader pack is currently active. ABTO's sky/fog/weather
 * render mixins check this and skip their cancels when a shader pack is in use, so
 * they never fight the shader's own sky/fog/weather rendering. Uses reflection so
 * ABTO has no hard dependency on Iris; returns false when Iris is absent.
 */
public final class ShaderCompat {

    private static final boolean IRIS_PRESENT = FabricLoader.getInstance().isModLoaded("iris");
    private static Method getInstance;
    private static Method isShaderPackInUse;
    private static boolean lookedUp;

    private ShaderCompat() {
    }

    public static boolean shaderPackActive() {
        if (!IRIS_PRESENT) {
            return false;
        }
        try {
            if (!lookedUp) {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                getInstance = api.getMethod("getInstance");
                isShaderPackInUse = api.getMethod("isShaderPackInUse");
                lookedUp = true;
            }
            Object instance = getInstance.invoke(null);
            return (Boolean) isShaderPackInUse.invoke(instance);
        } catch (Throwable ignored) {
            // Iris API shape changed or unavailable: assume no shader pack rather than crash.
            return false;
        }
    }
}
