package com.abto;

import net.fabricmc.api.ClientModInitializer;

public final class AbtoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Abto.LOGGER.info("A Bit Too Optimized loaded. No features yet, skeleton only.");
    }
}
