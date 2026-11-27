package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SpellBarStrip;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.List;

final class EditorLayout {
    static final int PAD = 4;
    static final int TITLE_Y = 4;
    static final int TITLE_H = 22;
    static final int POUCH_W = 168;
    static final int ROW = 21;
    static final int ICON = 20;
    private static final int CELL = 26;
    private static final int CELL_PITCH_Y = 28;
    static final int TAB = 20;
    static final int WEAVE_TOP = 11;
    static final int WEAVE_ROWS_MAX = 2;
    private static final int NAME_MIN_W = 60;

    record Cell(int index, int x, int y) {
    }

    private EditorLayout() {
    }

    static int bodyTop() {
        return TITLE_Y + TITLE_H + 3;
    }

    static int bodyBottom(int height) {
        return height - 21 - SpellBarStrip.HEIGHT - 3;
    }

    static int rightX0() {
        return 21 + POUCH_W + 3;
    }

    static int innerX0() {
        return rightX0() + PAD;
    }

    static int innerX1(int width) {
        return width - 21 - PAD;
    }

    static int weaveTop() {
        return bodyTop() + 4 + TAB + 3;
    }

    /** The weave's height with {@code rows} rows of cells and equal margins above and below them. */
    static int weaveHeight(int rows) {
        return 2 * WEAVE_TOP + rows * CELL_PITCH_Y - (CELL_PITCH_Y - ICON);
    }

    static int weaveBottom(int rows) {
        return weaveTop() + weaveHeight(rows);
    }

    /**
     * How many weave rows to show: the rows the spell needs, at most {@link #WEAVE_ROWS_MAX}, and fewer when the
     * stats box below would get too short for the spell column.
     */
    static int shownWeaveRows(int width, int height, int tokens) {
        int shown = Math.min(WEAVE_ROWS_MAX, weaveRows(tokens, perRow(innerX0(), innerX1(width))));
        while (shown > 1 && statsBottom(height) - statsTop(shown) < StatsPanel.spellColumnHeight()) {
            shown--;
        }
        return Math.max(1, shown);
    }

    static boolean overTitle(double mouseX, double mouseY, int width) {
        return SpellcraftDraw.inside(mouseX, mouseY, 21, TITLE_Y, width - 42, TITLE_H);
    }

    static int maxTabs(int width, int drafts) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        int byTable = config == null ? drafts : config.rules.maxSpellsPerItem;
        int byRoom = (width - 21 - PAD - NAME_MIN_W - rightX0() - PAD) / (TAB + 2);
        return Math.max(1, Math.min(byTable, byRoom - 1));
    }

    static int shownTabs(int width, int drafts) {
        return Math.min(drafts, maxTabs(width, drafts));
    }

    static int tabCount(int width, int drafts) {
        return shownTabs(width, drafts) + (drafts < maxTabs(width, drafts) ? 1 : 0);
    }

    static int tabX(int index) {
        return rightX0() + PAD + index * (TAB + 2);
    }

    static int tabY() {
        return bodyTop() + 4;
    }

    static int nameX(int width, int drafts) {
        return rightX0() + PAD + tabCount(width, drafts) * (TAB + 2) + 2;
    }

    static int pouchTabWidth() {
        return (POUCH_W - 2 * PAD - 2) / 2;
    }

    static int pouchTabX(int index) {
        return 21 + PAD + index * (pouchTabWidth() + 2);
    }

    static int pouchListX0() {
        return 21 + PAD;
    }

    static int pouchListX1() {
        return 21 + POUCH_W - PAD;
    }

    static int pouchListTop() {
        return bodyTop() + 19;
    }

    static int pouchListBottom(int height) {
        return bodyBottom(height) - PAD;
    }

    static int pouchFit(int height) {
        return Math.max(1, (pouchListBottom(height) - pouchListTop() - 2) / ROW);
    }

    static boolean overPouchList(double mouseX, double mouseY, int height) {
        return SpellcraftDraw.inside(mouseX, mouseY, pouchListX0(), pouchListTop() + 2, pouchListX1() - pouchListX0(), pouchFit(height) * ROW);
    }

    static boolean overPouch(double mouseX, double mouseY, int height) {
        return SpellcraftDraw.inside(mouseX, mouseY, 21, bodyTop(), POUCH_W, bodyBottom(height) - bodyTop());
    }

    static int perRow(int x0, int x1) {
        return Math.max(1, (x1 - x0 - 12) / CELL);
    }

    static int weaveRows(int tokens, int perRow) {
        return (tokens + 1 + perRow - 1) / perRow;
    }

    static List<Cell> cells(int tokens, int scroll, int rows, int x0, int y0, int x1) {
        List<Cell> cells = new ArrayList<>();
        int count = tokens + 1;
        int perRow = perRow(x0, x1);
        for (int i = 0; i < count; i++) {
            int row = i / perRow - scroll;
            if (row < 0 || row >= rows) {
                continue;
            }
            cells.add(new Cell(i, x0 + 8 + (i % perRow) * CELL, y0 + WEAVE_TOP + row * CELL_PITCH_Y));
        }
        return cells;
    }

    static boolean overWeave(double mouseX, double mouseY, int width, int rows) {
        return SpellcraftDraw.inside(mouseX, mouseY, innerX0(), weaveTop(), innerX1(width) - innerX0(), weaveHeight(rows));
    }

    static int statsTop(int rows) {
        return weaveBottom(rows) + 3;
    }

    static int statsBottom(int height) {
        return bodyBottom(height) - PAD;
    }
}
