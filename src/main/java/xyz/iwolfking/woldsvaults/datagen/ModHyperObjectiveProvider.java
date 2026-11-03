package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import xyz.iwolfking.woldsvaults.config.HyperObjectiveConfig;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * The HYPER objective: boss stat and rune scaling, spawn caps, chaos and wave pacing, the boss fight's health gates
 * and magic missiles, obelisk and elixir targets, score thresholds and the rare, epic and omega reward pools.
 */
public class ModHyperObjectiveProvider extends AbstractWoldsTableProvider {

    public ModHyperObjectiveProvider(DataGenerator generator) {
        super(generator, "objectives/hyper");
    }

    @Override
    public void registerConfigs() {
        HyperObjectiveConfig table = new HyperObjectiveConfig();
        table.hyperStatFactor = 1.85F;
        table.speedPerStack = 0.20F;
        table.bossHealthPercent = 50.0;
        table.bossDamagePercent = 50.0;
        table.bossStatIncrement = 15.0;
        table.bossAbilityHaste = 6;
        table.baseRuneTier = 40;
        table.runeTierCap = 40;
        table.playerScaleBossHealth = 0.5;

        table.spawnSoftCapThreshold = 2500.0;
        table.spawnSoftCapCoefficient = 15.0;
        table.spawnDensityCap = 1000;
        table.spawnDensityRadius = 48;

        table.chaosPerKill = 25;
        table.chaosCap = 350;
        table.ambientPeriodTicks = 20 * 120;

        table.wavePeriodTicks = 20 * 20;
        table.waveMobMin = 2;
        table.waveMobMax = 2;
        table.waveAliveCap = 6;
        table.healthGates = new float[]{0.8F, 0.6F, 0.4F, 0.2F};
        table.fightAddPeriodTicks = 20 * 6;
        table.magicMissileCooldownTicks = 20 * 15;
        table.magicMissileChargeTicks = 90;
        table.magicMissileCount = 3;
        table.magicMissileSpeed = 0.43;
        table.magicMissileTurnDegrees = 5.0;
        table.magicMissileLifetimeTicks = 20 * 6;
        table.magicMissileDamageMultiplier = 0.75;
        table.magicMissileAoeRadius = 2.0;
        table.magicMissileHoverTicks = 6;
        table.waveBlastDamageMultiplier = 3.0;

        table.exitPillarTicks = 20 * 15;
        table.winTransitionTicks = 20 * 15;

        table.obeliskMin = 2;
        table.obeliskMax = 5;
        table.brutalObeliskProbability = 0.6F;
        table.elixirTargetMultiplier = 0.75F;
        table.elixirTargetIncrement = 0.25F;
        table.cycleRequirementIncrement = 0.10;
        table.boardUpsizeCycle = 4;
        table.playerScaleElixir = 0.45;
        table.playerScaleBingo = 0.25;

        table.speedCapFactor = 2.5;

        table.referenceBossHealth = 388_000.0;
        table.referenceBossDamage = 1_460.0;
        table.scoreRare = 75_000;
        table.scoreEpic = 150_000;
        table.scoreOmega = 500_000;
        table.scoreDoubleGreedy = 1_000_000;
        table.scoreTripleGreedy = 5_000_000;
        table.scoreExtraDraw = 2_000_000;
        table.crateTierScore7 = 250_000;
        table.crateTierScore9 = 1_500_000;
        table.crateTierScore15 = 25_000_000;
        table.greedBonusTierEfficiency = 0.15F;
        table.greedyCoinBonusPerStack = 1.0F;

        table.rareRolls = 4;
        table.epicRolls = 3;
        table.omegaRolls = 3;
        table.rareRewards = new ArrayList<>(List.of(
                HyperObjectiveConfig.InjectionEntry.item("the_vault:repair_core", 1, 100),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:companion_reroller", 1, 25),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 1, 2),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:omega_box", 2, 10),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:map", 1, 10),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:recharge_core", 1, 50),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:fundamental_focus", 15, 40),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:opportunistic_focus", 10, 10),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:altar_recatalyzer", 2, 50),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:vault_platinum", 5, 40),
                HyperObjectiveConfig.InjectionEntry.etching(5)));
        table.epicRewards = new ArrayList<>(List.of(
                HyperObjectiveConfig.InjectionEntry.greedCoins(1, 1, 10),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:vault_palladium", 5, 30),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:nullite_crystal", 1, 5),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:map", 1, 30),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:chunk_of_power", 1, 1),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 1, 4),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:omega_box", 2, 5),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:evolution_pack", 10, 5),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:shiny_pack", 2, 5),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:unique_shard", 10, 20),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:greedy_ticket", 1, 5)));
        table.omegaRewards = new ArrayList<>(List.of(
                HyperObjectiveConfig.InjectionEntry.greedCoins(2, 3, 300),
                HyperObjectiveConfig.InjectionEntry.item("the_vault:mystic_pear", 1, 20),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:wold_star_chunk", 1, 10),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:chunk_of_power", 1, 40),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:nullite_crystal", 2, 50),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 2, 50),
                HyperObjectiveConfig.InjectionEntry.boosterPack("the_vault:shiny_pack", 5, 100),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:crystal_reinforcement", 1, 20),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:greedy_ticket", 3, 20),
                HyperObjectiveConfig.InjectionEntry.item("woldsvaults:core_of_the_vault_gods", 1, 1)));
        this.table("hyper_objective", table);
    }
}
