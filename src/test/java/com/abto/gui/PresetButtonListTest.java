package com.abto.gui;

import com.abto.preset.Preset;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PresetButtonListTest {
    @Test
    void hasAllSixTunablePresetsPlusCustomWithNonBlankText() {
        List<PresetButtonList.Entry> entries = PresetButtonList.entries();
        assertEquals(7, entries.size());
        assertEquals(Preset.ULTRA, entries.get(0).preset());
        assertEquals(Preset.CUSTOM, entries.get(entries.size() - 1).preset());
        for (PresetButtonList.Entry e : entries) {
            assertFalse(e.label().isBlank(), "label blank for " + e.preset());
            assertFalse(e.description().isBlank(), "description blank for " + e.preset());
        }
    }
}
