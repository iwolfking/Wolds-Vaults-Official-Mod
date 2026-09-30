package xyz.iwolfking.woldsvaults.items.trinket_pouch.menu;

public final class PouchLayout {
    public static final int WIDTH = 288;
    public static final int HEIGHT = 198;
    public static final int TAB_Y = 5;
    public static final int TAB_WIDTH = 22;
    public static final int TAB_HEIGHT = 22;
    public static final int TAB_GAP = 2;
    public static final int GRID_X = 9;
    public static final int GRID_Y = 24;
    public static final int INVENTORY_Y = 94;
    public static final int HOTBAR_Y = 154;
    public static final int COLLECTION_X = 11;
    public static final int COLLECTION_Y = 52;
    public static final int COLUMN_COUNT = 8;
    public static final int ROW_COUNT = 5;
    public static final int CELL_SIZE = 20;
    public static final int CELL_PITCH = 20;
    public static final int ROW_PITCH = 23;
    public static final int COLLECTION_SCROLL_HEIGHT = 115;
    public static final int VISIBLE_CELLS = COLUMN_COUNT * ROW_COUNT;
    public static final int PRESET_PANEL_X = 178;
    public static final int PRESET_PANEL_Y = 24;
    public static final int PRESET_PANEL_WIDTH = 102;
    public static final int PREVIEW_COLUMNS = 3;
    public static final int PREVIEW_ROWS = 2;
    public static final int PREVIEW_X = 198;
    public static final int PREVIEW_Y = 67;
    public static final int PREVIEW_PITCH = 21;
    public static final int PREVIEW_PANEL_HEIGHT = 149;
    public static final int PREVIEW_SCROLL_HEIGHT = 41;

    public static final int FOOTER_Y = 178;

    public static int tabY(int index) { return TAB_Y + index * (TAB_HEIGHT + TAB_GAP); }
    public static int panelLeft(int screenWidth) { return (screenWidth - WIDTH + TAB_WIDTH) / 2; }
    public static int panelTop(int screenHeight, int panelHeight) { return (screenHeight - panelHeight) / 2; }
    private PouchLayout() {}
}
