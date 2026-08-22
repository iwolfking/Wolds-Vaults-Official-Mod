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
 * Identity for the Vault Champion.
 *
 * <p>The Champion is not its own entity type - it is {@code TheVesselEntity}, the same class the
 * greed trial spawns in its arena. Every behaviour override in this feature therefore has to
 * distinguish the two at runtime, and the {@code greed_champion} tag is the only marker that does
 * it: a type check would catch the trial boss and break the fight the rank-up trials depend on.
 *
 * <p>Three values ride the entity's persistent data, which survives save and load and outlives the
 * in-memory vault state. The medallion rank is what every stat and reward reads. The summoner is
 * immutable and owns the rage bookkeeping - whose counter resets on defeat, whose threshold
 * escalates. The hunt target is mutable and is who the Champion is actually chasing; the two part
 * company the moment the summoner dies or leaves and the Champion re-binds to someone else.</p>
 */
public final class VaultChampion {
    public static final String CHAMPION_TAG = "greed_champion";

    private static final String RANK_KEY = "woldsvaults:greed_champion_rank";
    private static final String SUMMONER_KEY = "woldsvaults:greed_champion_summoner";
    private static final String HUNT_TARGET_KEY = "woldsvaults:greed_champion_hunt_target";

    private VaultChampion() {
    }

    public static boolean isChampion(Entity entity) {
        return entity instanceof TheVesselEntity && entity.getTags().contains(CHAMPION_TAG);
    }

    public static void stamp(Entity entity, GreedMedallionTier tier, UUID summoner) {
        CompoundTag data = entity.getPersistentData();
        data.putInt(RANK_KEY, tier.getRankIndex());
        data.putUUID(SUMMONER_KEY, summoner);
        data.putUUID(HUNT_TARGET_KEY, summoner);
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
     * The live champion config, falling back to the shipped defaults when the pack file has not been
     * read yet or did not survive validation. Mixins on the Vessel sit on a damage path and can run
     * before configs are registered, so this can never be allowed to return null.
     *
     * <p>The fallback is built on demand and then kept. Building one per call would allocate on every
     * swing, but building one eagerly in a static initialiser would drag the base mod's {@code Config}
     * class - and the very large registry-backed GSON graph attached to it - into whatever class-load
     * first touched this, which on the {@code Mob} target could be early in world load.</p>
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

    /** The stat block for the tier stamped on this entity, or null if it carries none. */
    public static GreedChampionConfig.Rank rankStats(Entity entity) {
        return getTier(entity).map(tier -> config().getRank(tier.getRankIndex())).orElse(null);
    }
}
