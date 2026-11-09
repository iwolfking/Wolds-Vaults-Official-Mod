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
 * assassin and Champion behaviour switches on at, and the assassin fury curve. Tiers are keyed by rank number,
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
        @Expose public double assassinFuryGain;
        @Expose public double assassinSquadTarget;
        @Expose public double assassinRecoverySeconds;

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

        /** Sets this rank's assassin fury multiplier, squad target (in assassins) and slowdown recovery time. */
        public Tier assassins(double furyGain, double squadTarget, double recoverySeconds) {
            this.assassinFuryGain = furyGain;
            this.assassinSquadTarget = squadTarget;
            this.assassinRecoverySeconds = recoverySeconds;
            return this;
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

    /**
     * The assassin fury curve. A kill adds {@code weight * rankGain * boost / (1 + slowdownPerAssassin * debt)} fury,
     * where {@code boost = 1 + boostAmount / (1 + (pace / boostKneeRate)^boostExponent)} and pace is the player's raw
     * fury per minute averaged over {@code rateWindowSeconds}. At {@code threshold} fury a roll every
     * {@code rollIntervalTicks} arms a squad with chance {@code 1 - exp(-rollRate * x^rollShape)}, where {@code x} is the
     * larger of fury over {@code squadTarget * threshold} and the seconds waited over {@code timeoutSeconds}. The next
     * kill spawns {@code floor(fury / threshold)} assassins, at most {@code maxSquad}, spending one threshold each.
     */
    public static class Fury {
        @Expose public double hordeKill;
        @Expose public double assassinKill;
        @Expose public double tankKill;
        @Expose public double eliteKill;
        @Expose public double threshold;
        @Expose public int rollIntervalTicks;
        @Expose public double rollRate;
        @Expose public double rollShape;
        @Expose public double timeoutSeconds;
        @Expose public int maxSquad;
        @Expose public double slowdownPerAssassin;
        @Expose public double boostAmount;
        @Expose public double boostKneeRate;
        @Expose public double boostExponent;
        @Expose public double rateWindowSeconds;
    }

    @Expose public Map<String, Tier> tiers;
    @Expose public Gates gates;
    @Expose public Fury fury;

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
            Tier tier = this.tiers.get(String.valueOf(rank));
            if (tier == null) {
                return "it has no tier for rank " + rank;
            }
            if (tier.assassinFuryGain <= 0.0D || tier.assassinSquadTarget < 1.0D || tier.assassinRecoverySeconds <= 0.0D) {
                return "rank " + rank + " needs a positive assassinFuryGain and assassinRecoverySeconds and an assassinSquadTarget of at least 1";
            }
        }
        if (this.gates == null) {
            return "the gates block is missing";
        }
        Fury fury = this.fury;
        if (fury == null) {
            return "the fury block is missing";
        }
        if (fury.threshold <= 0.0D || fury.rollIntervalTicks <= 0 || fury.timeoutSeconds <= 0.0D || fury.maxSquad < 1
                || fury.boostKneeRate <= 0.0D || fury.rateWindowSeconds <= 0.0D) {
            return "the fury block needs a positive threshold, rollIntervalTicks, timeoutSeconds, boostKneeRate and "
                    + "rateWindowSeconds and a maxSquad of at least 1";
        }
        return null;
    }
}
