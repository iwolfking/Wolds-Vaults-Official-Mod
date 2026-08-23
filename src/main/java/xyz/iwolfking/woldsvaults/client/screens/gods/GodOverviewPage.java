package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
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
 * The God's Mastery page of the gods tab: one panel per god with its minor-transfer slots beneath.
 * Reads the client alignment mirror; the only message it sends is a slot clear.
 */
public final class GodOverviewPage {
    private static final int PANEL_HEIGHT = 48;
    private static final int GAP = 6;
    private static final int NUMERAL_GUTTER = 14;
    private static final int CAPTION_HEIGHT = 10;
    private static final int CAPTION_GAP = 4;
    private static final int ROW_PITCH_MIN = 26;
    private static final int ROW_PITCH_MAX = 44;
    private static final String CAPTION = "Transfer slots - right-click a learned minor star to assign it";

    private final GodTreeScreen screen;

    public GodOverviewPage(GodTreeScreen screen) {
        this.screen = screen;
    }

    /** Where everything sits inside a frame; recomputed per call. */
    private record Layout(Rectangle inner, int panelWidth, int rowPitch, int rowsTop, int rows) {
        Rectangle panel(int column) {
            return new Rectangle(this.inner.x + NUMERAL_GUTTER + column * (this.panelWidth + GAP), this.inner.y,
                    this.panelWidth, PANEL_HEIGHT);
        }

        float cellX(int column) {
            return this.panel(column).x + this.panelWidth / 2.0F;
        }

        float cellY(int row) {
            return this.rowsTop + row * this.rowPitch + this.rowPitch / 2.0F;
        }

        float cellRadius() {
            return (this.rowPitch - 4) / 2.0F;
        }

        int captionY() {
            return this.inner.y + PANEL_HEIGHT + GAP;
        }
    }

    private static Layout layout(Rectangle frame) {
        Rectangle inner = new Rectangle(frame.x + 30, frame.y + 8, frame.width - 36, frame.height - 14);
        int panelWidth = (inner.width - NUMERAL_GUTTER - 3 * GAP) / 4;
        int rows = Math.max(1, GodLevels.maxMinorTransferSlots());
        int available = inner.height - PANEL_HEIGHT - GAP - CAPTION_HEIGHT - CAPTION_GAP;
        int rowPitch = Math.max(ROW_PITCH_MIN, Math.min(ROW_PITCH_MAX, available / rows));
        int rowsTop = inner.y + PANEL_HEIGHT + GAP + CAPTION_HEIGHT + CAPTION_GAP;
        return new Layout(inner, panelWidth, rowPitch, rowsTop, rows);
    }

    public void render(PoseStack poseStack, Rectangle frame, int mouseX, int mouseY, List<Runnable> postRender) {
        Font font = Minecraft.getInstance().font;
        Layout layout = layout(frame);
        VaultGod[] order = GodTreeTheme.DISPLAY_ORDER;
        for (int column = 0; column < order.length; column++) {
            this.renderPanel(poseStack, font, layout, column, order[column], mouseX, mouseY, postRender);
        }
        font.draw(poseStack, CAPTION, layout.inner.x + NUMERAL_GUTTER, layout.captionY(), GodTreeTheme.TEXT_DIM);
        for (int row = 0; row < layout.rows; row++) {
            font.draw(poseStack, GreedTheme.roman(row + 1), layout.inner.x + 2, layout.cellY(row) - 4, GodTreeTheme.TEXT_DIM);
        }
        for (int column = 0; column < order.length; column++) {
            for (int row = 0; row < layout.rows; row++) {
                this.renderCell(poseStack, layout, column, row, order[column], mouseX, mouseY, postRender);
            }
        }
    }

    private void renderPanel(PoseStack poseStack, Font font, Layout layout, int column, VaultGod god, int mouseX, int mouseY,
                             List<Runnable> postRender) {
        Rectangle panel = layout.panel(column);
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = minecraft.player != null && ActiveGodResolver.isActive(minecraft.player, god);
        boolean hovered = panel.contains(mouseX, mouseY);
        int accent = GodTreeTheme.accent(god);
        if (active) {
            GodDrawing.outline(poseStack, panel.x - 2, panel.y - 2, panel.width + 4, panel.height + 4, GodTreeTheme.accentDim(god));
            GodDrawing.outline(poseStack, panel.x - 1, panel.y - 1, panel.width + 2, panel.height + 2, accent);
            GuiComponent.fill(poseStack, panel.x, panel.y, panel.x + panel.width, panel.y + panel.height,
                    (GodTreeTheme.accentDeep(god) & 0xFFFFFF) | 0x99000000);
        } else {
            GodDrawing.outline(poseStack, panel.x - 1, panel.y - 1, panel.width + 2, panel.height + 2,
                    hovered ? GodTreeTheme.PLATE_BORDER_HOVER : GodTreeTheme.PLATE_BORDER);
            GuiComponent.fill(poseStack, panel.x, panel.y, panel.x + panel.width, panel.y + panel.height, GodTreeTheme.PLATE_FILL);
        }
        GodTreeTheme.godIcon(god).blit(poseStack, panel.x + 4, panel.y + 4, 0, 16, 16);
        font.drawShadow(poseStack, god.getName(), panel.x + 24, panel.y + 5, accent);
        int level = ClientGodAlignmentData.getLevel(god);
        font.drawShadow(poseStack, "Level " + level, panel.x + 24, panel.y + 15, GodTreeTheme.TEXT_MUTED);

        boolean[] tooltipShown = {false};
        Rectangle bar = new Rectangle(panel.x + 4, panel.y + 27, panel.width - 8, GodXpBarRenderer.BAR_HEIGHT);
        GodXpBarRenderer.render(poseStack, font, god, bar, false, mouseX, mouseY, lines -> {
            tooltipShown[0] = true;
            deferTooltip(postRender, poseStack, lines, mouseX, mouseY);
        });

        int rowY = panel.y + 36;
        String label = "Sacrifice";
        font.drawShadow(poseStack, label, panel.x + 4, rowY, GodTreeTheme.TEXT_MUTED);
        int glyphX = panel.x + 4 + font.width(label) + 4;
        if (GodXpBarRenderer.sacrificePerformed(god)) {
            GodDrawing.check(poseStack, glyphX, rowY + 1, 7.0F, GodTreeTheme.STATUS_GOOD);
        } else {
            GodDrawing.cross(poseStack, glyphX, rowY + 1, 7.0F, GodTreeTheme.STATUS_BAD);
        }
        Rectangle sacrificeRow = new Rectangle(panel.x + 4, rowY - 1, font.width(label) + 16, 10);
        if (!tooltipShown[0] && sacrificeRow.contains(mouseX, mouseY)) {
            tooltipShown[0] = true;
            deferTooltip(postRender, poseStack, List.of(GodXpBarRenderer.sacrificeLine(god)), mouseX, mouseY);
        }
        if (!tooltipShown[0] && hovered) {
            deferTooltip(postRender, poseStack, panelTooltip(god, level, active), mouseX, mouseY);
        }
    }

    private static List<Component> panelTooltip(VaultGod god, int level, boolean active) {
        List<Component> lines = new ArrayList<>();
        int accent = GodTreeTheme.accent(god) & 0xFFFFFF;
        lines.add(new TextComponent(god.getName() + " - Level " + level).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
        int unspent = ClientGodAlignmentData.getUnspentPoints(god);
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

    private void renderCell(PoseStack poseStack, Layout layout, int column, int row, VaultGod god, int mouseX, int mouseY,
                            List<Runnable> postRender) {
        float cx = layout.cellX(column);
        float cy = layout.cellY(row);
        float radius = layout.cellRadius();
        boolean locked = row >= ClientGodAlignmentData.getMinorTransferSlots(god);
        boolean live = !locked && ClientGodAlignmentData.isMinorTransferLive(god, row);
        Minecraft minecraft = Minecraft.getInstance();
        boolean dormant = minecraft.player != null && ActiveGodResolver.isActive(minecraft.player, god);
        String slotLabel = "Slot " + GreedTheme.roman(row + 1);
        List<Component> tooltip = new ArrayList<>();
        if (locked) {
            GodDrawing.disc(poseStack, cx, cy, radius - 1.0F, GodTreeTheme.SLOT_LOCKED_FILL);
            GodDrawing.ring(poseStack, cx, cy, radius, radius - 2.0F, GodTreeTheme.SLOT_LOCKED_RING);
            float half = radius * 0.55F;
            GodDrawing.cross(poseStack, cx - half, cy - half, half * 2.0F, GodTreeTheme.SLOT_LOCKED_CROSS);
            tooltip.add(new TextComponent(slotLabel + " - locked").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TextComponent("Unlocks at " + god.getName() + " level "
                    + GodLevels.minorTransferSlotUnlockLevel(row)).withStyle(ChatFormatting.GRAY));
        } else if (!live) {
            GodDrawing.disc(poseStack, cx, cy, radius - 1.0F, GodTreeTheme.SLOT_EMPTY_FILL);
            GodDrawing.ring(poseStack, cx, cy, radius, radius - 2.0F, GodTreeTheme.SLOT_EMPTY_RING);
            tooltip.add(new TextComponent(slotLabel + " - empty")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.TEXT_MUTED & 0xFFFFFF))));
            tooltip.add(new TextComponent("Right-click a learned minor star in " + god.getName()
                    + "'s constellation to assign it").withStyle(ChatFormatting.GRAY));
        } else {
            String content = ClientGodAlignmentData.getMinorTransferSlot(god, row).orElse("");
            GodTreeModel tree = GodNodeRegistry.tree(god).orElse(null);
            GodNode placement = tree == null ? null : tree.placementOf(content);
            int size = Math.round(radius * 2.0F + 4.0F);
            ResourceLocation sheet = dormant ? GodTreeTheme.starSheetNeutral() : GodTreeTheme.starSheet(god);
            GodDrawing.starFrame(poseStack, sheet, GodTreeTheme.STAR_COLUMN_MINOR, GodTreeTheme.STAR_ROW_UNLOCKED,
                    Math.round(cx - size / 2.0F), Math.round(cy - size / 2.0F), size, 1.0F);
            ResourceLocation icon = iconOf(tree, placement);
            if (icon != null) {
                int iconSize = Math.max(8, Math.round(radius));
                GodDrawing.texture(poseStack, icon, Math.round(cx - iconSize / 2.0F), Math.round(cy - iconSize / 2.0F),
                        iconSize, dormant ? 0.55F : 1.0F);
            }
            String name = placement == null ? content : placement.name();
            tooltip.add(new TextComponent(name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(GodTreeTheme.accent(god) & 0xFFFFFF))));
            if (dormant) {
                tooltip.add(new TextComponent("Dormant - " + god.getName()
                        + " is your active god, so this star already applies from the constellation").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(new TextComponent("Transferring - applies while " + god.getName()
                        + " is not your active god").withStyle(ChatFormatting.GREEN));
            }
            tooltip.add(new TextComponent("Right-click to clear").withStyle(ChatFormatting.GRAY));
        }
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        if (dx * dx + dy * dy <= radius * radius) {
            deferTooltip(postRender, poseStack, tooltip, mouseX, mouseY);
        }
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

    /** Left-click on a god panel opens its constellation; right-click on a filled slot clears it. */
    public boolean mouseClicked(Rectangle frame, double mouseX, double mouseY, int button) {
        Layout layout = layout(frame);
        VaultGod[] order = GodTreeTheme.DISPLAY_ORDER;
        for (int column = 0; column < order.length; column++) {
            VaultGod god = order[column];
            if (button == 0 && layout.panel(column).contains(mouseX, mouseY)) {
                this.screen.selectTab(GodScreenTab.of(god));
                return true;
            }
            if (button != 1) {
                continue;
            }
            for (int row = 0; row < layout.rows; row++) {
                double dx = mouseX - layout.cellX(column);
                double dy = mouseY - layout.cellY(row);
                float radius = layout.cellRadius();
                if (dx * dx + dy * dy > radius * radius || !ClientGodAlignmentData.isMinorTransferLive(god, row)) {
                    continue;
                }
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.player.playSound(ModSounds.SKILL_TREE_LEARN_SFX, 1.0F, 1.0F);
                }
                NetworkHandler.INSTANCE.sendToServer(new ServerboundSetMinorTransferMessage(god, row, ""));
                return true;
            }
        }
        return false;
    }
}
