package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.util.ClientScheduler;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.gods.tree.GodTreeDefinition;

import javax.annotation.Nonnull;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * One star of a god's constellation: a frame from the god's star sheet (wide four-point for
 * stats, eight-point for minors, an ornate burst for majors, a ringed spark for starts), the
 * node's icon in the middle, and a slow shimmer once the star is lit. Hit-testing is radial
 * like the old greed tree's circle nodes.
 */
public class GodNodeWidget extends AbstractWidget {
    private final GodTreeDefinition.Node node;
    private final VaultGod god;
    private final boolean unlocked;
    private final boolean available;
    private final int nodeSize;
    private final int iconSize;
    private final int twinklePhase;
    private boolean selected;

    public GodNodeWidget(GodTreeDefinition.Node node, VaultGod god, boolean unlocked, boolean available) {
        super(node.x() - sizeFor(node) / 2, node.y() - sizeFor(node) / 2,
                sizeFor(node), sizeFor(node), new TextComponent(node.name()));
        this.node = node;
        this.god = god;
        this.unlocked = unlocked;
        this.available = available;
        this.nodeSize = sizeFor(node);
        this.iconSize = iconSizeFor(node);
        this.twinklePhase = node.id().hashCode() & 0xFF;
        this.selected = false;
    }

    private static int sizeFor(GodTreeDefinition.Node node) {
        return switch (node.type()) {
            case MAJOR -> 44;
            case MINOR -> 30;
            case ROOT -> 26;
            case STAT -> 22;
        };
    }

    private static int iconSizeFor(GodTreeDefinition.Node node) {
        return switch (node.type()) {
            case MAJOR -> 16;
            case MINOR -> 14;
            case ROOT -> 0;
            case STAT -> 12;
        };
    }

    private int frameColumn() {
        return switch (this.node.type()) {
            case STAT -> GodTreeTheme.STAR_COLUMN_STAT;
            case MINOR -> GodTreeTheme.STAR_COLUMN_MINOR;
            case MAJOR -> GodTreeTheme.STAR_COLUMN_MAJOR;
            case ROOT -> GodTreeTheme.STAR_COLUMN_ROOT;
        };
    }

    public GodTreeDefinition.Node getNode() {
        return this.node;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void select() {
        this.selected = true;
    }

    public void deselect() {
        this.selected = false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        double cx = this.x + this.width / 2.0D;
        double cy = this.y + this.height / 2.0D;
        double radius = this.width / 2.0D;
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void renderWidget(PoseStack renderStack, Rectangle containerBounds, int screenMouseX, int screenMouseY,
                             int containerMouseX, int containerMouseY, float pTicks, List<Runnable> postContainerRender) {
        boolean hovered = this.isMouseOver(containerMouseX, containerMouseY);
        this.renderFrame(renderStack, hovered, pTicks);
        this.renderIcon(renderStack);
        if (hovered) {
            this.renderTooltip(renderStack, screenMouseX, screenMouseY, postContainerRender);
        }
    }

    private void renderFrame(PoseStack renderStack, boolean hovered, float pTicks) {
        int row;
        if (this.selected || hovered) {
            row = GodTreeTheme.STAR_ROW_SELECTED;
        } else if (this.unlocked) {
            row = GodTreeTheme.STAR_ROW_UNLOCKED;
        } else if (this.available) {
            row = GodTreeTheme.STAR_ROW_AVAILABLE;
        } else {
            row = GodTreeTheme.STAR_ROW_LOCKED;
        }
        this.blitFrame(renderStack, row, 1.0F);
        if (this.unlocked && !this.selected && !hovered) {
            float tick = (float) ClientScheduler.INSTANCE.getTick() + pTicks;
            float shimmer = 0.18F + 0.16F * (float) Math.sin((tick + this.twinklePhase * 3) / 14.0D);
            this.blitFrame(renderStack, GodTreeTheme.STAR_ROW_SELECTED, Math.max(0.0F, shimmer));
        }
    }

    private void blitFrame(PoseStack renderStack, int row, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.setShaderTexture(0, GodTreeTheme.starSheet(this.god));
        RenderSystem.enableBlend();
        GuiComponent.blit(renderStack, this.x, this.y, this.nodeSize, this.nodeSize,
                (float) (this.frameColumn() * GodTreeTheme.STAR_CELL), (float) (row * GodTreeTheme.STAR_CELL),
                GodTreeTheme.STAR_CELL, GodTreeTheme.STAR_CELL,
                GodTreeTheme.STAR_SHEET_SIZE, GodTreeTheme.STAR_SHEET_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderIcon(PoseStack renderStack) {
        ResourceLocation icon = this.node.icon();
        if (icon == null || this.iconSize <= 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.unlocked || this.available ? 1.0F : 0.4F);
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.enableBlend();
        int iconX = this.x + (this.nodeSize - this.iconSize) / 2;
        int iconY = this.y + (this.nodeSize - this.iconSize) / 2;
        GuiComponent.blit(renderStack, iconX, iconY, this.iconSize, this.iconSize, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTooltip(PoseStack renderStack, int screenMouseX, int screenMouseY, List<Runnable> postContainerRender) {
        List<Component> tooltip = new ArrayList<>();
        MutableComponent name = new TextComponent(this.node.name())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(this.god) & 0xFFFFFF)));
        tooltip.add(name);
        if (!this.node.enabled()) {
            tooltip.add(new TextComponent("Coming soon").withStyle(ChatFormatting.GRAY));
        } else if (this.unlocked) {
            tooltip.add(new TextComponent("Unlocked").withStyle(ChatFormatting.GREEN));
        } else if (this.available) {
            tooltip.add(new TextComponent("Click to unlock").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(new TextComponent("Locked - reach it through a connected star").withStyle(ChatFormatting.RED));
        }
        postContainerRender.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null) {
                minecraft.screen.renderComponentTooltip(renderStack, tooltip, screenMouseX, screenMouseY);
            }
        });
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void updateNarration(@Nonnull NarrationElementOutput narrationElementOutput) {
    }
}
