package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * What a greed medallion does to the vault it rides, generated as
 * {@code vault_configs/greed/medallions/greed_medallions.json}: the numeric columns of every tier, the rank each
 * assassin and Champion behaviour switches on at, the assassin fury curve, the assassin behaviour settings and the
 * completion crate coin settings. Tiers are keyed by rank number, from the first rank to Legend.
 */
public class GreedMedallionsConfig extends Config implements WoldsTable {

    /** One tier's vault-side numbers. Percent columns are whole percents. */
    public static class Tier {
        @Expose public int baseGreedCoins;
        @Expose public int assassinGreedCoins;
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
        @Expose public int omegaRoomWeightBonus;
        @Expose public String omegaPlusRollType;
        @Expose public int strongboxTier;

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

        /** Sets the greed coins one greed assassin drops at this rank. */
        public Tier assassinGreedCoins(int coins) {
            this.assassinGreedCoins = coins;
            return this;
        }

        /** Sets the percent added to the omega room weight at this rank. */
        public Tier omegaRoomWeightBonus(int percent) {
            this.omegaRoomWeightBonus = percent;
            return this;
        }

        /** Sets the gear roll type that replaces {@code Omega+} on gear made in this rank's vaults. */
        public Tier omegaPlusRollType(String rollType) {
            this.omegaPlusRollType = rollType;
            return this;
        }

        /** Sets the displayed strongbox tier of this rank's vaults, 0 for untiered strongboxes. */
        public Tier strongboxTier(int tier) {
            this.strongboxTier = tier;
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

    /** A potion effect by registry id and amplifier. */
    public static class Effect {
        @Expose public String effect;
        @Expose public int amplifier;

        public static Effect of(String effect, int amplifier) {
            Effect entry = new Effect();
            entry.effect = effect;
            entry.amplifier = amplifier;
            return entry;
        }
    }

    /**
     * What greed assassins do once spawned: where they land around the player, the debuff one of their melee hits
     * applies (one of {@code negativeEffects} at random), the aura pulse that buffs nearby vault mobs, and how many
     * infernal modifiers they draw.
     */
    public static class Assassins {
        @Expose public int spawnDistanceMin;
        @Expose public int spawnDistanceRange;
        @Expose public int negativeEffectTicks;
        @Expose public List<Effect> negativeEffects;
        @Expose public int auraPulseTicks;
        @Expose public double auraRadius;
        @Expose public int auraBuffTicks;
        @Expose public List<Effect> auraEffects;
        @Expose public int infernalModifierCount;
    }

    /**
     * Completion crate coins: the vault level a crate needs before greed coins and greed crate loot are added, and the
     * objective coin multipliers. Bingo objectives grow theirs by a factor per completed line.
     */
    public static class Crate {
        @Expose public int minimumVaultLevel;
        @Expose public double minorObjectiveMultiplier;
        @Expose public double majorObjectiveMultiplier;
        @Expose public double ballisticObjectiveMultiplier;
        @Expose public double doubleObjectiveMultiplier;
        @Expose public double bingoLineGrowth;
        @Expose public double ballisticLineGrowth;
    }

    @Expose public Map<String, Tier> tiers;
    @Expose public Gates gates;
    @Expose public Fury fury;
    @Expose public Assassins assassins;
    @Expose public Crate crate;

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
            String problem = validateTier(rank, this.tiers.get(String.valueOf(rank)));
            if (problem != null) {
                return problem;
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
        String assassinsProblem = validateAssassins(this.assassins);
        if (assassinsProblem != null) {
            return assassinsProblem;
        }
        Crate crate = this.crate;
        if (crate == null) {
            return "the crate block is missing";
        }
        if (crate.minorObjectiveMultiplier < 1.0D || crate.majorObjectiveMultiplier < 1.0D
                || crate.ballisticObjectiveMultiplier < 1.0D || crate.doubleObjectiveMultiplier < 1.0D
                || crate.bingoLineGrowth < 1.0D || crate.ballisticLineGrowth < 1.0D) {
            return "the crate block's objective multipliers and line growth factors must be at least 1";
        }
        return null;
    }

    @Nullable
    private static String validateTier(int rank, @Nullable Tier tier) {
        if (tier == null) {
            return "it has no tier for rank " + rank;
        }
        if (tier.assassinFuryGain <= 0.0D || tier.assassinSquadTarget < 1.0D || tier.assassinRecoverySeconds <= 0.0D) {
            return "rank " + rank + " needs a positive assassinFuryGain and assassinRecoverySeconds and an assassinSquadTarget of at least 1";
        }
        if (tier.baseGreedCoins < 0 || tier.assassinGreedCoins < 0 || tier.mobHealthDamageBonus <= -100) {
            return "rank " + rank + " needs non-negative greed coin columns and a mobHealthDamageBonus above -100";
        }
        if (tier.omegaRoomWeightBonus < 0 || tier.strongboxTier < 0) {
            return "rank " + rank + " needs a non-negative omegaRoomWeightBonus and strongboxTier";
        }
        if (tier.omegaPlusRollType == null || tier.omegaPlusRollType.isBlank()) {
            return "rank " + rank + " names no omegaPlusRollType";
        }
        return null;
    }

    @Nullable
    private static String validateAssassins(@Nullable Assassins assassins) {
        if (assassins == null) {
            return "the assassins block is missing";
        }
        if (assassins.spawnDistanceMin <= 0 || assassins.spawnDistanceRange < 0 || assassins.negativeEffectTicks <= 0
                || assassins.auraPulseTicks <= 0 || assassins.auraRadius <= 0.0D || assassins.auraBuffTicks <= 0
                || assassins.infernalModifierCount < 0) {
            return "the assassins block needs positive spawn distance, effect ticks, aura pulse, radius and buff ticks "
                    + "and a non-negative spawnDistanceRange and infernalModifierCount";
        }
        if (!validEffects(assassins.negativeEffects) || !validEffects(assassins.auraEffects)) {
            return "the assassins block needs non-empty negativeEffects and auraEffects lists of named effects with "
                    + "non-negative amplifiers";
        }
        return null;
    }

    private static boolean validEffects(@Nullable List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return false;
        }
        for (Effect entry : effects) {
            if (entry == null || entry.effect == null || entry.effect.isBlank() || entry.amplifier < 0) {
                return false;
            }
        }
        return true;
    }
}
