package xyz.iwolfking.woldsvaults.medallions;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.Map;
import java.util.Optional;

/**
 * The sixteen greed medallion tiers. The enum is the identity of a tier - what the item registry,
 * the textures and the one NBT int riding the crystal all refer to - and its rank index matches the
 * greed rank convention used across the rework (Scavenger 1 = 1 ... Hunter 1 = 7, Master 1 = 10,
 * Legend = 16), so a medallion's rank compares directly against
 * {@code PlayerGreedTreeData#getGreedTier}.
 *
 * <p>Every number a tier carries is pack data in
 * {@code config/the_vault/gods/greed_medallions.json}, installed by {@code ModConfigs.register}.
 * The enum runs on the shipped table from its own class init, because the medallion items are
 * registered during mod construction and their registry paths are read long before configs are.
 *
 * <p>Percent columns are whole percents (25 = +25%). {@code trapDisarmPenalty} is the magnitude of
 * the reduction; consumers subtract it. All medallion bonuses are multiplicative with every other
 * source per the design doc. The band a tier sits in and its step inside that band are not here
 * either: they are the rank ladder's shape and come from {@link MilestoneRankLadder}.</p>
 */
public enum GreedMedallionTier {
    SCAVENGER_1(1),
    SCAVENGER_2(2),
    SCAVENGER_3(3),
    LOOTER_1(4),
    LOOTER_2(5),
    LOOTER_3(6),
    HUNTER_1(7),
    HUNTER_2(8),
    HUNTER_3(9),
    MASTER_1(10),
    MASTER_2(11),
    MASTER_3(12),
    CHAMPION_1(13),
    CHAMPION_2(14),
    CHAMPION_3(15),
    LEGEND(16);

    private static GreedMedallionsConfig.Tier[] tiers;
    private static GreedMedallionsConfig.Gates gates;
    private static GreedMedallionsConfig.Spawn spawn;

    static {
        apply(GreedMedallionsConfig.defaults());
    }

    private final int rankIndex;

    GreedMedallionTier(int rankIndex) {
        this.rankIndex = rankIndex;
    }

    /**
     * Installs a medallion table read from config. A file that does not describe every rank is
     * refused whole and the shipped table used instead: half a table would leave some medallions
     * silently doing nothing, which reads in play as a broken item rather than a broken config.
     */
    public static void load(GreedMedallionsConfig config) {
        if (isUsable(config)) {
            apply(config);
            return;
        }
        apply(GreedMedallionsConfig.defaults());
    }

    /** The assassin spawn-rate curve, for the spawner that is its only reader. */
    public static GreedMedallionsConfig.Spawn spawnCurve() {
        return spawn;
    }

    public static Optional<GreedMedallionTier> byRankIndex(int rankIndex) {
        for (GreedMedallionTier tier : values()) {
            if (tier.rankIndex == rankIndex) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    private static boolean isUsable(GreedMedallionsConfig config) {
        Map<String, GreedMedallionsConfig.Tier> configured = config.getTiers();
        if (configured == null) {
            WoldsVaults.LOGGER.error("greed_medallions.json lists no tiers; using the shipped table instead");
            return false;
        }
        for (GreedMedallionTier tier : values()) {
            if (configured.get(String.valueOf(tier.rankIndex)) == null) {
                WoldsVaults.LOGGER.error("greed_medallions.json has no entry for rank {} ({}); using the shipped "
                        + "table instead", tier.rankIndex, tier.name());
                return false;
            }
        }
        if (config.getGates() == null || config.getSpawn() == null) {
            WoldsVaults.LOGGER.error("greed_medallions.json is missing its gates or spawn block; using the shipped "
                    + "table instead");
            return false;
        }
        return true;
    }

    private static void apply(GreedMedallionsConfig config) {
        GreedMedallionsConfig.Tier[] applied = new GreedMedallionsConfig.Tier[values().length + 1];
        for (GreedMedallionTier tier : values()) {
            applied[tier.rankIndex] = config.getTiers().get(String.valueOf(tier.rankIndex));
        }
        tiers = applied;
        gates = config.getGates();
        spawn = config.getSpawn();
    }

    /**
     * Registry path segment for this tier: {@code scavenger_1} ... {@code champion_3},
     * {@code legend}. Item ids, textures and lang keys all derive from it.
     */
    public String getPathName() {
        int tierInBand = this.getTierInBand();
        return tierInBand > 0 ? this.getBand() + "_" + tierInBand : this.getBand();
    }

    public String getBand() {
        return MilestoneRankLadder.getBandName(this.rankIndex);
    }

    public int getTierInBand() {
        return MilestoneRankLadder.getTierInBand(this.rankIndex);
    }

    public int getRankIndex() {
        return this.rankIndex;
    }

    public int getBaseGreedCoins() {
        return this.row().baseGreedCoins;
    }

    public int getMobHealthDamageBonus() {
        return this.row().mobHealthDamageBonus;
    }

    public int getCrateLootBonus() {
        return this.row().crateLootBonus;
    }

    public int getGreedCrateLootTier() {
        return this.row().greedCrateLootTier;
    }

    public int getChestRollsBonus() {
        return this.row().chestRollsBonus;
    }

    public int getTrapDisarmPenalty() {
        return this.row().trapDisarmPenalty;
    }

    public int getMobSpawnBonus() {
        return this.row().mobSpawnBonus;
    }

    public int getObjectiveDifficultyBonus() {
        return this.row().objectiveDifficultyBonus;
    }

    public boolean assassinsInheritVaultModifiers() {
        return this.rankIndex >= gates.assassinsInheritVaultModifiers;
    }

    public boolean assassinsInflictNegativeEffects() {
        return this.rankIndex >= gates.assassinsInflictNegativeEffects;
    }

    public boolean assassinsGainBuffingAuras() {
        return this.rankIndex >= gates.assassinsGainBuffingAuras;
    }

    public boolean assassinsGainInfernalModifiers() {
        return this.rankIndex >= gates.assassinsGainInfernalModifiers;
    }

    /**
     * From this rank up, a medallion builds rage and can summon a Vault Champion at all - and every
     * such rank needs a matching entry in {@code gods/greed_champion.json}, which is what actually
     * decides what one is made of.
     */
    public boolean vaultChampionHunts() {
        return this.rankIndex >= gates.vaultChampionHunts;
    }

    /** From this rank up, the arming roll uses the higher legend chance rather than the base one. */
    public boolean vaultChampionEnraged() {
        return this.rankIndex >= gates.vaultChampionEnraged;
    }

    private GreedMedallionsConfig.Tier row() {
        return tiers[this.rankIndex];
    }
}
