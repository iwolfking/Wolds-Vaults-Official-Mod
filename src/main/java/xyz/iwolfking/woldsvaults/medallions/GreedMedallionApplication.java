package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The gates a greed medallion has to pass to land on a crystal: one medallion per crystal, and the
 * applying player's greed rank must be at or above the medallion's. Entering the vault is not gated.
 */
public final class GreedMedallionApplication {
    private GreedMedallionApplication() {
    }

    /**
     * Whether {@code player} may write {@code tier} onto {@code data}. Refusals are silent in-game and logged at
     * debug level.
     */
    public static boolean canApply(@Nullable Player player, CrystalData data, GreedMedallionTier tier) {
        if (data == null || tier == null) {
            WoldsVaults.LOGGER.debug("Greed medallion refused: no crystal data ({}) or no tier ({}).", data, tier);
            return false;
        }
        if (data.getProperties().isUnmodifiable()) {
            WoldsVaults.LOGGER.debug("Greed medallion {} refused: crystal is unmodifiable.", tier);
            return false;
        }
        if (((GreedMedallionCrystal) data).woldsvaults$hasMedallion()) {
            WoldsVaults.LOGGER.debug("Greed medallion {} refused: crystal already carries medallion rank {}.",
                    tier, ((GreedMedallionCrystal) data).woldsvaults$getMedallionRank());
            return false;
        }
        int rank = getGreedRank(player);
        if (rank < tier.getRankIndex()) {
            WoldsVaults.LOGGER.debug("Greed medallion {} refused: greed rank {} is below the required rank {}.",
                    tier, rank, tier.getRankIndex());
            return false;
        }
        return true;
    }

    public static void apply(CrystalData data, GreedMedallionTier tier) {
        ((GreedMedallionCrystal) data).woldsvaults$setMedallionRank(tier.getRankIndex());
    }

    /**
     * The player's greed rank: the greed tier integer as a rank index, Scavenger 1 = 1 through Legend = 16, or 0
     * when no server player resolves.
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
