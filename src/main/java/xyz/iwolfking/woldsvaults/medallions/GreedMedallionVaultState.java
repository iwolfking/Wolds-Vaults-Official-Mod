package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.Optional;

/**
 * The one way to read which greed medallion a vault was opened with. The rank is set when the vault is built from a
 * medallion-bearing crystal and saved with the vault's own data, so it survives a server restart and ends with the
 * vault. Server side only: clients never receive it.
 */
public final class GreedMedallionVaultState {
    private GreedMedallionVaultState() {
    }

    public static void set(Vault vault, GreedMedallionTier tier) {
        if (vault == null || tier == null) {
            WoldLogger.error("Greed medallion {} could not be written to vault {}; the vault runs without it.",
                    tier, vault);
            return;
        }
        ((GreedMedallionVault) vault).woldsvaults$setMedallionRank(tier.getRankIndex());
    }

    public static Optional<GreedMedallionTier> get(Vault vault) {
        if (vault == null) {
            return Optional.empty();
        }
        int rank = ((GreedMedallionVault) vault).woldsvaults$getMedallionRank();
        if (rank <= 0) {
            return Optional.empty();
        }
        Optional<GreedMedallionTier> tier = GreedMedallionTier.byRankIndex(rank);
        if (tier.isEmpty()) {
            WoldLogger.errorOnce("greed-medallion-vault-rank-" + rank,
                    "A vault carries greed medallion rank {}, which matches no tier; it runs without medallion effects.",
                    rank);
        }
        return tier;
    }

    /** The medallion of the vault this level belongs to; empty for any other level and on the client. */
    public static Optional<GreedMedallionTier> get(Level level) {
        if (level == null || level.isClientSide) {
            return Optional.empty();
        }
        return VaultUtils.getVault(level).flatMap(GreedMedallionVaultState::get);
    }

    public static Optional<GreedMedallionTier> get(Entity entity) {
        return entity == null ? Optional.empty() : get(entity.getLevel());
    }

    /**
     * The medallion greed assassins and the Vault Champion hunt under: the vault's medallion, except in herald and
     * rebirth vaults, where neither appears.
     */
    public static Optional<GreedMedallionTier> huntRank(Vault vault) {
        if (vault == null || VaultUtils.isHeraldVault(vault) || VaultUtils.isRebirthVault(vault)) {
            return Optional.empty();
        }
        return get(vault);
    }
}
