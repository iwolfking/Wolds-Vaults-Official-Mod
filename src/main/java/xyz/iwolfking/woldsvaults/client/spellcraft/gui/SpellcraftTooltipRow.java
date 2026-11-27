package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import java.util.List;

/**
 * A drawn row of an item's spellcraft tooltip block, for the rows that carry runes, icons or the arcana bar. Rows of
 * one block share {@code width} so their right-hand numbers line up.
 */
public record SpellcraftTooltipRow(Kind kind, String text, String right, String extra, List<Rune> runes, List<String> icons, int color,
                                   float used, float budget, int[] width) implements TooltipComponent {

    public enum Kind {
        HEAD,
        RUNES,
        GLYPH,
        SPELL,
        SPELL_ROW
    }

    public record Rune(GlyphType type) {
    }

    private static final int BAR_W = 60;
    private static final int RUNE = 8;
    private static final int RUNE_PITCH = 9;
    private static final int INDENT = 10;

    public int naturalWidth(Font font) {
        return switch (this.kind) {
            case HEAD -> font.width(this.text) + 6 + BAR_W + 6 + font.width(this.right);
            case RUNES -> this.runes.size() * RUNE_PITCH + 6 + font.width(this.text);
            case GLYPH -> INDENT + font.width(this.text + " " + this.extra) + 8 + font.width(this.right) + 10 + font.width(costText());
            case SPELL -> INDENT + font.width(this.text) + 8 + 10 + font.width(costText());
            case SPELL_ROW -> INDENT + (this.runes.size() + this.icons.size()) * RUNE_PITCH + 5 + font.width(this.text);
        };
    }

    private String costText() {
        return this.kind == Kind.GLYPH || this.kind == Kind.SPELL ? String.valueOf(Math.round(this.used)) : "";
    }

    public static final class Client implements ClientTooltipComponent {
        private final SpellcraftTooltipRow row;

        public Client(SpellcraftTooltipRow row) {
            this.row = row;
        }

        @Override
        public int getHeight() {
            return 10;
        }

        @Override
        public int getWidth(Font font) {
            return this.row.naturalWidth(font);
        }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack pose, ItemRenderer itemRenderer, int blitOffset) {
            SpellcraftTooltipRow r = this.row;
            int w = Math.max(r.width()[0], r.naturalWidth(font));
            pose.pushPose();
            pose.translate(0.0, 0.0, blitOffset);
            switch (r.kind()) {
                case HEAD -> {
                    font.drawShadow(pose, r.text(), x, y, r.color());
                    int numX = x + w - font.width(r.right()) - 1;
                    font.drawShadow(pose, r.right(), numX, y, r.color());
                    int bx = x + font.width(r.text()) + 6;
                    SpellcraftDraw.arcanaBar(pose, bx, y, numX - 5 - bx, 7, r.used(), 0.0F, r.budget());
                }
                case RUNES -> {
                    for (int k = 0; k < r.runes().size(); k++) {
                        SpellcraftDraw.rune(pose, r.runes().get(k).type(), x + k * RUNE_PITCH, y - 1, RUNE, false);
                    }
                    font.drawShadow(pose, r.text(), x + r.runes().size() * RUNE_PITCH + 6, y, SpellcraftPalette.GREY);
                }
                case GLYPH -> {
                    GlyphType type = r.runes().get(0).type();
                    SpellcraftDraw.rune(pose, type, x, y - 1, RUNE, false);
                    int tx = x + INDENT;
                    font.drawShadow(pose, r.text() + " ", tx, y, r.color());
                    font.drawShadow(pose, r.extra(), tx + font.width(r.text() + " "), y, SpellcraftPalette.GREY);
                    String cost = r.costText();
                    int cx = x + w - font.width(cost) - 1;
                    font.drawShadow(pose, cost, cx, y, SpellcraftPalette.ARCANA);
                    GuiComponent.fill(pose, cx - 6, y + 2, cx - 2, y + 6, SpellcraftPalette.ARCANA);
                    font.drawShadow(pose, r.right(), cx - 9 - font.width(r.right()), y, SpellcraftPalette.WHITE);
                }
                case SPELL -> {
                    if (!r.icons().isEmpty()) {
                        SpellcraftDraw.ability(pose, r.icons().get(0), x, y - 1, RUNE, 1.0F);
                    }
                    font.drawShadow(pose, r.text(), x + INDENT, y, r.color());
                    String cost = r.costText();
                    int cx = x + w - font.width(cost) - 1;
                    font.drawShadow(pose, cost, cx, y, SpellcraftPalette.ARCANA);
                    GuiComponent.fill(pose, cx - 6, y + 2, cx - 2, y + 6, SpellcraftPalette.ARCANA);
                }
                case SPELL_ROW -> {
                    int tx = x + INDENT;
                    for (Rune rune : r.runes()) {
                        SpellcraftDraw.rune(pose, rune.type(), tx, y - 1, RUNE, false);
                        tx += RUNE_PITCH;
                    }
                    for (String icon : r.icons()) {
                        SpellcraftDraw.ability(pose, icon, tx, y - 1, RUNE, 1.0F);
                        tx += RUNE_PITCH;
                    }
                    font.drawShadow(pose, r.text(), tx + 5, y, SpellcraftPalette.GREY);
                }
            }
            pose.popPose();
        }
    }
}
