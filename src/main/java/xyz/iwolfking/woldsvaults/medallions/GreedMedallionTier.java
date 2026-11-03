package xyz.iwolfking.woldsvaults.medallions;

import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The sixteen greed medallion tiers; a tier's rank index follows the greed rank convention (Scavenger 1 = 1
 * through Legend = 16) and compares directly against {@code PlayerGreedTreeData#getGreedTier}. Every number
 * a tier carries is pack data in {@code config/the_vault/gods/greed_medallions.json}: percent columns are
 * whole percents, and {@code trapDisarmPenalty} is a magnitude consumers subtract.
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

    private static final GreedMedallionsConfig.Tier NO_BONUSES = new GreedMedallionsConfig.Tier();
    private static final GreedMedallionsConfig.Spawn NO_SPAWNS = noSpawns();
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private final int rankIndex;

    GreedMedallionTier(int rankIndex) {
        this.rankIndex = rankIndex;
    }

    /** Installs the generated medallion table; the installer has already checked every rank, the gates and the spawn block. */
    public static void load(GreedMedallionsConfig config) {
        GreedMedallionsConfig.Tier[] applied = new GreedMedallionsConfig.Tier[values().length + 1];
        for (GreedMedallionTier tier : values()) {
            applied[tier.rankIndex] = config.tiers.get(String.valueOf(tier.rankIndex));
        }
        tiers = applied;
        gates = config.gates;
        spawn = config.spawn;
    }

    /** The assassin spawn curve; until the medallion table is installed it spawns nothing. */
    public static GreedMedallionsConfig.Spawn spawnCurve() {
        return spawn == null ? NO_SPAWNS : spawn;
    }

    public static Optional<GreedMedallionTier> byRankIndex(int rankIndex) {
        for (GreedMedallionTier tier : values()) {
            if (tier.rankIndex == rankIndex) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    /** Registry path segment for this tier: {@code scavenger_1} ... {@code champion_3}, {@code legend}. */
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
        return gates() != null && this.rankIndex >= gates.assassinsInheritVaultModifiers;
    }

    public boolean assassinsInflictNegativeEffects() {
        return gates() != null && this.rankIndex >= gates.assassinsInflictNegativeEffects;
    }

    public boolean assassinsGainBuffingAuras() {
        return gates() != null && this.rankIndex >= gates.assassinsGainBuffingAuras;
    }

    public boolean assassinsGainInfernalModifiers() {
        return gates() != null && this.rankIndex >= gates.assassinsGainInfernalModifiers;
    }

    /**
     * From this rank up a medallion builds rage and can summon a Vault Champion; each such rank needs an entry in
     * {@code gods/greed_champion.json}.
     */
    public boolean vaultChampionHunts() {
        return gates() != null && this.rankIndex >= gates.vaultChampionHunts;
    }

    /** From this rank up, the arming roll uses the higher legend chance rather than the base one. */
    public boolean vaultChampionEnraged() {
        return gates() != null && this.rankIndex >= gates.vaultChampionEnraged;
    }

    private GreedMedallionsConfig.Tier row() {
        return tiers == null ? NO_BONUSES : tiers[this.rankIndex];
    }

    @Nullable
    private static GreedMedallionsConfig.Gates gates() {
        if (gates == null && WARNED.compareAndSet(false, true)) {
            WoldsVaults.LOGGER.error("Greed medallions were read before the greed/medallions/greed_medallions table was installed; "
                    + "medallions add nothing until it is");
        }
        return gates;
    }

    private static GreedMedallionsConfig.Spawn noSpawns() {
        GreedMedallionsConfig.Spawn none = new GreedMedallionsConfig.Spawn();
        none.recoveryTicks = 1.0D;
        return none;
    }
}
