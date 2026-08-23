package xyz.iwolfking.woldsvaults.client.screens.gods;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodLevels;
import xyz.iwolfking.woldsvaults.gods.sacrifice.GodSacrifices;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The god experience bar both the star chart and the overview draw: a slim bar filling toward the
 * next level in the god's accent, optionally with the god's icon and level number over it, and a
 * hover tooltip with the exact experience numbers and the sacrifice standing. The tooltip goes to
 * a sink rather than straight to the screen so each surface can layer it where it needs to.
 */
public final class GodXpBarRenderer {
    public static final int BAR_HEIGHT = 5;

    private GodXpBarRenderer() {
    }

    public static void render(PoseStack poseStack, Font font, VaultGod god, Rectangle bar, boolean withIconAndLevel,
                              int mouseX, int mouseY, Consumer<List<Component>> tooltipSink) {
        int level = ClientGodAlignmentData.getLevel(god);
        long xp = ClientGodAlignmentData.getXp(god);
        long currentThreshold = GodLevels.xpForLevel(level);
        long nextThreshold = GodLevels.xpForLevel(level + 1);
        float progress = nextThreshold > currentThreshold
                ? (float) (xp - currentThreshold) / (float) (nextThreshold - currentThreshold)
                : 1.0F;
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        int accent = GodTreeTheme.pointsColor(god);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 100.0D);
        if (withIconAndLevel) {
            GodTreeTheme.godIcon(god).blit(poseStack, bar.x - 20, bar.y - 6, 0, 16, 16);
        }
        GuiComponent.fill(poseStack, bar.x - 1, bar.y - 1, bar.x + bar.width + 1, bar.y + bar.height + 1, 0xCC000000);
        GuiComponent.fill(poseStack, bar.x, bar.y, bar.x + bar.width, bar.y + bar.height, 0xFF1A0F12);
        int filled = (int) (bar.width * progress);
        if (filled > 0) {
            GuiComponent.fill(poseStack, bar.x, bar.y, bar.x + filled, bar.y + bar.height, accent);
        }
        if (withIconAndLevel) {
            String levelText = String.valueOf(level);
            FontHelper.drawStringWithBorder(poseStack, levelText,
                    bar.x + (bar.width - font.width(levelText)) / 2.0F, bar.y - 10,
                    accent & 0xFFFFFF | 0xFF000000, 0xFF1E1E1E);
        }
        poseStack.popPose();
        Rectangle hover = withIconAndLevel
                ? new Rectangle(bar.x - 20, bar.y - 10, bar.width + 20, bar.height + 12)
                : new Rectangle(bar.x - 1, bar.y - 1, bar.width + 2, bar.height + 2);
        if (hover.contains(mouseX, mouseY)) {
            tooltipSink.accept(tooltipLines(god));
        }
    }

    /** The hover text: level with exact experience, then the sacrifice standing. */
    public static List<Component> tooltipLines(VaultGod god) {
        int level = ClientGodAlignmentData.getLevel(god);
        long xp = ClientGodAlignmentData.getXp(god);
        long nextThreshold = GodLevels.xpForLevel(level + 1);
        List<Component> lines = new ArrayList<>();
        lines.add(new TextComponent(god.getName() + " - Level " + level + "  (")
                .append(String.format("%,d", xp)).append(" / ").append(String.format("%,d", nextThreshold))
                .append(" XP)"));
        lines.add(sacrificeLine(god));
        return lines;
    }

    /** Whether the sacrifice the player's current level waits on has been performed. */
    public static boolean sacrificePerformed(VaultGod god) {
        int sacrifices = ClientGodAlignmentData.getSacrifices(god);
        return sacrifices >= GodSacrifices.GATE_COUNT || sacrifices > ClientGodAlignmentData.getLevel(god);
    }

    /** The one-line sacrifice standing, worded as the chart's bar tooltip has always worded it. */
    public static Component sacrificeLine(VaultGod god) {
        int level = ClientGodAlignmentData.getLevel(god);
        long xp = ClientGodAlignmentData.getXp(god);
        int sacrifices = ClientGodAlignmentData.getSacrifices(god);
        if (sacrifices >= GodSacrifices.GATE_COUNT) {
            return new TextComponent("Every cauldron sacrifice has been performed.").withStyle(ChatFormatting.GRAY);
        }
        if (sacrifices > level) {
            return new TextComponent("Next sacrifice: already performed.").withStyle(ChatFormatting.GREEN);
        }
        String gate = GodSacrifices.gateLabel(sacrifices);
        if (GodLevels.levelForXp(xp) > level) {
            return new TextComponent(gate + " required at the Greed Cauldron to advance!").withStyle(ChatFormatting.RED);
        }
        return new TextComponent(gate + ": not yet performed.").withStyle(ChatFormatting.GRAY);
    }
}
