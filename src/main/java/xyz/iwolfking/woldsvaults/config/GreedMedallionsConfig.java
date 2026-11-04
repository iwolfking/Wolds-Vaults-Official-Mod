package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

/**
 * What a greed medallion does to the vault it rides, generated as
 * {@code vault_configs/greed/medallions/greed_medallions.json}: the numeric columns of every tier, the rank each
 * assassin and Champion behaviour switches on at, and the assassin spawn-rate curve. Tiers are keyed by rank number,
 * from the first rank to Legend.
 */
public class GreedMedallionsConfig extends Config implements WoldsTable {

    /** One tier's vault-side numbers. Percent columns are whole percents. */
    public static class Tier {
        @Expose public int baseGreedCoins;
        @Expose public int mobHealthDamageBonus;
        @Expose public int crateLootBonus;
        @Expose public int greedCrateLootTier;
        @Expose public int chestRollsBonus;
        @Expose public int trapDisarmPenalty;
        @Expose public int mobSpawnBonus;
        @Expose public int objectiveDifficultyBonus;

        public static Tier of(int baseGreedCoins, int mobHealthDamageBonus, int crateLootBonus, int greedCrateLootTier,
                              int chestRollsBonus, int trapDisarmPenalty, int mobSpawnBonus, int objectiveDifficultyBonus) {
            Tier tier = new Tier();
            tier.baseGreedCoins = baseGreedCoins;
            tier.mobHealthDamageBonus = mobHealthDamageBonus;
            tier.crateLootBonus = crateLootBonus;
            tier.greedCrateLootTier = greedCrateLootTier;
            tier.chestRollsBonus = chestRollsBonus;
            tier.trapDisarmPenalty = trapDisarmPenalty;
            tier.mobSpawnBonus = mobSpawnBonus;
            tier.objectiveDifficultyBonus = objectiveDifficultyBonus;
            return tier;
        }
    }

    /** The rank each behaviour switches on at, as a medallion rank number. */
    public static class Gates {
        @Expose public int assassinsInheritVaultModifiers;
        @Expose public int assassinsInflictNegativeEffects;
        @Expose public int assassinsGainBuffingAuras;
        @Expose public int assassinsGainInfernalModifiers;
        @Expose public int vaultChampionHunts;
        @Expose public int vaultChampionEnraged;
    }

    /** Assassin spawn chance per attempt: base * (1 + rankChanceStep * (rank - 1)), decaying per spawn and recovering over time. */
    public static class Spawn {
        @Expose public double hordeBaseChance;
        @Expose public double eliteBaseChance;
        @Expose public double rankChanceStep;
        @Expose public double decayFactor;
        @Expose public double recoveryTicks;
    }

    @Expose public Map<String, Tier> tiers;
    @Expose public Gates gates;
    @Expose public Spawn spawn;

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_medallions";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.tiers == null) {
            return "it lists no tiers";
        }
        for (int rank = MilestoneRankLadder.FIRST_RANK; rank <= MilestoneRankLadder.LEGEND_RANK; rank++) {
            if (this.tiers.get(String.valueOf(rank)) == null) {
                return "it has no tier for rank " + rank;
            }
        }
        if (this.gates == null) {
            return "the gates block is missing";
        }
        if (this.spawn == null || this.spawn.recoveryTicks <= 0.0D) {
            return "the spawn block is missing or has no positive recoveryTicks";
        }
        return null;
    }
}
