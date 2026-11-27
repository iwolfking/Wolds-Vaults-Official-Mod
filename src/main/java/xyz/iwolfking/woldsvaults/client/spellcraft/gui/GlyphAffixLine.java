package xyz.iwolfking.woldsvaults.client.spellcraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

/** A gear affix line that grants a glyph, drawn as the affix's own text with the glyph's rune to its right. */
public record GlyphAffixLine(Component text, GlyphType type) implements TooltipComponent {

    private static final int RUNE = 8;
    private static final int GAP = 3;

    public static final class Client implements ClientTooltipComponent {
        private final GlyphAffixLine line;

        public Client(GlyphAffixLine line) {
            this.line = line;
        }

        @Override
        public int getHeight() {
            return 10;
        }

        @Override
        public int getWidth(Font font) {
            return font.width(this.line.text()) + GAP + RUNE;
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
            font.drawInBatch(this.line.text().getVisualOrderText(), x, y, -1, true, matrix, buffer, false, 0, 0xF000F0);
        }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack pose, ItemRenderer itemRenderer, int blitOffset) {
            pose.pushPose();
            pose.translate(0.0, 0.0, blitOffset);
            SpellcraftDraw.rune(pose, this.line.type(), x + font.width(this.line.text()) + GAP, y - 1, RUNE, false);
            pose.popPose();
        }
    }
}
