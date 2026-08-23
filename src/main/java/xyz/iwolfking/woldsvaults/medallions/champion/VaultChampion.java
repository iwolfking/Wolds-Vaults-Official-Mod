package xyz.iwolfking.woldsvaults.medallions.champion;

import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import xyz.iwolfking.woldsvaults.config.GreedChampionConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;

import java.util.Optional;
import java.util.UUID;

/**
 * Identity for the Vault Champion, a {@code TheVesselEntity} carrying the {@code greed_champion} tag
 * rather than an entity type of its own; the tag is the only thing that tells it from the greed trial
 * boss. Its persistent data holds the medallion rank, the immutable summoner that owns the rage
 * bookkeeping, the mutable hunt target, and the vault id the manager re-adopts it by.
 */
public final class VaultChampion {
    public static final String CHAMPION_TAG = "greed_champion";

    private static final String RANK_KEY = "woldsvaults:greed_champion_rank";
    private static final String SUMMONER_KEY = "woldsvaults:greed_champion_summoner";
    private static final String HUNT_TARGET_KEY = "woldsvaults:greed_champion_hunt_target";
    private static final String VAULT_KEY = "woldsvaults:greed_champion_vault";

    private VaultChampion() {
    }

    public static boolean isChampion(Entity entity) {
        return entity instanceof TheVesselEntity && entity.getTags().contains(CHAMPION_TAG);
    }

    public static void stamp(Entity entity, GreedMedallionTier tier, UUID summoner, UUID vaultId) {
        CompoundTag data = entity.getPersistentData();
        data.putInt(RANK_KEY, tier.getRankIndex());
        data.putUUID(SUMMONER_KEY, summoner);
        data.putUUID(HUNT_TARGET_KEY, summoner);
        if (vaultId != null) {
            data.putUUID(VAULT_KEY, vaultId);
        }
    }

    /** The vault this Champion was summoned in, or null on one stamped before the key existed. */
    public static UUID getVaultId(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.hasUUID(VAULT_KEY) ? data.getUUID(VAULT_KEY) : null;
    }

    public static Optional<GreedMedallionTier> getTier(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        CompoundTag data = entity.getPersistentData();
        return data.contains(RANK_KEY) ? GreedMedallionTier.byRankIndex(data.getInt(RANK_KEY)) : Optional.empty();
    }

    public static UUID getSummoner(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.hasUUID(SUMMONER_KEY) ? data.getUUID(SUMMONER_KEY) : null;
    }

    public static UUID getHuntTarget(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.hasUUID(HUNT_TARGET_KEY) ? data.getUUID(HUNT_TARGET_KEY) : null;
    }

    public static void setHuntTarget(Entity entity, UUID target) {
        entity.getPersistentData().putUUID(HUNT_TARGET_KEY, target);
    }

    private static GreedChampionConfig fallback;

    /**
     * The live champion config, or the shipped defaults when the pack file is unread or invalid; never null, since
     * Vessel mixins can run before configs are registered.
     */
    public static GreedChampionConfig config() {
        GreedChampionConfig config = ModConfigs.GREED_CHAMPION;
        if (config != null && config.isValid()) {
            return config;
        }
        if (fallback == null) {
            fallback = GreedChampionConfig.defaults();
        }
        return fallback;
    }

    public static GreedChampionConfig.Rank rankStats(Entity entity) {
        return getTier(entity).map(tier -> config().getRank(tier.getRankIndex())).orElse(null);
    }
}
