package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig;
import xyz.iwolfking.woldsvaults.config.GreedMedallionsConfig.Tier;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRankLadder;

import java.util.LinkedHashMap;

/**
 * What each greed medallion does to its vault. Tier columns, in order: base greed coins, mob health and damage
 * bonus, completion crate loot bonus, extra greed crate table, tiered chest roll bonus, trap disarm penalty, mob
 * population bonus and objective difficulty bonus, all whole percents except the coins and the crate table index.
 * Each tier also carries its assassin fury multiplier and squad target; its slowdown recovery time is
 * {@link #LEGEND_RECOVERY_SECONDS} scaled by the Legend multiplier over its own, so every rank's long-run spawn rate
 * stays proportional to its multiplier.
 */
public class ModGreedMedallionsProvider extends AbstractWoldsTableProvider {

    private static final double LEGEND_RECOVERY_SECONDS = 60.0D;
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
        table.tiers.put("1", Tier.of(2, 25, 0, 0, 0, 0, 0, 0));
        table.tiers.put("2", Tier.of(3, 45, 5, 0, 0, 0, 0, 0));
        table.tiers.put("3", Tier.of(3, 120, 5, 1, 0, 0, 0, 0));
        table.tiers.put("4", Tier.of(4, 180, 10, 1, 0, 0, 0, 0));
        table.tiers.put("5", Tier.of(5, 250, 15, 1, 10, 50, 33, 0));
        table.tiers.put("6", Tier.of(5, 300, 20, 2, 10, 75, 33, 0));
        table.tiers.put("7", Tier.of(6, 675, 25, 2, 15, 100, 33, 0));
        table.tiers.put("8", Tier.of(7, 900, 25, 3, 15, 125, 50, 25));
        table.tiers.put("9", Tier.of(8, 1200, 30, 4, 20, 175, 50, 25));
        table.tiers.put("10", Tier.of(9, 1650, 35, 4, 20, 175, 50, 25));
        table.tiers.put("11", Tier.of(10, 2100, 35, 4, 25, 200, 60, 35));
        table.tiers.put("12", Tier.of(11, 2850, 40, 5, 25, 200, 60, 35));
        table.tiers.put("13", Tier.of(12, 6250, 40, 5, 30, 250, 60, 40));
        table.tiers.put("14", Tier.of(13, 8000, 45, 6, 30, 275, 65, 45));
        table.tiers.put("15", Tier.of(14, 10000, 45, 6, 35, 300, 70, 45));
        table.tiers.put("16", Tier.of(15, 12500, 50, 7, 40, 350, 75, 50));

        table.gates = new GreedMedallionsConfig.Gates();
        table.gates.assassinsInheritVaultModifiers = MilestoneRankLadder.LOOTER_1;
        table.gates.assassinsInflictNegativeEffects = MilestoneRankLadder.HUNTER_1;
        table.gates.assassinsGainBuffingAuras = MilestoneRankLadder.HUNTER_3;
        table.gates.assassinsGainInfernalModifiers = MilestoneRankLadder.CHAMPION_1;
        table.gates.vaultChampionHunts = MilestoneRankLadder.MASTER_1;
        table.gates.vaultChampionEnraged = MilestoneRankLadder.LEGEND;

        double legendGain = ASSASSIN_FURY_GAIN[ASSASSIN_FURY_GAIN.length - 1];
        for (int i = 0; i < ASSASSIN_FURY_GAIN.length; i++) {
            table.tiers.get(String.valueOf(i + 1)).assassins(ASSASSIN_FURY_GAIN[i], ASSASSIN_SQUAD_TARGET[i],
                    LEGEND_RECOVERY_SECONDS * legendGain / ASSASSIN_FURY_GAIN[i]);
        }

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
        table.tiers.forEach((rank, tier) -> {
            if (tier.greedCrateLootTier > GreedCrateLootTables.TIERS) {
                throw new IllegalStateException("Medallion rank " + rank + " rolls greed crate loot table " + tier.greedCrateLootTier
                        + ", but only " + GreedCrateLootTables.TIERS + " are generated");
            }
        });
        this.table("greed_medallions", table);
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
