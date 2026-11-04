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
 */
public class ModGreedMedallionsProvider extends AbstractWoldsTableProvider {

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

        table.spawn = new GreedMedallionsConfig.Spawn();
        table.spawn.hordeBaseChance = 2.0E-4D;
        table.spawn.eliteBaseChance = 0.01D;
        table.spawn.rankChanceStep = 0.15D;
        table.spawn.decayFactor = 0.5D;
        table.spawn.recoveryTicks = 6000.0D;
        table.tiers.forEach((rank, tier) -> {
            if (tier.greedCrateLootTier > GreedCrateLootTables.TIERS) {
                throw new IllegalStateException("Medallion rank " + rank + " rolls greed crate loot table " + tier.greedCrateLootTier
                        + ", but only " + GreedCrateLootTables.TIERS + " are generated");
            }
        });
        this.table("greed_medallions", table);
    }
}
