package com.abto.gui;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ButtonColumnTest {
    @Test
    void allSlotsFitOnTinyScreen() {
        // The crash case: GUI scale 4 -> ~240px tall, 11 buttons.
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(240, 11, 20, 24, 6);
        assertEquals(11, slots.size());
        for (ButtonColumn.Slot s : slots) {
            assertTrue(s.y() >= 0, "y negative: " + s.y());
            assertTrue(s.y() + s.height() <= 240, "off bottom: " + (s.y() + s.height()));
            assertTrue(s.height() >= 8, "button too short: " + s.height());
        }
    }

    @Test
    void usesPreferredSizeWhenRoomy() {
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(600, 5, 20, 24, 6);
        assertEquals(20, slots.get(0).height());
        // centered group fits comfortably
        for (ButtonColumn.Slot s : slots) {
            assertTrue(s.y() >= 0 && s.y() + s.height() <= 600);
        }
    }

    @Test
    void slotsAreOrderedTopToBottom() {
        List<ButtonColumn.Slot> slots = ButtonColumn.layout(240, 11, 20, 24, 6);
        for (int i = 1; i < slots.size(); i++) {
            assertTrue(slots.get(i).y() >= slots.get(i - 1).y());
        }
    }
}
