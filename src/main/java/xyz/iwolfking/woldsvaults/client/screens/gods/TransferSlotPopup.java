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
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;
import xyz.iwolfking.woldsvaults.gods.network.ServerboundSetMinorTransferMessage;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The slot picker that opens beside a learned minor star on a right-click: one circle per
 * transfer slot the star's god has unlocked, stacked down the star's left side in tree space so
 * the picker pans and zooms with the chart. A click on a circle asks the server to put the star
 * there - or to take it out again when the circle already holds it - and the picker stays open,
 * repainting from the next alignment sync. Opening and closing are the pan region's business.
 */
final class TransferSlotPopup {
    private static final int CIRCLE = 26;
    private static final int PITCH = 32;
    private static final int GAP_TO_STAR = 8;
    private static final int NUMERAL_GUTTER = 16;
    private static final int PAD = 4;

    private final VaultGod god;
    private final GodNode anchor;

    TransferSlotPopup(VaultGod god, GodNode anchor) {
        this.god = god;
        this.anchor = anchor;
    }

    String anchorNodeId() {
        return this.anchor.id();
    }

    private int slotCount() {
        return ClientGodAlignmentData.getMinorTransferSlots(this.god);
    }

    /** Circle centres in tree space for the star's current widget position, top slot first. */
    private List<float[]> circles(GodNodeWidget anchorWidget) {
        int count = this.slotCount();
        List<float[]> centres = new ArrayList<>(count);
        float cx = anchorWidget.x - GAP_TO_STAR - CIRCLE / 2.0F;
        float starCy = anchorWidget.y + anchorWidget.getHeight() / 2.0F;
        float top = starCy - (count - 1) * PITCH / 2.0F;
        for (int i = 0; i < count; i++) {
            centres.add(new float[]{cx, top + i * PITCH});
        }
        return centres;
    }

    /** The slot whose circle is under a tree-space point, or -1. */
    int slotAt(GodNodeWidget anchorWidget, double treeX, double treeY) {
        List<float[]> centres = this.circles(anchorWidget);
        float radius = CIRCLE / 2.0F;
        for (int i = 0; i < centres.size(); i++) {
            double dx = treeX - centres.get(i)[0];
            double dy = treeY - centres.get(i)[1];
            if (dx * dx + dy * dy <= radius * radius) {
                return i;
            }
        }
        return -1;
    }

    void render(PoseStack poseStack, GodNodeWidget anchorWidget, GodTreeModel tree, int treeMouseX, int treeMouseY,
                int screenMouseX, int screenMouseY, List<Runnable> postRender) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int count = this.slotCount();
        if (count == 0) {
            this.renderHint(poseStack, anchorWidget, font);
            return;
        }
        List<float[]> centres = this.circles(anchorWidget);
        float radius = CIRCLE / 2.0F;
        int left = Math.round(centres.get(0)[0] - radius - NUMERAL_GUTTER);
        int right = Math.round(centres.get(0)[0] + radius + PAD);
        int top = Math.round(centres.get(0)[1] - radius - PAD);
        int bottom = Math.round(centres.get(count - 1)[1] + radius + PAD);
        GuiComponent.fill(poseStack, left, top, right, bottom, GodTreeTheme.POPUP_FILL);
        GodDrawing.outline(poseStack, left, top, right - left, bottom - top, GodTreeTheme.accentDim(this.god));
        boolean dormant = minecraft.player != null && ActiveGodResolver.isActive(minecraft.player, this.god);
        Map<String, Integer> ledger = ClientGodAlignmentData.getSpentLedger(this.god);
        String anchorEffect = this.anchor.ledgerKey();
        for (int i = 0; i < count; i++) {
            float cx = centres.get(i)[0];
            float cy = centres.get(i)[1];
            String content = ClientGodAlignmentData.getMinorTransferSlot(this.god, i).orElse(null);
            boolean live = content != null && MinorTransferSlots.isTransferable(this.god, content, ledger);
            boolean holdsAnchor = live && content.equals(anchorEffect);
            font.draw(poseStack, GreedTheme.roman(i + 1), left + 3, cy - 4, GodTreeTheme.TEXT_DIM);
            if (!live) {
                GodDrawing.disc(poseStack, cx, cy, radius - 1.0F, GodTreeTheme.SLOT_EMPTY_FILL);
                GodDrawing.ring(poseStack, cx, cy, radius, radius - 2.0F, GodTreeTheme.SLOT_EMPTY_RING);
            } else {
                ResourceLocation sheet = dormant ? GodTreeTheme.starSheetNeutral() : GodTreeTheme.starSheet(this.god);
                int row = holdsAnchor ? GodTreeTheme.STAR_ROW_SELECTED : GodTreeTheme.STAR_ROW_UNLOCKED;
                int size = CIRCLE + 4;
                GodDrawing.starFrame(poseStack, sheet, GodTreeTheme.STAR_COLUMN_MINOR, row,
                        Math.round(cx - size / 2.0F), Math.round(cy - size / 2.0F), size, 1.0F);
                ResourceLocation icon = iconOf(tree, content);
                if (icon != null) {
                    GodDrawing.texture(poseStack, icon, Math.round(cx - 6), Math.round(cy - 6), 12, dormant ? 0.55F : 1.0F);
                }
            }
            double dx = treeMouseX - cx;
            double dy = treeMouseY - cy;
            if (dx * dx + dy * dy <= radius * radius) {
                List<Component> tooltip = this.tooltip(tree, i, content, live, holdsAnchor, dormant);
                postRender.add(() -> {
                    if (minecraft.screen != null) {
                        minecraft.screen.renderComponentTooltip(poseStack, tooltip, screenMouseX, screenMouseY);
                    }
                });
            }
        }
    }

    private void renderHint(PoseStack poseStack, GodNodeWidget anchorWidget, Font font) {
        String text = "Unlock a transfer slot at " + this.god.getName() + " level "
                + GodLevels.minorTransferSlotUnlockLevel(0);
        int width = font.width(text) + 8;
        int height = 14;
        int right = anchorWidget.x - GAP_TO_STAR;
        int left = right - width;
        int top = Math.round(anchorWidget.y + anchorWidget.getHeight() / 2.0F - height / 2.0F);
        GuiComponent.fill(poseStack, left, top, right, top + height, GodTreeTheme.POPUP_FILL);
        GodDrawing.outline(poseStack, left, top, width, height, GodTreeTheme.accentDim(this.god));
        font.draw(poseStack, text, left + 4, top + 3, GodTreeTheme.TEXT_MUTED);
    }

    private List<Component> tooltip(GodTreeModel tree, int slot, @Nullable String content, boolean live,
                                    boolean holdsAnchor, boolean dormant) {
        List<Component> lines = new ArrayList<>();
        String label = "Slot " + GreedTheme.roman(slot + 1);
        int accent = GodTreeTheme.accent(this.god) & 0xFFFFFF;
        if (live) {
            lines.add(new TextComponent(label + " - " + nameOf(tree, content))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(accent))));
        } else {
            lines.add(new TextComponent(label + " - empty").withStyle(ChatFormatting.GRAY));
        }
        if (holdsAnchor) {
            lines.add(new TextComponent("Click to take " + this.anchor.name() + " out").withStyle(ChatFormatting.YELLOW));
        } else if (live) {
            lines.add(new TextComponent("Click to replace it with " + this.anchor.name()).withStyle(ChatFormatting.YELLOW));
        } else {
            lines.add(new TextComponent("Click to put " + this.anchor.name() + " here").withStyle(ChatFormatting.YELLOW));
        }
        if (dormant) {
            lines.add(new TextComponent("Dormant while " + this.god.getName()
                    + " is your active god - the constellation already applies it").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    @Nullable
    private static ResourceLocation iconOf(GodTreeModel tree, String effectId) {
        GodNode placement = tree.placementOf(effectId);
        if (placement == null) {
            return null;
        }
        SkillStyle style = tree.getStyle(placement.id());
        return style == null ? null : style.icon;
    }

    private static String nameOf(GodTreeModel tree, String effectId) {
        GodNode placement = tree.placementOf(effectId);
        return placement == null ? effectId : placement.name();
    }

    /** Puts the anchor star into the slot, or takes it out when the slot already holds it. */
    void click(int slot) {
        String content = ClientGodAlignmentData.getMinorTransferSlot(this.god, slot).orElse("");
        String target = content.equals(this.anchor.ledgerKey()) ? "" : this.anchor.ledgerKey();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound(ModSounds.SKILL_TREE_LEARN_SFX, 1.0F, 1.0F);
        }
        NetworkHandler.INSTANCE.sendToServer(new ServerboundSetMinorTransferMessage(this.god, slot, target));
    }
}
