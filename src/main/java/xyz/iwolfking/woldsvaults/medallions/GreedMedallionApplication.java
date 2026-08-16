package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The gates a greed medallion has to pass to land on a crystal.
 *
 * <p>Two of the three rules in the design doc live here: a crystal holds exactly one medallion and
 * is locked afterwards, and a player may only <i>apply</i> a medallion whose rank is at or below
 * their own greed rank. The third rule — anyone may enter the resulting vault regardless of rank —
 * needs no code, since nothing reads the medallion when a player steps through a portal.
 *
 * <p>The crafting-time rank gate the doc also describes belongs to the future Medallion Infuser and
 * is deliberately not implemented here.
 */
public final class GreedMedallionApplication {
    private GreedMedallionApplication() {
    }

    /**
     * Whether {@code player} may write {@code tier} onto {@code data} right now. Refusals are
     * silent by design: the workbench simply produces no output and the medallion stays in its
     * slot.
     */
    public static boolean canApply(@Nullable Player player, CrystalData data, GreedMedallionTier tier) {
        if (data == null || tier == null) {
            return false;
        }
        if (data.getProperties().isUnmodifiable()) {
            return false;
        }
        if (((GreedMedallionCrystal) data).woldsvaults$hasMedallion()) {
            return false;
        }
        return getGreedRank(player) >= tier.getRankIndex();
    }

    public static void apply(CrystalData data, GreedMedallionTier tier) {
        ((GreedMedallionCrystal) data).woldsvaults$setMedallionRank(tier.getRankIndex());
    }

    /**
     * The player's greed rank on the interim convention shared across the rework: the greed tier
     * integer is the rank index, Scavenger 1 = 1 through Legend = 16. A player that cannot be
     * resolved server-side ranks 0, which fails every gate.
     */
    public static int getGreedRank(@Nullable Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            WoldsVaults.LOGGER.warn("Greed medallion rank gate could not resolve a server player ({}); refusing the application.",
                    player == null ? "null" : player.getClass().getName());
            return 0;
        }
        return PlayerGreedTreeData.get(serverPlayer.getLevel()).getGreedTier(serverPlayer);
    }
}
