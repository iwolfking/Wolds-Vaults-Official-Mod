package xyz.iwolfking.woldsvaults.medallions;

import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The sixteen greed medallion tiers; a tier's rank index follows the greed rank convention (Scavenger 1 = 1
 * through Legend = 16) and compares directly against {@code PlayerGreedTreeData#getGreedTier}. Every number
 * a tier carries comes from the generated medallion table: percent columns are whole percents, and
 * {@code trapDisarmPenalty} is a magnitude consumers subtract.
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
    private static GreedMedallionsConfig.Fury fury;
    private static GreedMedallionsConfig.Assassins assassins;
    private static GreedMedallionsConfig.Crate crate;

    private static final GreedMedallionsConfig.Tier NO_BONUSES = new GreedMedallionsConfig.Tier();

    private final int rankIndex;

    GreedMedallionTier(int rankIndex) {
        this.rankIndex = rankIndex;
    }

    /** Installs the generated medallion table; the installer has already checked every rank and block. */
    public static void load(GreedMedallionsConfig config) {
        GreedMedallionsConfig.Tier[] applied = new GreedMedallionsConfig.Tier[values().length + 1];
        for (GreedMedallionTier tier : values()) {
            applied[tier.rankIndex] = config.tiers.get(String.valueOf(tier.rankIndex));
        }
        tiers = applied;
        gates = config.gates;
        fury = config.fury;
        assassins = config.assassins;
        crate = config.crate;
    }

    /** The assassin fury curve, or null until the medallion table is installed, in which case no assassins spawn. */
    @Nullable
    public static GreedMedallionsConfig.Fury furyCurve() {
        return installed(fury);
    }

    /** What greed assassins do once spawned, or null until the medallion table is installed. */
    @Nullable
    public static GreedMedallionsConfig.Assassins assassinSettings() {
        return installed(assassins);
    }

    /** The completion crate coin settings, or null until the medallion table is installed. */
    @Nullable
    public static GreedMedallionsConfig.Crate crateSettings() {
        return installed(crate);
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

    public int getAssassinGreedCoins() {
        return this.row().assassinGreedCoins;
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

    public double getAssassinFuryGain() {
        return this.row().assassinFuryGain;
    }

    public double getAssassinSquadTarget() {
        return this.row().assassinSquadTarget;
    }

    public double getAssassinRecoverySeconds() {
        return this.row().assassinRecoverySeconds;
    }

    public int getOmegaRoomWeightBonus() {
        return this.row().omegaRoomWeightBonus;
    }

    /** The gear roll type that replaces {@code Omega+} in this rank's vaults; null until the table is installed. */
    @Nullable
    public String getOmegaPlusRollType() {
        return this.row().omegaPlusRollType;
    }

    /** The displayed strongbox tier of this rank's vaults, 0 for untiered strongboxes. */
    public int getStrongboxTier() {
        return this.row().strongboxTier;
    }

    public boolean assassinsInheritVaultModifiers() {
        return installed(gates) != null && this.rankIndex >= gates.assassinsInheritVaultModifiers;
    }

    public boolean assassinsInflictNegativeEffects() {
        return installed(gates) != null && this.rankIndex >= gates.assassinsInflictNegativeEffects;
    }

    public boolean assassinsGainBuffingAuras() {
        return installed(gates) != null && this.rankIndex >= gates.assassinsGainBuffingAuras;
    }

    public boolean assassinsGainInfernalModifiers() {
        return installed(gates) != null && this.rankIndex >= gates.assassinsGainInfernalModifiers;
    }

    /** From this rank up a medallion builds rage and can summon a Vault Champion, if the Champion table has its rank. */
    public boolean vaultChampionHunts() {
        return installed(gates) != null && this.rankIndex >= gates.vaultChampionHunts;
    }

    /** From this rank up, the arming roll uses the higher legend chance rather than the base one. */
    public boolean vaultChampionEnraged() {
        return installed(gates) != null && this.rankIndex >= gates.vaultChampionEnraged;
    }

    private GreedMedallionsConfig.Tier row() {
        GreedMedallionsConfig.Tier[] installed = installed(tiers);
        return installed == null ? NO_BONUSES : installed[this.rankIndex];
    }

    @Nullable
    private static <T> T installed(@Nullable T block) {
        if (block == null) {
            WoldLogger.errorOnce("greed-medallion-table-missing", "Greed medallions were read before the "
                    + "greed/medallions/greed_medallions table was installed; medallions add nothing and no assassins "
                    + "spawn until it is");
        }
        return block;
    }
}
