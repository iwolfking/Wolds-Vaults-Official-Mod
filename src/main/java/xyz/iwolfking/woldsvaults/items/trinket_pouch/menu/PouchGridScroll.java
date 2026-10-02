package xyz.iwolfking.woldsvaults.items.trinket_pouch.menu;

public final class PouchGridScroll {
    private final int columns;
    private final int visibleRows;
    private int entryCount;
    private int firstRow;

    public PouchGridScroll(int columns, int visibleRows) {
        if (columns < 1 || visibleRows < 1) {
            throw new IllegalArgumentException("Grid columns and visible rows must be positive");
        }
        this.columns = columns;
        this.visibleRows = visibleRows;
    }

    public void setEntryCount(int entryCount) {
        if (entryCount < 0) throw new IllegalArgumentException("Grid entry count cannot be negative");
        this.entryCount = entryCount;
        setFirstRow(firstRow);
    }

    public void setFirstRow(int row) { firstRow = Math.max(0, Math.min(maxFirstRow(), row)); }
    public void scroll(int rows) { setFirstRow(firstRow + rows); }
    public int firstRow() { return firstRow; }
    public int totalRows() { return (entryCount + columns - 1) / columns; }
    public int maxFirstRow() { return Math.max(0, totalRows() - visibleRows); }
    public boolean canScroll() { return maxFirstRow() > 0; }

    public int entryIndex(int cell) {
        if (cell < 0 || cell >= columns * visibleRows) return -1;
        int index = firstRow * columns + cell;
        return index < entryCount ? index : -1;
    }

    public int thumbHeight(int trackHeight) {
        return canScroll() ? Math.min(trackHeight, Math.max(8, trackHeight * visibleRows / totalRows())) : trackHeight;
    }

    public int thumbOffset(int trackHeight) {
        return canScroll() ? Math.round((float) firstRow * (trackHeight - thumbHeight(trackHeight)) / maxFirstRow()) : 0;
    }

    public void dragThumb(double offset, int trackHeight) {
        int travel = trackHeight - thumbHeight(trackHeight);
        if (travel > 0) setFirstRow((int) Math.round(offset * maxFirstRow() / travel));
    }
}
