package com.abto.gui.options.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageModelTest {
    @Test
    void defaultsToFirstPage() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO, OptionPageId.TWEAKS));
        assertEquals(OptionPageId.PRESETS, m.selected());
        assertEquals(0, m.selectedIndex());
    }

    @Test
    void selectChangesPage() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO, OptionPageId.TWEAKS));
        m.select(OptionPageId.VIDEO);
        assertEquals(OptionPageId.VIDEO, m.selected());
        assertEquals(1, m.selectedIndex());
    }

    @Test
    void selectingUnknownPageIsIgnored() {
        PageModel m = new PageModel(List.of(OptionPageId.PRESETS, OptionPageId.VIDEO));
        m.select(OptionPageId.TWEAKS); // not in this model
        assertEquals(OptionPageId.PRESETS, m.selected());
    }
}
