package xyz.iwolfking.woldsvaults.client.spellcraft.editor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftDraw;
import xyz.iwolfking.woldsvaults.client.spellcraft.gui.SpellcraftPalette;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rows of the stats box: each {@link #ROW_H} tall with its text centered between the lines around it, a light line
 * between rows and a dark line with {@link #SECTION_GAP} more space on each side between sections.
 */
final class StatColumn {
    static final int ROW_H = 12;
    static final int CAP_Y = 2;
    static final int SECTION_GAP = 2;
    static final int LIGHT_LINE = 0xFF7A7A7A;
    static final int DARK_LINE = 0xFF555555;
    private static final int HEADER_TEXT = 0xFF3B2A00;
    private static final int TIER_TEXT = 0xFF5A5A5A;

    enum Kind {
        ROW,
        HEADER,
        BREAK
    }

    /**
     * A row: label left, value right in {@code color}. {@code base} is the value without glyphs, null when glyphs
     * cannot change it. A header names an ability, with its tier as the value.
     */
    record Line(Kind kind, String label, String value, int color, @Nullable String base) {
        static final Line BREAK = new Line(Kind.BREAK, "", "", 0, null);

        static Line row(String label, String value, int color, @Nullable String base) {
            return new Line(Kind.ROW, label, value, color, base);
        }

        static Line header(String name, String tier) {
            return new Line(Kind.HEADER, name, tier, TIER_TEXT, null);
        }

        boolean changed() {
            return this.base != null && !this.base.equals(this.value);
        }
    }

    /** A drawn line with the top of its row and whether its label was cut to fit. */
    record Placed(Line line, int y, boolean cut) {
    }

    private StatColumn() {
    }

    /** The height the lines need with nothing left out. */
    static int height(List<Line> lines) {
        int height = 0;
        Line previous = null;
        for (Line line : lines) {
            if (line.kind() == Kind.BREAK) {
                if (previous != null && previous.kind() != Kind.BREAK) {
                    height += 2 * SECTION_GAP;
                }
            } else {
                height += ROW_H;
            }
            previous = line;
        }
        return height;
    }

    /** Draws the lines from {@code top} and stops before a row that would cross {@code bottom}. */
    static List<Placed> draw(PoseStack pose, Font font, List<Line> lines, int x0, int x1, int top, int bottom) {
        List<Placed> placed = new ArrayList<>();
        int y = top;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (line.kind() == Kind.BREAK) {
                continue;
            }
            if (y + ROW_H > bottom) {
                break;
            }
            placed.add(new Placed(line, y, row(pose, font, line, x0, x1, y + CAP_Y)));
            Line next = i + 1 < lines.size() ? lines.get(i + 1) : null;
            if (next == null) {
                y += ROW_H;
            } else if (next.kind() == Kind.BREAK) {
                GuiComponent.fill(pose, x0 - 2, y + ROW_H - 1 + SECTION_GAP, x1 + 2, y + ROW_H + SECTION_GAP, DARK_LINE);
                y += ROW_H + 2 * SECTION_GAP;
            } else {
                GuiComponent.fill(pose, x0 - 2, y + ROW_H - 1, x1 + 2, y + ROW_H, LIGHT_LINE);
                y += ROW_H;
            }
        }
        return placed;
    }

    /** Draws one line's text with its glyph tops at {@code y}; returns whether the label was cut. */
    static boolean row(PoseStack pose, Font font, Line line, int x0, int x1, int y) {
        if (line.kind() == Kind.HEADER) {
            String name = font.plainSubstrByWidth(line.label(), x1 - x0);
            font.draw(pose, name, x0, y, HEADER_TEXT);
            font.draw(pose, line.value(), x0 + font.width(name) + 3, y, line.color());
            return !name.equals(line.label());
        }
        int vw = font.width(line.value());
        font.draw(pose, line.value(), x1 - vw, y, line.color());
        String label = font.plainSubstrByWidth(line.label(), Math.max(0, x1 - x0 - vw - 4));
        font.draw(pose, label, x0, y, SpellcraftPalette.LABEL);
        return !label.equals(line.label());
    }

    /** The drawn line under the mouse, if any. */
    @Nullable
    static Placed at(List<Placed> placed, int x0, int x1, double mouseX, double mouseY) {
        for (Placed row : placed) {
            if (SpellcraftDraw.inside(mouseX, mouseY, x0 - 2, row.y(), x1 - x0 + 4, ROW_H)) {
                return row;
            }
        }
        return null;
    }
}
