package xyz.iwolfking.woldsvaults.util;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.render.IVaultOptions;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.util.function.ObservableSupplier;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityStatData;
import xyz.iwolfking.woldsvaults.data.skill.PlayerDivinityStats;

public class DivinityUtils {
    public static int unspentDivinityPoints;
    public static final ObservableSupplier<Integer> DIVINITY_POINT_SUPPLIER = ObservableSupplier.of(() -> unspentDivinityPoints, Integer::equals);
    public static Component unspentDivinityPointComponent;
    public static int unspentDivinityPointComponentWidth;

    public static SkillContext ofDivinity(ServerPlayer player) {
        PlayerVaultStats stats = PlayerVaultStatsData.get((ServerLevel)player.level).getVaultStats(player);
        PlayerDivinityStats divinityStats = PlayerDivinityStatData.get((ServerLevel)player.level).getVaultStats(player);
        return new SkillContext(stats.getVaultLevel(), divinityStats.getUnspentDivinityPoints(), 0, SkillSource.of(player));
    }


    // VaultBarOverlay Methods
    public static void onUnspentDivinityPointsChanged(int unspentPowerPoints) {
        int absUnspentPowerPoint = Math.abs(unspentPowerPoints);

        DivinityUtils.unspentDivinityPointComponent = ComponentUtils.wavingComponent(
                new TextComponent(unspentPowerPoints + " unspent Divinity point" + (absUnspentPowerPoint == 1 ? "" : "s")),
                TextColor.parseColor("#00fff7"),
                0.1F,
                0.6F
        );
        DivinityUtils.unspentDivinityPointComponentWidth = Minecraft.getInstance().font.width(DivinityUtils.unspentDivinityPointComponent);
    }

    public static void handleOverlayTextRendering(Minecraft minecraft, int right, PoseStack poseStack, MultiBufferSource.BufferSource buffer) {
        IVaultOptions options = (IVaultOptions)Minecraft.getInstance().options;
        if (options.showPointMessages()) {
            int x;
            int gap = 5;

            minecraft.getProfiler().popPush("batchDivinityPointText");
            if (DivinityUtils.unspentDivinityPoints != 0 && VaultBarOverlay.vaultLevel >= 100) {
                DivinityUtils.DIVINITY_POINT_SUPPLIER.ifChangedOrElse(DivinityUtils::onUnspentDivinityPointsChanged, DivinityUtils::onUnspentDivinityPointsChanged); // oh well lmao
                x = right - DivinityUtils.unspentDivinityPointComponentWidth - gap;
                minecraft.font.drawInBatch(DivinityUtils.unspentDivinityPointComponent, (float)x, 18.0F, 16777215, true, poseStack.last().pose(), buffer, false, 0, 15728880);
                poseStack.translate(0.0, 12.0, 0.0);
            }
        }
    }
}
