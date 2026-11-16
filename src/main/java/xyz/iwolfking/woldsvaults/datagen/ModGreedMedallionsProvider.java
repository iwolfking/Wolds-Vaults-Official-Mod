package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig.Effect;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig.Tier;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * What each greed medallion does to its vault, one row per rank. The mob health and damage multiplier rises linearly
 * through Scavenger and Looter, cubically from Hunter 1 to Champion 3 and jumps at Legend; crate coins rise linearly from
 * {@link #FIRST_CRATE_COINS} to {@link #LEGEND_CRATE_COINS}. Each rank's assassin slowdown recovery time is
 * {@link #LEGEND_RECOVERY_SECONDS} scaled by the Legend fury multiplier over its own, so every rank's long-run spawn
 * rate stays proportional to its multiplier.
 */
public class ModGreedMedallionsProvider extends AbstractWoldsTableProvider {

    private static final double LEGEND_RECOVERY_SECONDS = 60.0D;
    private static final int FIRST_CRATE_COINS = 2;
    private static final int LEGEND_CRATE_COINS = 25;
    private static final double SCAVENGER_MOB_MULTIPLIER = 1.5D;
    private static final double SCAVENGER_MOB_STEP = 0.75D;
    private static final double LOOTER_MOB_MULTIPLIER = 4.0D;
    private static final double LOOTER_MOB_STEP = 2.0D;
    private static final double HUNTER_MOB_MULTIPLIER = 10.0D;
    private static final double CHAMPION_3_MOB_MULTIPLIER = 2000.0D;
    private static final double LEGEND_MOB_MULTIPLIER = 5000.0D;
    private static final String OMEGA_PLUS = "Omega+";
    private static final String OMEGA_PLUS_HIGH = "Omega+ High";
    private static final int OMEGA_PLUS_HIGH_RANK = MilestoneRankLadder.HUNTER_2;
    private static final int FIRST_STRONGBOX_RANK = MilestoneRankLadder.LOOTER_3;
    private static final int MAX_STRONGBOX_TIER = 6;

    private static final int[] ASSASSIN_GREED_COINS = {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7};
    private static final int[] CRATE_LOOT_BONUS = {0, 5, 5, 10, 15, 20, 25, 25, 30, 35, 35, 40, 40, 45, 45, 50};
    private static final int[] GREED_CRATE_LOOT_TIER = {0, 0, 1, 1, 1, 2, 2, 3, 4, 4, 4, 5, 5, 6, 6, 7};
    private static final int[] CHEST_ROLLS_BONUS = {0, 0, 0, 0, 10, 10, 15, 15, 20, 20, 25, 25, 30, 30, 35, 40};
    private static final int[] TRAP_DISARM_PENALTY = {0, 0, 0, 0, 50, 75, 100, 125, 175, 175, 200, 200, 250, 275, 300, 350};
    private static final int[] MOB_SPAWN_BONUS = {0, 0, 0, 0, 17, 17, 17, 25, 25, 25, 30, 30, 30, 33, 35, 38};
    private static final int[] OBJECTIVE_DIFFICULTY_BONUS = {0, 0, 0, 0, 0, 0, 0, 25, 25, 25, 35, 35, 40, 45, 45, 50};
    private static final int[] OMEGA_ROOM_WEIGHT_BONUS = {0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 20, 30, 40, 50, 60, 100};
    private static final double[] ASSASSIN_FURY_GAIN = {
            1.2D, 1.27D, 1.35D, 1.45D, 1.55D, 1.65D, 1.85D, 1.95D, 2.05D, 3.0D, 3.2D, 3.4D, 4.6D, 4.9D, 5.2D, 6.0D};
    private static final double[] ASSASSIN_SQUAD_TARGET = {
            1.0D, 1.0D, 1.0D, 1.5D, 1.5D, 1.5D, 2.5D, 2.5D, 2.5D, 3.5D, 3.5D, 3.5D, 4.5D, 4.5D, 4.5D, 6.0D};

    public ModGreedMedallionsProvider(DataGenerator generator) {
        super(generator, "greed/medallions");
    }

    @Override
    public void registerConfigs() {
        GreedMedallionsConfig table = new GreedMedallionsConfig();
        table.tiers = new LinkedHashMap<>();
        double legendGain = ASSASSIN_FURY_GAIN[ASSASSIN_FURY_GAIN.length - 1];
        for (int rank = MilestoneRankLadder.FIRST_RANK; rank <= MilestoneRankLadder.LEGEND_RANK; rank++) {
            int i = rank - MilestoneRankLadder.FIRST_RANK;
            Tier tier = Tier.of(crateCoins(rank), mobHealthDamageBonus(rank), CRATE_LOOT_BONUS[i], GREED_CRATE_LOOT_TIER[i],
                            CHEST_ROLLS_BONUS[i], TRAP_DISARM_PENALTY[i], MOB_SPAWN_BONUS[i], OBJECTIVE_DIFFICULTY_BONUS[i])
                    .assassinGreedCoins(ASSASSIN_GREED_COINS[i])
                    .assassins(ASSASSIN_FURY_GAIN[i], ASSASSIN_SQUAD_TARGET[i],
                            LEGEND_RECOVERY_SECONDS * legendGain / ASSASSIN_FURY_GAIN[i])
                    .omegaRoomWeightBonus(OMEGA_ROOM_WEIGHT_BONUS[i])
                    .omegaPlusRollType(rank >= OMEGA_PLUS_HIGH_RANK ? OMEGA_PLUS_HIGH : OMEGA_PLUS)
                    .strongboxTier(strongboxTier(rank));
            table.tiers.put(String.valueOf(rank), tier);
        }

        table.gates = new GreedMedallionsConfig.Gates();
        table.gates.assassinsInheritVaultModifiers = MilestoneRankLadder.LOOTER_1;
        table.gates.assassinsInflictNegativeEffects = MilestoneRankLadder.HUNTER_1;
        table.gates.assassinsGainBuffingAuras = MilestoneRankLadder.HUNTER_3;
        table.gates.assassinsGainInfernalModifiers = MilestoneRankLadder.CHAMPION_1;
        table.gates.vaultChampionHunts = MilestoneRankLadder.MASTER_1;
        table.gates.vaultChampionEnraged = MilestoneRankLadder.LEGEND;

        table.fury = new GreedMedallionsConfig.Fury();
        table.fury.hordeKill = 1.0D;
        table.fury.assassinKill = 5.0D;
        table.fury.tankKill = 10.0D;
        table.fury.eliteKill = 30.0D;
        table.fury.threshold = 5300.0D;
        table.fury.rollIntervalTicks = 20;
        table.fury.rollRate = 0.3D;
        table.fury.rollShape = 12.0D;
        table.fury.timeoutSeconds = 180.0D;
        table.fury.maxSquad = 8;
        table.fury.slowdownPerAssassin = 0.05D;
        table.fury.boostAmount = 2.0D;
        table.fury.boostKneeRate = 1000.0D;
        table.fury.boostExponent = 2.0D;
        table.fury.rateWindowSeconds = 60.0D;
        requireMonotoneBoost(table.fury);

        table.assassins = new GreedMedallionsConfig.Assassins();
        table.assassins.spawnDistanceMin = 24;
        table.assassins.spawnDistanceRange = 8;
        table.assassins.negativeEffectTicks = 100;
        table.assassins.negativeEffects = List.of(
                Effect.of("minecraft:wither", 0),
                Effect.of("minecraft:slowness", 0),
                Effect.of("minecraft:weakness", 0),
                Effect.of("minecraft:poison", 1));
        table.assassins.auraPulseTicks = 100;
        table.assassins.auraRadius = 8.0D;
        table.assassins.auraBuffTicks = 120;
        table.assassins.auraEffects = List.of(
                Effect.of("minecraft:strength", 0),
                Effect.of("minecraft:speed", 0));
        table.assassins.infernalModifierCount = 2;

        table.crate = new GreedMedallionsConfig.Crate();
        table.crate.minimumVaultLevel = 100;
        table.crate.minorObjectiveMultiplier = 1.2D;
        table.crate.majorObjectiveMultiplier = 1.5D;
        table.crate.ballisticObjectiveMultiplier = 1.6D;
        table.crate.doubleObjectiveMultiplier = 2.0D;
        table.crate.bingoLineGrowth = 1.025D;
        table.crate.ballisticLineGrowth = 1.03D;

        table.tiers.forEach((rank, tier) -> {
            if (tier.greedCrateLootTier > GreedCrateLootTables.TIERS) {
                throw new IllegalStateException("Medallion rank " + rank + " rolls greed crate loot table " + tier.greedCrateLootTier
                        + ", but only " + GreedCrateLootTables.TIERS + " are generated");
            }
        });
        this.table("greed_medallions", table);
    }

    /** Completion crate coins: linear from the first rank to Legend, rounded to a whole coin. */
    private static int crateCoins(int rank) {
        double share = (rank - MilestoneRankLadder.FIRST_RANK)
                / (double) (MilestoneRankLadder.LEGEND_RANK - MilestoneRankLadder.FIRST_RANK);
        return (int) Math.round(FIRST_CRATE_COINS + (LEGEND_CRATE_COINS - FIRST_CRATE_COINS) * share);
    }

    /** The mob health and damage multiplier as the table's whole-percent bonus, {@code 100 (f - 1)}. */
    private static int mobHealthDamageBonus(int rank) {
        return (int) Math.round(100.0D * (mobMultiplier(rank) - 1.0D));
    }

    private static double mobMultiplier(int rank) {
        if (rank <= MilestoneRankLadder.SCAVENGER_3) {
            return SCAVENGER_MOB_MULTIPLIER + SCAVENGER_MOB_STEP * (rank - MilestoneRankLadder.SCAVENGER_1);
        }
        if (rank <= MilestoneRankLadder.LOOTER_3) {
            return LOOTER_MOB_MULTIPLIER + LOOTER_MOB_STEP * (rank - MilestoneRankLadder.LOOTER_1);
        }
        if (rank <= MilestoneRankLadder.CHAMPION_3) {
            double share = (rank - MilestoneRankLadder.HUNTER_1)
                    / (double) (MilestoneRankLadder.CHAMPION_3 - MilestoneRankLadder.HUNTER_1);
            return HUNTER_MOB_MULTIPLIER + (CHAMPION_3_MOB_MULTIPLIER - HUNTER_MOB_MULTIPLIER) * Math.pow(share, 3.0D);
        }
        return LEGEND_MOB_MULTIPLIER;
    }

    /** Tier 1 at the first strongbox rank, one more per rank after it, capped at {@link #MAX_STRONGBOX_TIER}. */
    private static int strongboxTier(int rank) {
        if (rank < FIRST_STRONGBOX_RANK) {
            return 0;
        }
        return Math.min(MAX_STRONGBOX_TIER, rank - FIRST_STRONGBOX_RANK + 1);
    }

    /**
     * Refuses a boost curve under which killing faster could earn less fury: pace times boost must rise with pace.
     */
    private static void requireMonotoneBoost(GreedMedallionsConfig.Fury fury) {
        double previous = 0.0D;
        for (int step = 1; step <= 100_000; step++) {
            double pace = fury.boostKneeRate * step / 1000.0D;
            double effective = pace * (1.0D + fury.boostAmount / (1.0D + Math.pow(pace / fury.boostKneeRate, fury.boostExponent)));
            if (effective <= previous) {
                throw new IllegalStateException("Assassin fury boost is not monotone: effective fury falls at pace " + pace
                        + " fury/min; lower boostAmount or boostExponent");
            }
            previous = effective;
        }
    }
}
