package xyz.iwolfking.woldsvaults.client.screens.stats;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.gui.framework.element.spi.IRenderedElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

/** A category header in the Statistics tab's player list: dark label, then an etched rule to the right edge. */
public class StatCategoryHeaderElement extends AbstractSpatialElement<StatCategoryHeaderElement> implements IRenderedElement {
    public static final int HEIGHT = 12;
    private static final int TEXT_COLOR = 0xFF3F3F3F;
    private static final int RULE_DARK = 0xFF8B8B8B;
    private static final int RULE_LIGHT = 0xFFFFFFFF;
    private static final int TEXT_Y = 2;
    private static final int RULE_GAP = 4;

    private final String name;
    private boolean visible = true;

    public StatCategoryHeaderElement(ISpatial spatial, String name) {
        super(spatial);
        this.name = name;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ISpatial world = this.getWorldSpatial();
        Font font = Minecraft.getInstance().font;
        int y = world.y() + TEXT_Y;
        renderer.renderPlain(new TextComponent(this.name).getVisualOrderText(), font, poseStack, world.x(), y, world.z(), TEXT_COLOR);
        int ruleX = world.x() + font.width(this.name) + RULE_GAP;
        int ruleWidth = world.x() + world.width() - ruleX;
        if (ruleWidth > 0) {
            renderer.renderColoredQuad(poseStack, RULE_DARK, ruleX, y + 3, world.z(), ruleWidth, 1);
            renderer.renderColoredQuad(poseStack, RULE_LIGHT, ruleX, y + 4, world.z(), ruleWidth, 1);
        }
    }
}
