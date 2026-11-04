package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Every tuning knob of the HYPER objective, plus the three score-gated crate injection pools, generated as
 * {@code vault_configs/objectives/hyper/hyper_objective.json}.
 */
public class HyperObjectiveConfig extends Config implements WoldsTable {

    @Expose public float hyperStatFactor;
    @Expose public float speedPerStack;
    @Expose public double bossHealthPercent;
    @Expose public double bossDamagePercent;
    @Expose public double bossStatIncrement;
    @Expose public int bossAbilityHaste;
    @Expose public int baseRuneTier;
    @Expose public int runeTierCap;
    @Expose public double playerScaleBossHealth;


    @Expose public double spawnSoftCapThreshold;
    @Expose public double spawnSoftCapCoefficient;
    @Expose public int spawnDensityCap;
    @Expose public int spawnDensityRadius;

    @Expose public int chaosPerKill;
    @Expose public int chaosCap;
    @Expose public int ambientPeriodTicks;

    @Expose public int wavePeriodTicks;
    @Expose public int waveMobMin;
    @Expose public int waveMobMax;
    @Expose public int waveAliveCap;
    @Expose public float[] healthGates;
    @Expose public int fightAddPeriodTicks;
    @Expose public int magicMissileCooldownTicks;
    @Expose public int magicMissileChargeTicks;
    @Expose public int magicMissileCount;
    @Expose public double magicMissileSpeed;
    @Expose public double magicMissileTurnDegrees;
    @Expose public int magicMissileLifetimeTicks;
    @Expose public double magicMissileDamageMultiplier;
    @Expose public double magicMissileAoeRadius;
    @Expose public int magicMissileHoverTicks;
    @Expose public double waveBlastDamageMultiplier;

    @Expose public int exitPillarTicks;
    @Expose public int winTransitionTicks;

    @Expose public int obeliskMin;
    @Expose public int obeliskMax;
    @Expose public float brutalObeliskProbability;
    @Expose public float elixirTargetMultiplier;
    @Expose public float elixirTargetIncrement;
    @Expose public double cycleRequirementIncrement;
    @Expose public int boardUpsizeCycle;
    @Expose public double playerScaleElixir;
    @Expose public double playerScaleBingo;

    @Expose public double speedCapFactor;

    @Expose public double referenceBossHealth;
    @Expose public double referenceBossDamage;
    @Expose public int scoreRare;
    @Expose public int scoreEpic;
    @Expose public int scoreOmega;
    @Expose public int scoreDoubleGreedy;
    @Expose public int scoreTripleGreedy;
    @Expose public int scoreExtraDraw;
    @Expose public int crateTierScore7;
    @Expose public int crateTierScore9;
    @Expose public int crateTierScore15;
    @Expose public float greedBonusTierEfficiency;
    @Expose public float greedyCoinBonusPerStack;

    @Expose public int rareRolls;
    @Expose public int epicRolls;
    @Expose public int omegaRolls;
    @Expose public List<InjectionEntry> rareRewards;
    @Expose public List<InjectionEntry> epicRewards;
    @Expose public List<InjectionEntry> omegaRewards;

    @Override
    public String getName() {
        return "hyper_objective";
    }

    /** A config with every value zero and no rewards, for readers that run before the hyper table installs. */
    public static HyperObjectiveConfig inert() {
        HyperObjectiveConfig config = new HyperObjectiveConfig();
        config.healthGates = new float[0];
        config.rareRewards = new ArrayList<>();
        config.epicRewards = new ArrayList<>();
        config.omegaRewards = new ArrayList<>();
        return config;
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.healthGates == null || this.rareRewards == null || this.epicRewards == null || this.omegaRewards == null) {
            return "the health gates or a reward pool is missing";
        }
        return null;
    }

    public float getHyperStatFactor() {
        return this.hyperStatFactor;
    }

    public float getSpeedPerStack() {
        return this.speedPerStack;
    }

    public double getBossHealthPercent() {
        return this.bossHealthPercent;
    }

    public double getBossDamagePercent() {
        return this.bossDamagePercent;
    }

    public double getBossStatIncrement() {
        return this.bossStatIncrement;
    }

    public int getBossAbilityHaste() {
        return this.bossAbilityHaste;
    }

    public int getBaseRuneTier() {
        return this.baseRuneTier;
    }

    public int getRuneTierCap() {
        return this.runeTierCap;
    }

    public double getPlayerScaleBossHealth() {
        return this.playerScaleBossHealth;
    }

    public double getSpawnSoftCapThreshold() {
        return this.spawnSoftCapThreshold;
    }

    public double getSpawnSoftCapCoefficient() {
        return this.spawnSoftCapCoefficient;
    }

    public int getSpawnDensityCap() {
        return this.spawnDensityCap;
    }

    public int getSpawnDensityRadius() {
        return this.spawnDensityRadius;
    }

    public int getChaosPerKill() {
        return this.chaosPerKill;
    }

    public int getChaosCap() {
        return this.chaosCap;
    }

    public int getAmbientPeriodTicks() {
        return Math.max(1, this.ambientPeriodTicks);
    }

    public int getWavePeriodTicks() {
        return Math.max(1, this.wavePeriodTicks);
    }

    public int getWaveMobMin() {
        return this.waveMobMin;
    }

    public int getWaveMobMax() {
        return Math.max(this.waveMobMin, this.waveMobMax);
    }

    /** Guarded so a config written before this key existed reads as the shipped cap, not as zero. */
    public int getWaveAliveCap() {
        return this.waveAliveCap;
    }

    public float[] getHealthGates() {
        return this.healthGates == null ? new float[0] : this.healthGates;
    }

    public int getFightAddPeriodTicks() {
        return Math.max(1, this.fightAddPeriodTicks);
    }

    public int getMagicMissileCooldownTicks() {
        return this.magicMissileCooldownTicks;
    }

    public int getMagicMissileChargeTicks() {
        return this.magicMissileChargeTicks;
    }

    public int getMagicMissileCount() {
        return this.magicMissileCount;
    }

    public double getMagicMissileSpeed() {
        return this.magicMissileSpeed;
    }

    public double getMagicMissileTurnDegrees() {
        return this.magicMissileTurnDegrees;
    }

    public int getMagicMissileLifetimeTicks() {
        return this.magicMissileLifetimeTicks;
    }

    public double getMagicMissileDamageMultiplier() {
        return this.magicMissileDamageMultiplier;
    }

    public double getMagicMissileAoeRadius() {
        return this.magicMissileAoeRadius;
    }

    /** Hover 0 is a legal tuning (fire instantly), so this clamps instead of falling back. */
    public int getMagicMissileHoverTicks() {
        return Math.max(0, this.magicMissileHoverTicks);
    }

    public double getWaveBlastDamageMultiplier() {
        return this.waveBlastDamageMultiplier;
    }

    public int getExitPillarTicks() {
        return this.exitPillarTicks;
    }

    public int getWinTransitionTicks() {
        return this.winTransitionTicks;
    }

    public int getObeliskMin() {
        return this.obeliskMin;
    }

    public int getObeliskMax() {
        return Math.max(this.obeliskMin, this.obeliskMax);
    }

    public float getBrutalObeliskProbability() {
        return this.brutalObeliskProbability;
    }

    public float getElixirTargetMultiplier() {
        return this.elixirTargetMultiplier;
    }

    public float getElixirTargetIncrement() {
        return this.elixirTargetIncrement;
    }

    public double getCycleRequirementIncrement() {
        return this.cycleRequirementIncrement;
    }

    public int getBoardUpsizeCycle() {
        return this.boardUpsizeCycle;
    }

    public double getPlayerScaleElixir() {
        return this.playerScaleElixir;
    }

    public double getPlayerScaleBingo() {
        return this.playerScaleBingo;
    }

    public double getSpeedCapFactor() {
        return this.speedCapFactor;
    }

    public double getReferenceBossHealth() {
        return this.referenceBossHealth;
    }

    public double getReferenceBossDamage() {
        return this.referenceBossDamage;
    }

    public int getScoreRare() {
        return this.scoreRare;
    }

    public int getScoreEpic() {
        return this.scoreEpic;
    }

    public int getScoreOmega() {
        return this.scoreOmega;
    }

    public int getScoreDoubleGreedy() {
        return this.scoreDoubleGreedy;
    }

    public int getScoreTripleGreedy() {
        return this.scoreTripleGreedy;
    }

    public int getScoreExtraDraw() {
        return this.scoreExtraDraw;
    }

    public int getCrateTierScore7() {
        return this.crateTierScore7;
    }

    public int getCrateTierScore9() {
        return this.crateTierScore9;
    }

    public int getCrateTierScore15() {
        return this.crateTierScore15;
    }

    public float getGreedBonusTierEfficiency() {
        return this.greedBonusTierEfficiency;
    }

    public float getGreedyCoinBonusPerStack() {
        return this.greedyCoinBonusPerStack;
    }

    public int getRareRolls() {
        return this.rareRolls;
    }

    public int getEpicRolls() {
        return this.epicRolls;
    }

    public int getOmegaRolls() {
        return this.omegaRolls;
    }

    public WeightedList<InjectionEntry> getRarePool() {
        return toWeightedList(this.rareRewards);
    }

    public WeightedList<InjectionEntry> getEpicPool() {
        return toWeightedList(this.epicRewards);
    }

    public WeightedList<InjectionEntry> getOmegaPool() {
        return toWeightedList(this.omegaRewards);
    }

    private static WeightedList<InjectionEntry> toWeightedList(List<InjectionEntry> entries) {
        WeightedList<InjectionEntry> list = new WeightedList<>();
        if (entries != null) {
            for (InjectionEntry entry : entries) {
                if (entry != null && entry.getWeight() > 0) {
                    list.add(entry, entry.getWeight());
                }
            }
        }
        return list;
    }

    /**
     * One weighted injection-pool line. Plain lines are item+count; the specials are a booster
     * pack (item + "id" NBT), a greater deck core (deck_socket + Modifier/ModifierRoll NBT),
     * greed coins (count x the receiving player's greed tier) or a random identified etching.
     */
    public static class InjectionEntry {
        @Expose private String item;
        @Expose private String boosterPack;
        @Expose private String deckCore;
        @Expose private int min;
        @Expose private int max;
        @Expose private boolean greedCoins;
        @Expose private boolean randomEtching;
        @Expose private int weight;

        public InjectionEntry(String item, String boosterPack, String deckCore, int min, int max,
                              boolean greedCoins, boolean randomEtching, int weight) {
            this.item = item;
            this.boosterPack = boosterPack;
            this.deckCore = deckCore;
            this.min = min;
            this.max = max;
            this.greedCoins = greedCoins;
            this.randomEtching = randomEtching;
            this.weight = weight;
        }

        public static InjectionEntry item(String itemId, int count, int weight) {
            return new InjectionEntry(itemId, null, null, count, count, false, false, weight);
        }

        public static InjectionEntry boosterPack(String boosterPackId, int count, int weight) {
            return new InjectionEntry("the_vault:booster_pack", boosterPackId, null, count, count, false, false, weight);
        }

        public static InjectionEntry greedCoins(int min, int max, int weight) {
            return new InjectionEntry("the_vault:greed_coin", null, null, min, max, true, false, weight);
        }

        /** The greater variant of one deck core (an id from config/the_vault/card/deck_modifiers.json). */
        public static InjectionEntry greaterDeckCore(String deckCoreId, int weight) {
            return new InjectionEntry("the_vault:deck_socket", null, deckCoreId, 1, 1, false, false, weight);
        }

        /** A random identified etching, rolled at the receiving player's greed tier. */
        public static InjectionEntry etching(int weight) {
            return new InjectionEntry("the_vault:etching", null, null, 1, 1, false, true, weight);
        }

        public String getItem() {
            return this.item;
        }

        public String getBoosterPack() {
            return this.boosterPack;
        }

        public String getDeckCore() {
            return this.deckCore;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return Math.max(this.min, this.max);
        }

        public boolean isGreedCoins() {
            return this.greedCoins;
        }

        public boolean isRandomEtching() {
            return this.randomEtching;
        }

        public int getWeight() {
            return this.weight;
        }
    }
}
