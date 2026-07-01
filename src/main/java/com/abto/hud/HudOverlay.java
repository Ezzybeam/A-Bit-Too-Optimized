package com.abto.hud;

import com.abto.render.RenderToggles;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * A small top-left HUD overlay showing FPS, coordinates, and facing direction,
 * each independently toggleable. Registered through Fabric's HudElementRegistry so
 * one implementation draws on every supported Minecraft version despite their
 * differing HUD internals. Registered as a HUD element, so it is skipped
 * automatically when the HUD is hidden (F1). Draws nothing when all readouts are
 * off, so it costs nothing unless the player turns something on.
 */
public final class HudOverlay implements HudElement {

    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        List<String> lines = new ArrayList<>(3);
        if (RenderToggles.showFps()) {
            lines.add(mc.getFps() + " fps");
        }
        if (RenderToggles.showCoords() && mc.player != null) {
            lines.add(String.format("XYZ %.1f / %.1f / %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (RenderToggles.showFacing() && mc.player != null) {
            lines.add("Facing " + mc.player.getDirection().getName());
        }
        if (lines.isEmpty()) {
            return;
        }

        int y = MARGIN;
        for (String line : lines) {
            g.text(mc.font, line, MARGIN, y, WHITE, true);
            y += LINE_HEIGHT;
        }
    }
}
