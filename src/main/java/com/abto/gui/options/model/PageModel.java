package com.abto.gui.options.model;

import java.util.List;

/**
 * Holds the ordered tab pages and which one is selected. Pure logic so the
 * Sodium-style screen stays a thin renderer.
 */
public final class PageModel {

    private final List<OptionPageId> pages;
    private OptionPageId selected;

    public PageModel(List<OptionPageId> pages) {
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("PageModel needs at least one page");
        }
        this.pages = List.copyOf(pages);
        this.selected = this.pages.get(0);
    }

    public List<OptionPageId> pages() {
        return pages;
    }

    public OptionPageId selected() {
        return selected;
    }

    public int selectedIndex() {
        return pages.indexOf(selected);
    }

    public void select(OptionPageId page) {
        if (pages.contains(page)) {
            selected = page;
        }
    }
}
