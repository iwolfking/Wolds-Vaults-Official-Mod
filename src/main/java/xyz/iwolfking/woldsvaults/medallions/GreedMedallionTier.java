package xyz.iwolfking.woldsvaults.medallions;

import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.Optional;

/**
 * The sixteen greed medallion tiers and their vault-side numbers, exactly as tabled in the greed
 * rework design doc. Rank index matches the interim greed-tier integer convention used across the
 * rework (Scavenger 1 = 1 ... Hunter 1 = 7, Master 1 = 10, Legend = 16), so a medallion's rank can
 * be compared directly against {@code PlayerGreedTreeData#getGreedTier}.
 *
 * <p>Percent columns are stored as whole percents (25 = +25%). {@code trapDisarmPenalty} is the
 * magnitude of the reduction; consumers subtract it. All medallion bonuses are multiplicative with
 * every other source per the design doc. The band a tier sits in and its step inside that band are
 * deliberately not tabled here: they are the rank ladder's shape and come from
 * {@link MilestoneRankLadder}.</p>
 */
public enum GreedMedallionTier {
    SCAVENGER_1(1, 2, 25, 0, 0, 0, 0, 0, 0),
    SCAVENGER_2(2, 3, 45, 5, 0, 0, 0, 0, 0),
    SCAVENGER_3(3, 3, 60, 5, 1, 0, 0, 0, 0),
    LOOTER_1(4, 4, 90, 10, 1, 0, 0, 0, 0),
    LOOTER_2(5, 5, 125, 15, 1, 10, 50, 33, 0),
    LOOTER_3(6, 5, 150, 20, 2, 10, 75, 33, 0),
    HUNTER_1(7, 6, 225, 25, 2, 15, 100, 33, 0),
    HUNTER_2(8, 7, 300, 25, 3, 15, 125, 50, 25),
    HUNTER_3(9, 8, 400, 30, 4, 20, 175, 50, 25),
    MASTER_1(10, 9, 550, 35, 4, 20, 175, 50, 25),
    MASTER_2(11, 10, 700, 35, 4, 25, 200, 60, 35),
    MASTER_3(12, 11, 950, 40, 5, 25, 200, 60, 35),
    CHAMPION_1(13, 12, 1250, 40, 5, 30, 250, 60, 40),
    CHAMPION_2(14, 13, 1600, 45, 6, 30, 275, 65, 45),
    CHAMPION_3(15, 14, 2000, 45, 6, 35, 300, 70, 45),
    LEGEND(16, 15, 2500, 50, 7, 40, 350, 75, 50);

    private final int rankIndex;
    private final int baseGreedCoins;
    private final int mobHealthDamageBonus;
    private final int crateLootBonus;
    private final int greedCrateLootTier;
    private final int chestRollsBonus;
    private final int trapDisarmPenalty;
    private final int mobSpawnBonus;
    private final int objectiveDifficultyBonus;

    GreedMedallionTier(int rankIndex, int baseGreedCoins,
                       int mobHealthDamageBonus, int crateLootBonus, int greedCrateLootTier,
                       int chestRollsBonus, int trapDisarmPenalty, int mobSpawnBonus,
                       int objectiveDifficultyBonus) {
        this.rankIndex = rankIndex;
        this.baseGreedCoins = baseGreedCoins;
        this.mobHealthDamageBonus = mobHealthDamageBonus;
        this.crateLootBonus = crateLootBonus;
        this.greedCrateLootTier = greedCrateLootTier;
        this.chestRollsBonus = chestRollsBonus;
        this.trapDisarmPenalty = trapDisarmPenalty;
        this.mobSpawnBonus = mobSpawnBonus;
        this.objectiveDifficultyBonus = objectiveDifficultyBonus;
    }

    public static Optional<GreedMedallionTier> byRankIndex(int rankIndex) {
        for (GreedMedallionTier tier : values()) {
            if (tier.rankIndex == rankIndex) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
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
        return this.baseGreedCoins;
    }

    public int getMobHealthDamageBonus() {
        return this.mobHealthDamageBonus;
    }

    public int getCrateLootBonus() {
        return this.crateLootBonus;
    }

    public int getGreedCrateLootTier() {
        return this.greedCrateLootTier;
    }

    public int getChestRollsBonus() {
        return this.chestRollsBonus;
    }

    public int getTrapDisarmPenalty() {
        return this.trapDisarmPenalty;
    }

    public int getMobSpawnBonus() {
        return this.mobSpawnBonus;
    }

    public int getObjectiveDifficultyBonus() {
        return this.objectiveDifficultyBonus;
    }

    public boolean assassinsInheritVaultModifiers() {
        return this.rankIndex >= LOOTER_1.rankIndex;
    }

    public boolean assassinsInflictNegativeEffects() {
        return this.rankIndex >= HUNTER_1.rankIndex;
    }

    public boolean assassinsGainBuffingAuras() {
        return this.rankIndex >= HUNTER_3.rankIndex;
    }

    public boolean assassinsGainInfernalModifiers() {
        return this.rankIndex >= CHAMPION_1.rankIndex;
    }

    /**
     * Vault Champion behaviors (Master 1 hunt, Legend enrage) are deferred; these exist so the
     * later champion wave has its gates already tabled.
     */
    public boolean vaultChampionHunts() {
        return this.rankIndex >= MASTER_1.rankIndex;
    }

    public boolean vaultChampionEnraged() {
        return this.rankIndex >= LEGEND.rankIndex;
    }
}
