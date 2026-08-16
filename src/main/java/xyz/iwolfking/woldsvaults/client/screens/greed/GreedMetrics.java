package xyz.iwolfking.woldsvaults.client.screens.greed;

/**
 * The pixel table both greed panels lay themselves out from. Mr. Greedy keeps his fixed 345x189
 * pane and so stays on {@link #compact()}, which is the original hand-tuned layout; the player
 * menu's greed tab fills the same near-full-window inset the talent and statistics tabs use and
 * picks up {@link #roomy()} whenever that inset is actually large enough, which grows every plate,
 * badge, bar and row by roughly a fifth. Font sizes are deliberately absent - text stays at the
 * one size Minecraft draws it.
 */
public final class GreedMetrics {
    private static final int ROOMY_MIN_WIDTH = 380;
    private static final int ROOMY_MIN_HEIGHT = 230;

    public final int margin;
    public final int tabStripY;
    public final int tabHeight;
    public final int contentY;
    public final int medallionSize;
    public final int repBarHeight;
    public final int repFlankWidth;
    public final int unlockBoxHeight;
    public final int mainColumnMaxWidth;
    public final int rowHeight;
    public final int rowGap;
    public final int rowChipWidth;
    public final int rowPinY;
    public final int rowPinWidth;
    public final int rowPinHeight;
    public final int rowRepY;
    public final int rowClaimY;
    public final int rowClaimHeight;
    public final int rowTitleY;
    public final int rowTierY;
    public final int rowBarY;
    public final int rowBarHeight;
    public final int rowNumeralWidth;

    private GreedMetrics(int margin, int tabStripY, int tabHeight, int contentGap, int medallionSize,
                         int repBarHeight, int repFlankWidth, int unlockBoxHeight, int mainColumnMaxWidth,
                         int rowHeight, int rowGap, int rowChipWidth, int rowPinY, int rowPinWidth,
                         int rowPinHeight, int rowRepY, int rowClaimY, int rowClaimHeight, int rowTitleY,
                         int rowTierY, int rowBarY, int rowBarHeight, int rowNumeralWidth) {
        this.margin = margin;
        this.tabStripY = tabStripY;
        this.tabHeight = tabHeight;
        this.contentY = tabStripY + tabHeight + contentGap;
        this.medallionSize = medallionSize;
        this.repBarHeight = repBarHeight;
        this.repFlankWidth = repFlankWidth;
        this.unlockBoxHeight = unlockBoxHeight;
        this.mainColumnMaxWidth = mainColumnMaxWidth;
        this.rowHeight = rowHeight;
        this.rowGap = rowGap;
        this.rowChipWidth = rowChipWidth;
        this.rowPinY = rowPinY;
        this.rowPinWidth = rowPinWidth;
        this.rowPinHeight = rowPinHeight;
        this.rowRepY = rowRepY;
        this.rowClaimY = rowClaimY;
        this.rowClaimHeight = rowClaimHeight;
        this.rowTitleY = rowTitleY;
        this.rowTierY = rowTierY;
        this.rowBarY = rowBarY;
        this.rowBarHeight = rowBarHeight;
        this.rowNumeralWidth = rowNumeralWidth;
    }

    public static GreedMetrics compact() {
        return new GreedMetrics(7, 18, 14, 4, 34, 13, 24, 44, 331,
                40, 3, 66, 2, 30, 10, 15, 26, 11, 4, 15, 28, 8, 14);
    }

    public static GreedMetrics roomy() {
        return new GreedMetrics(9, 20, 17, 5, 44, 16, 28, 52, 430,
                48, 4, 78, 3, 36, 12, 19, 32, 13, 6, 19, 34, 10, 16);
    }

    /**
     * Picks the table a panel of the given size can carry. The roomy table needs about 380x230 of
     * panel to place its stack without the main tab's bottom lines colliding with the pinned row,
     * which the player tab clears at gui scale 3 and below but not at gui scale 4.
     */
    public static GreedMetrics forSize(int width, int height) {
        return width >= ROOMY_MIN_WIDTH && height >= ROOMY_MIN_HEIGHT ? roomy() : compact();
    }
}
