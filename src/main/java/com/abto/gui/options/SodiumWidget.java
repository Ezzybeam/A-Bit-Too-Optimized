package com.abto.gui.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * A flat, Sodium-style clickable widget. Draws a dark translucent panel with a
 * hover highlight, a thin border, and left-aligned text - no vanilla 3D button
 * sprite. Three roles: PLAIN (a normal action button), TOGGLE (shows a green
 * accent and tinted background when on), and TAB (a category selector with an
 * accent bar when selected). Position and click behaviour are the standard
 * AbstractWidget ones; only the paint is custom.
 */
public final class SodiumWidget extends AbstractWidget {

    public enum Role { PLAIN, TOGGLE, TAB }

    private static final int NORMAL_BG = 0x22FFFFFF;
    private static final int HOVER_BG = 0x44FFFFFF;
    private static final int ON_BG = 0x552ECC71;
    private static final int ON_HOVER_BG = 0x772ECC71;
    private static final int TAB_SELECTED_BG = 0x40FFFFFF;
    private static final int ACCENT = 0xFF2ECC71;
    private static final int BORDER = 0x33FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFBFBFBF;

    private final Runnable onPress;
    private Role role = Role.PLAIN;
    private boolean on;
    private boolean selected;

    public SodiumWidget(int x, int y, int width, int height, Component message, Runnable onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    public SodiumWidget role(Role role) {
        this.role = role;
        return this;
    }

    public SodiumWidget on(boolean on) {
        this.on = on;
        return this;
    }

    public SodiumWidget selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float delta) {
        int x = getX();
        int y = getY();
        int right = x + this.width;
        int bottom = y + this.height;
        boolean hovered = isHoveredOrFocused();

        int bg;
        if (role == Role.TOGGLE && on) {
            bg = hovered ? ON_HOVER_BG : ON_BG;
        } else if (role == Role.TAB && selected) {
            bg = TAB_SELECTED_BG;
        } else {
            bg = hovered ? HOVER_BG : NORMAL_BG;
        }
        g.fill(x, y, right, bottom, bg);

        // Accent bar on the left for an enabled toggle or the current tab.
        if ((role == Role.TOGGLE && on) || (role == Role.TAB && selected)) {
            g.fill(x, y, x + 2, bottom, ACCENT);
        }

        g.outline(x, y, this.width, this.height, BORDER);

        Minecraft mc = Minecraft.getInstance();
        int textColor = this.active ? (hovered ? TEXT : TEXT_DIM) : 0xFF808080;
        int textY = y + (this.height - 8) / 2;
        int textX = x + (role == Role.TAB ? 8 : 6);
        g.text(mc.font, getMessage(), textX, textY, textColor);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (this.onPress != null) {
            this.onPress.run();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
