package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundSetMinorTransferMessage;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * The God's Mastery page of the gods tab: one column per god with its name, its idol (coloured by god level, the
 * transfer slots set into it inside the animated cloud of {@link GodIdolCloud}), the level bar, the level and the
 * sacrifice standing. Reads the client alignment mirror; the only message it sends is a slot clear.
 */
public final class GodOverviewPage {
    private static final int GAP = 6;
    private static final int PAD = 6;
    private static final int NAME_HEIGHT = 9;
    private static final int LINE_GAP = 4;
    private static final int FOOTER_HEIGHT = 9;
    private static final int CAPTION_HEIGHT = 10;
    private static final int CAPTION_GAP = 6;
    private static final int IDOL_STEP = 16;
    private static final int IDOL_MIN = 64;
    private static final int IDOL_MAX = 160;
    private static final float SLOT_REFERENCE_IDOL = 160.0F;
    private static final int GLYPH_SIZE = 7;
    private static final String CAPTION = "Transfer slots - right-click a learned minor star to assign it";

    private final GodTreeScreen screen;
    private final GodTreeView view;

    public GodOverviewPage(GodTreeScreen screen, GodTreeView view) {
        this.screen = screen;
        this.view = view;
    }

    /** Where everything sits inside a frame; recomputed per call. */
    private record Layout(Rectangle inner, int panelWidth, int panelHeight, int top, int idol) {
        Rectangle panel(int column) {
            return new Rectangle(this.inner.x + column * (this.panelWidth + GAP), this.top, this.panelWidth, this.panelHeight);
        }

        int idolX(int column) {
            return this.panel(column).x + (this.panelWidth - this.idol) / 2;
        }

        int idolY() {
            return this.top + PAD + NAME_HEIGHT + LINE_GAP;
        }

        Rectangle bar(int column) {
            Rectangle panel = this.panel(column);
            return new Rectangle(panel.x + PAD + 2, this.idolY() + this.idol + LINE_GAP, panel.width - 2 * (PAD + 2),
                    GodXpBarRenderer.BAR_HEIGHT);
        }

        int footerY() {
            return this.idolY() + this.idol + LINE_GAP + GodXpBarRenderer.BAR_HEIGHT + LINE_GAP;
        }

        int captionY() {
            return this.top + this.panelHeight + CAPTION_GAP;
        }

        float slotScale() {
            return this.idol / SLOT_REFERENCE_IDOL;
        }

        float slotX(int column, GodIdolCloud.Opening opening) {
            return this.idolX(column) + opening.x() / GodIdolCloud.BASE * this.idol;
        }

        float slotY(GodIdolCloud.Opening opening) {
            return this.idolY() + opening.y() / GodIdolCloud.BASE * this.idol;
        }

        float slotRadius() {
            return GodTreeTheme.SLOT_RING_RADIUS * this.slotScale();
        }
    }

    private static Layout layout(Rectangle frame) {
        Rectangle inner = new Rectangle(frame.x + 30, frame.y + 8, frame.width - 36, frame.height - 14);
        int panelWidth = (inner.width - 3 * GAP) / 4;
        int chrome = PAD + NAME_HEIGHT + LINE_GAP + LINE_GAP + GodXpBarRenderer.BAR_HEIGHT + LINE_GAP + FOOTER_HEIGHT + PAD;
        int fit = Math.min(panelWidth - 2 * PAD, inner.height - CAPTION_GAP - CAPTION_HEIGHT - chrome);
        int idol = Mth.clamp(fit / IDOL_STEP * IDOL_STEP, IDOL_MIN, IDOL_MAX);
        int panelHeight = chrome + idol;
        int top = inner.y + Math.max(0, (inner.height - panelHeight - CAPTION_GAP - CAPTION_HEIGHT) / 2);
        return new Layout(inner, panelWidth, panelHeight, top, idol);
    }

    private static int slotCount() {
        int openings = GodIdolCloud.openings().size();
        int granted = GodLevels.maxMinorTransferSlots();
        if (granted > openings) {
            GodTreeLog.warnOnce("transfer_slots", "god_levels grants {} transfer slots but the idol cloud has {} openings; the page shows {}.", granted, openings, openings);
        }
        return Math.min(openings, granted);
    }

    public void render(PoseStack poseStack, Rectangle frame, int mouseX, int mouseY, List<Runnable> postRender) {
        Font font = Minecraft.getInstance().font;
        Layout layout = layout(frame);
        VaultGod[] order = GodTreeTheme.DISPLAY_ORDER;
        for (int column = 0; column < order.length; column++) {
            this.renderColumn(poseStack, font, layout, column, order[column], mouseX, mouseY, postRender);
        }
        font.draw(poseStack, CAPTION, layout.inner.x + (layout.inner.width - font.width(CAPTION)) / 2.0F, layout.captionY(),
                GodTreeTheme.TEXT_DIM);
    }

    private void renderColumn(PoseStack poseStack, Font font, Layout layout, int column, VaultGod god, int mouseX, int mouseY,
                              List<Runnable> postRender) {
        Rectangle panel = layout.panel(column);
        boolean active = this.view.isCharmActive(god);
        boolean hovered = panel.contains(mouseX, mouseY);
        int accent = GodTreeTheme.masteryAccent(god);
        if (active) {
            GodDrawing.outline(poseStack, panel.x - 2, panel.y - 2, panel.width + 4, panel.height + 4, GodTreeTheme.masteryAccentDim(god));
            GodDrawing.outline(poseStack, panel.x - 1, panel.y - 1, panel.width + 2, panel.height + 2, accent);
            GuiComponent.fill(poseStack, panel.x, panel.y, panel.x + panel.width, panel.y + panel.height,
                    (GodTreeTheme.masteryAccentDeep(god) & 0xFFFFFF) | 0x99000000);
        } else {
            GodDrawing.outline(poseStack, panel.x - 1, panel.y - 1, panel.width + 2, panel.height + 2,
                    hovered ? GodTreeTheme.PLATE_BORDER_HOVER : GodTreeTheme.PLATE_BORDER);
            GuiComponent.fill(poseStack, panel.x, panel.y, panel.x + panel.width, panel.y + panel.height, GodTreeTheme.PLATE_FILL);
        }
        String name = god.getName();
        font.drawShadow(poseStack, name, panel.x + (panel.width - font.width(name)) / 2.0F, panel.y + PAD, accent);

        int level = this.view.getLevel(god);
        int idolX = layout.idolX(column);
        int idolY = layout.idolY();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GodTreeTheme.idolSheet(god));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int cell = Math.min(level, GodTreeTheme.IDOL_LEVELS - 1);
        GuiComponent.blit(poseStack, idolX, idolY, layout.idol, layout.idol,
                0.0F, (float) (cell * GodTreeTheme.IDOL_CELL), GodTreeTheme.IDOL_CELL, GodTreeTheme.IDOL_CELL,
                GodTreeTheme.IDOL_CELL, GodTreeTheme.IDOL_CELL * GodTreeTheme.IDOL_LEVELS);
        GodIdolCloud.render(poseStack, god, column, idolX, idolY, layout.idol, GodIdolCloud.levelFade(level));

        boolean tooltipShown = false;
        List<GodIdolCloud.Opening> openings = GodIdolCloud.openings();
        for (int slot = 0; slot < slotCount(); slot++) {
            tooltipShown |= this.renderSlot(poseStack, layout, column, slot, openings.get(slot), god, active, !tooltipShown,
                    mouseX, mouseY, postRender);
        }

        boolean[] barTooltip = {false};
        Rectangle bar = layout.bar(column);
        GodXpBarRenderer.render(this.view, poseStack, font, god, bar, false, mouseX, mouseY, lines -> {
            barTooltip[0] = true;
            deferTooltip(postRender, poseStack, lines, mouseX, mouseY);
        });
        tooltipShown |= barTooltip[0];

        int footerY = layout.footerY();
        font.drawShadow(poseStack, "Level " + level, bar.x, footerY, GodTreeTheme.TEXT_MUTED);
        String label = "Sacrifice";
        int labelX = bar.x + bar.width - (font.width(label) + 3 + GLYPH_SIZE);
        font.drawShadow(poseStack, label, labelX, footerY, GodTreeTheme.TEXT_MUTED);
        int glyphX = labelX + font.width(label) + 3;
        if (GodXpBarRenderer.sacrificePerformed(this.view, god)) {
            GodDrawing.check(poseStack, glyphX, footerY + 1, GLYPH_SIZE, GodTreeTheme.STATUS_GOOD);
        } else {
            GodDrawing.cross(poseStack, glyphX, footerY + 1, GLYPH_SIZE, GodTreeTheme.STATUS_BAD);
        }
        Rectangle sacrificeRow = new Rectangle(labelX, footerY - 1, font.width(label) + 3 + GLYPH_SIZE, 10);
        if (!tooltipShown && sacrificeRow.contains(mouseX, mouseY)) {
            tooltipShown = true;
            deferTooltip(postRender, poseStack, List.of(GodXpBarRenderer.sacrificeLine(this.view, god)), mouseX, mouseY);
        }
        if (!tooltipShown && hovered) {
            deferTooltip(postRender, poseStack, panelTooltip(this.view, god, level, active), mouseX, mouseY);
        }
    }

    private static List<Component> panelTooltip(GodTreeView view, VaultGod god, int level, boolean active) {
        List<Component> lines = new ArrayList<>();
        int accent = GodTreeTheme.masteryAccent(god) & 0xFFFFFF;
        lines.add(new TextComponent(god.getName() + " - Level " + level).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
        int unspent = view.getUnspentPoints(god);
        if (unspent > 0) {
            lines.add(new TextComponent(unspent + " unspent god " + (unspent == 1 ? "point" : "points"))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.pointsColor(god) & 0xFFFFFF))));
        } else {
            lines.add(new TextComponent("No unspent god points").withStyle(ChatFormatting.GRAY));
        }
        if (active) {
            lines.add(new TextComponent("Active - " + god.getName() + " charm equipped").withStyle(ChatFormatting.GREEN));
        } else {
            lines.add(new TextComponent("Not active - no " + god.getName() + " charm equipped").withStyle(ChatFormatting.GRAY));
        }
        lines.add(new TextComponent("Click to open " + god.getName() + "'s constellation").withStyle(ChatFormatting.GRAY));
        return lines;
    }

    private boolean renderSlot(PoseStack poseStack, Layout layout, int column, int slot, GodIdolCloud.Opening opening,
                               VaultGod god, boolean dormant, boolean mayTooltip, int mouseX, int mouseY,
                               List<Runnable> postRender) {
        float cx = layout.slotX(column, opening);
        float cy = layout.slotY(opening);
        boolean locked = slot >= this.view.getMinorTransferSlots(god);
        boolean live = !locked && this.view.isMinorTransferLive(god, slot);
        int state = locked ? GodTreeTheme.SLOT_COLUMN_LOCKED
                : !live ? GodTreeTheme.SLOT_COLUMN_EMPTY
                : dormant ? GodTreeTheme.SLOT_COLUMN_DORMANT : GodTreeTheme.SLOT_COLUMN_ASSIGNED;
        int cell = Math.round(GodTreeTheme.SLOT_CELL * layout.slotScale());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GodTreeTheme.TRANSFER_SLOTS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int sheetSize = GodTreeTheme.SLOT_CELL * 4;
        GuiComponent.blit(poseStack, Math.round(cx - cell / 2.0F), Math.round(cy - cell / 2.0F), cell, cell,
                (float) (state * GodTreeTheme.SLOT_CELL), (float) (column * GodTreeTheme.SLOT_CELL),
                GodTreeTheme.SLOT_CELL, GodTreeTheme.SLOT_CELL, sheetSize, sheetSize);

        String slotLabel = "Slot " + GreedTheme.roman(slot + 1);
        List<Component> tooltip = new ArrayList<>();
        if (locked) {
            tooltip.add(new TextComponent(slotLabel + " - locked").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent("Unlocks at " + god.getName() + " level "
                    + GodLevels.minorTransferSlotUnlockLevel(slot)).withStyle(ChatFormatting.GRAY));
        } else if (!live) {
            tooltip.add(new TextComponent(slotLabel + " - empty")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.TEXT_MUTED & 0xFFFFFF))));
            tooltip.add(new TextComponent("Right-click a learned minor star in " + god.getName()
                    + "'s constellation to assign it").withStyle(ChatFormatting.GRAY));
        } else {
            String content = this.view.getMinorTransferSlot(god, slot).orElse("");
            GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
            GodNode placement = tree == null ? null : tree.placementOf(content);
            ResourceLocation icon = iconOf(tree, placement);
            if (icon != null) {
                int iconSize = Math.round(16 * layout.slotScale());
                GodDrawing.texture(poseStack, icon, Math.round(cx - iconSize / 2.0F), Math.round(cy - iconSize / 2.0F),
                        iconSize, dormant ? 0.55F : 1.0F);
            }
            String name = placement == null ? content : placement.name();
            tooltip.add(new TextComponent(name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.masteryAccent(god) & 0xFFFFFF))));
            if (dormant) {
                tooltip.add(new TextComponent("Dormant - " + god.getName()
                        + " is your active god, so this star already applies from the constellation").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(new TextComponent("Transferring - applies while " + god.getName()
                        + " is not your active god").withStyle(ChatFormatting.GREEN));
            }
            if (this.view.isLive()) {
                tooltip.add(new TextComponent("Right-click to clear").withStyle(ChatFormatting.GRAY));
            }
        }
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        float radius = layout.slotRadius();
        if (mayTooltip && dx * dx + dy * dy <= radius * radius) {
            deferTooltip(postRender, poseStack, tooltip, mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Nullable
    private static ResourceLocation iconOf(@Nullable GodTreeModel tree, @Nullable GodNode placement) {
        if (tree == null || placement == null) {
            return null;
        }
        SkillStyle style = tree.getStyle(placement.id());
        return style == null ? null : style.icon;
    }

    private static void deferTooltip(List<Runnable> postRender, PoseStack poseStack, List<Component> lines, int mouseX, int mouseY) {
        postRender.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen != null) {
                minecraft.screen.renderComponentTooltip(poseStack, lines, mouseX, mouseY);
            }
        });
    }

    public boolean mouseClicked(Rectangle frame, double mouseX, double mouseY, int button) {
        Layout layout = layout(frame);
        VaultGod[] order = GodTreeTheme.DISPLAY_ORDER;
        List<GodIdolCloud.Opening> openings = GodIdolCloud.openings();
        float radius = layout.slotRadius();
        for (int column = 0; column < order.length; column++) {
            VaultGod god = order[column];
            if (button == 0 && layout.panel(column).contains(mouseX, mouseY)) {
                this.screen.selectTab(GodScreenTab.of(god));
                return true;
            }
            if (button != 1 || !this.view.isLive()) {
                continue;
            }
            for (int slot = 0; slot < slotCount(); slot++) {
                double dx = mouseX - layout.slotX(column, openings.get(slot));
                double dy = mouseY - layout.slotY(openings.get(slot));
                if (dx * dx + dy * dy > radius * radius || !this.view.isMinorTransferLive(god, slot)) {
                    continue;
                }
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.player.playSound(ModSounds.SKILL_TREE_LEARN_SFX, 1.0F, 1.0F);
                }
                NetworkHandler.INSTANCE.sendToServer(new ServerboundSetMinorTransferMessage(god, slot, ""));
                return true;
            }
        }
        return false;
    }
}
