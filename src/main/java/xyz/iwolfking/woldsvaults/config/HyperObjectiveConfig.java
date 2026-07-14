package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every tuning knob of the HYPER objective (config/the_vault/hyper_objective.json), plus the
 * three score-gated crate injection pools. The {@code documentation} block at the top of the
 * JSON is generated from {@link #reset()} and describes each knob in one line — it is data for
 * humans only and never read back.
 *
 * <p>The defaults below are the shipped balance (playtest rounds 1-44); a pack can retune any
 * of it without a mod rebuild.
 */
public class HyperObjectiveConfig extends Config {
    @Expose private Map<String, String> documentation;

    @Expose private float hyperStatFactor;
    @Expose private float speedPerStack;
    @Expose private double bossHealthPercent;
    @Expose private double bossDamagePercent;
    @Expose private double bossStatIncrement;
    @Expose private int bossAbilityHaste;
    @Expose private int baseRuneTier;
    @Expose private int runeTierCap;
    @Expose private double playerScaleBossHealth;

    @Expose private int chaosPerKill;
    @Expose private int chaosCap;
    @Expose private int ambientPeriodTicks;

    @Expose private int wavePeriodTicks;
    @Expose private int waveMobMin;
    @Expose private int waveMobMax;
    @Expose private float[] healthGates;
    @Expose private int fightAddPeriodTicks;

    @Expose private int exitPillarTicks;
    @Expose private int winTransitionTicks;

    @Expose private int obeliskMin;
    @Expose private int obeliskMax;
    @Expose private float brutalObeliskProbability;
    @Expose private float elixirTargetMultiplier;
    @Expose private float elixirTargetIncrement;
    @Expose private double cycleRequirementIncrement;
    @Expose private int boardUpsizeCycle;
    @Expose private double playerScaleElixir;
    @Expose private double playerScaleBingo;

    @Expose private double speedCapFactor;

    @Expose private double referenceBossHealth;
    @Expose private double referenceBossDamage;
    @Expose private int scoreRare;
    @Expose private int scoreEpic;
    @Expose private int scoreOmega;
    @Expose private int scoreDoubleGreedy;
    @Expose private int scoreTripleGreedy;
    @Expose private int scoreExtraDraw;
    @Expose private int crateTierScore7;
    @Expose private int crateTierScore9;
    @Expose private int crateTierScore15;
    @Expose private float greedBonusTierEfficiency;
    @Expose private float greedyCoinBonusPerStack;

    @Expose private int rareRolls;
    @Expose private int epicRolls;
    @Expose private int omegaRolls;
    @Expose private List<InjectionEntry> rareRewards;
    @Expose private List<InjectionEntry> epicRewards;
    @Expose private List<InjectionEntry> omegaRewards;

    @Override
    public String getName() {
        return "hyper_objective";
    }

    /**
     * Shipped defaults (the playtested balance). Injection-pool weights ride the pack's x10
     * convention — everything is scaled x10 so outliers can go rarer while staying integer;
     * the greater archive core is x2 (~5x rarer relative) and the God's Mastery crafting core
     * is the rarest line in the omega pool.
     */
    @Override
    protected void reset() {
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("hyperStatFactor", "Per-cycle compounding factor for mob health/damage and the boss escalation");
        this.documentation.put("speedPerStack", "Additive mob movement speed per HYPER stack (0.2 = +20%)");
        this.documentation.put("bossHealthPercent", "Boss base escalation as a fraction of base health (50 = +5000%), multiplied by hyperStatFactor^cycle");
        this.documentation.put("bossDamagePercent", "Boss base damage escalation, same shape as bossHealthPercent");
        this.documentation.put("bossStatIncrement", "Boss-exclusive linear bonus added per cycle on top of the exponential (15 = +1500%/cycle)");
        this.documentation.put("bossAbilityHaste", "Rune 'haste points' for the boss's shield/waveblast abilities (vanilla rune roll is 0-3)");
        this.documentation.put("baseRuneTier", "Rune count at cycle 0 (keys the shield/waveblast settings table)");
        this.documentation.put("runeTierCap", "Rune count ceiling so deep cycles cannot index past the config");
        this.documentation.put("playerScaleBossHealth", "Extra boss max health per EXTRA runner (0.5 = duo x1.5, trio x2.0); excluded from the loot score");
        this.documentation.put("chaosPerKill", "Chaos modifiers dumped per boss kill (and once when the vault starts)");
        this.documentation.put("chaosCap", "Total chaos budget for the whole vault (dumps, ambient events and brutal kills all draw from it)");
        this.documentation.put("ambientPeriodTicks", "Ticks between ambient negative modifier pulls (one per runner per period)");
        this.documentation.put("wavePeriodTicks", "Ticks between timed brutal waves during the boss fight");
        this.documentation.put("waveMobMin/waveMobMax", "Brutal bosses per wave");
        this.documentation.put("healthGates", "Boss health fractions that each trigger one extra brutal wave (order = persisted gate bits; do not reorder mid-vault)");
        this.documentation.put("fightAddPeriodTicks", "Ticks between lone tank/assassin arena adds during the fight");
        this.documentation.put("exitPillarTicks", "Lifetime of the post-kill exit pillar");
        this.documentation.put("winTransitionTicks", "Personal victory countdown after clicking the exit pillar");
        this.documentation.put("obeliskMin/obeliskMax", "Brutal pillar count range; the floor rises +1 per 3 kills and +1 per 2 extra runners");
        this.documentation.put("brutalObeliskProbability", "Per-room chance the brutal mini claims a room's objective anchor");
        this.documentation.put("elixirTargetMultiplier/elixirTargetIncrement", "Elixir target = base x (multiplier + increment x cycle)");
        this.documentation.put("cycleRequirementIncrement", "Card requirements grow this fraction of base per boss kill");
        this.documentation.put("boardUpsizeCycle", "Cycle at which bingo/collector boards grow to 5x5");
        this.documentation.put("playerScaleElixir/playerScaleBingo", "Requirement scale per EXTRA runner (the collector keeps VH's native +50%)");
        this.documentation.put("speedCapFactor", "Final mob movement speed is capped at base x this (2.5 = +150%)");
        this.documentation.put("referenceBossHealth/referenceBossDamage", "Boogeyman level-100 bases; scores apply the killed boss's multipliers to these so every roster boss scores identically");
        this.documentation.put("scoreRare/scoreEpic/scoreOmega", "Kill scores that add one rare/epic/omega crate-injection marker (non-exclusive)");
        this.documentation.put("scoreDoubleGreedy/scoreTripleGreedy", "Kill scores that raise the greedy crate tiers granted per kill to 2 / 3");
        this.documentation.put("scoreExtraDraw", "Epic/omega markers earned at or above this score roll one extra draw per set");
        this.documentation.put("crateTierScore7/9/15", "Kill k grants 5k regular crate tiers, 7k/9k/15k once the kill scores past these");
        this.documentation.put("greedBonusTierEfficiency", "The per-crate greed bonus rolls at crate quantity x this in hyper vaults (coins excluded)");
        this.documentation.put("greedyCoinBonusPerStack", "Extra greed coins in the completion crate per greedy crate tier stack (1.0 = +100%)");
        this.documentation.put("rareRolls/epicRolls/omegaRolls", "Draws per marker stack from each injection pool");
        this.documentation.put("rareRewards/epicRewards/omegaRewards", "The injection pools: item+count(+weight), or a boosterPack/deckCore/greedCoins/randomEtching special");

        this.hyperStatFactor = 1.75F;
        this.speedPerStack = 0.20F;
        this.bossHealthPercent = 50.0;
        this.bossDamagePercent = 50.0;
        this.bossStatIncrement = 15.0;
        this.bossAbilityHaste = 6;
        this.baseRuneTier = 3;
        this.runeTierCap = 10;
        this.playerScaleBossHealth = 0.5;

        this.chaosPerKill = 25;
        this.chaosCap = 350;
        this.ambientPeriodTicks = 20 * 120;

        this.wavePeriodTicks = 20 * 20;
        this.waveMobMin = 2;
        this.waveMobMax = 4;
        this.healthGates = new float[]{0.8F, 0.6F, 0.4F, 0.2F};
        this.fightAddPeriodTicks = 20 * 6;

        this.exitPillarTicks = 20 * 15;
        this.winTransitionTicks = 20 * 15;

        this.obeliskMin = 2;
        this.obeliskMax = 5;
        this.brutalObeliskProbability = 0.6F;
        this.elixirTargetMultiplier = 0.75F;
        this.elixirTargetIncrement = 0.25F;
        this.cycleRequirementIncrement = 0.10;
        this.boardUpsizeCycle = 4;
        this.playerScaleElixir = 0.45;
        this.playerScaleBingo = 0.25;

        this.speedCapFactor = 2.5;

        this.referenceBossHealth = 388_000.0;
        this.referenceBossDamage = 1_460.0;
        this.scoreRare = 75_000;
        this.scoreEpic = 150_000;
        this.scoreOmega = 500_000;
        this.scoreDoubleGreedy = 1_000_000;
        this.scoreTripleGreedy = 5_000_000;
        this.scoreExtraDraw = 2_000_000;
        this.crateTierScore7 = 250_000;
        this.crateTierScore9 = 1_500_000;
        this.crateTierScore15 = 25_000_000;
        this.greedBonusTierEfficiency = 0.15F;
        this.greedyCoinBonusPerStack = 1.0F;

        this.rareRolls = 4;
        this.epicRolls = 3;
        this.omegaRolls = 3;
        this.rareRewards = new ArrayList<>(List.of(
                InjectionEntry.item("the_vault:repair_core", 1, 100),
                InjectionEntry.item("vending_companions:companion_temporalizer", 1, 25),
                InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 1, 2),
                InjectionEntry.item("woldsvaults:omega_box", 2, 10),
                InjectionEntry.item("the_vault:map", 1, 10),
                InjectionEntry.item("woldsvaults:legend_sigil", 1, 5),
                InjectionEntry.item("the_vault:recharge_core", 1, 50),
                InjectionEntry.item("the_vault:fundamental_focus", 15, 40),
                InjectionEntry.item("the_vault:opportunistic_focus", 10, 10),
                InjectionEntry.item("woldsvaults:altar_recatalyzer", 2, 50),
                InjectionEntry.item("the_vault:vault_platinum", 5, 40),
                InjectionEntry.etching(5)));
        this.epicRewards = new ArrayList<>(List.of(
                InjectionEntry.greedCoins(1, 1, 10),
                InjectionEntry.item("the_vault:vault_palladium", 5, 30),
                InjectionEntry.item("woldsvaults:nullite_crystal", 1, 5),
                InjectionEntry.item("the_vault:map", 1, 30),
                InjectionEntry.item("woldsvaults:chunk_of_power", 1, 1),
                InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 1, 4),
                InjectionEntry.item("woldsvaults:omega_box", 2, 5),
                InjectionEntry.boosterPack("the_vault:evolution_pack", 10, 5),
                InjectionEntry.boosterPack("the_vault:shiny_pack", 2, 5),
                InjectionEntry.item("the_vault:unique_shard", 10, 20),
                InjectionEntry.item("woldsvaults:greedy_ticket", 1, 5),
                InjectionEntry.greaterDeckCore("pure", 2),
                InjectionEntry.greaterDeckCore("arcane", 2),
                InjectionEntry.greaterDeckCore("construction", 2),
                InjectionEntry.greaterDeckCore("shiny", 2),
                InjectionEntry.greaterDeckCore("void", 2)));
        this.omegaRewards = new ArrayList<>(List.of(
                InjectionEntry.greedCoins(2, 3, 300),
                InjectionEntry.item("the_vault:mystic_pear", 1, 20),
                InjectionEntry.item("woldsvaults:wold_star_chunk", 1, 10),
                InjectionEntry.item("woldsvaults:chunk_of_power", 1, 40),
                InjectionEntry.item("woldsvaults:nullite_crystal", 2, 50),
                InjectionEntry.boosterPack("the_vault:deluxe_resource_pack", 2, 50),
                InjectionEntry.boosterPack("the_vault:shiny_pack", 5, 100),
                InjectionEntry.item("woldsvaults:crystal_reinforcement", 1, 20),
                InjectionEntry.greaterDeckCore("archive", 2),
                InjectionEntry.item("woldsvaults:greedy_ticket", 3, 20),
                InjectionEntry.item("woldsvaults:core_of_the_vault_gods", 1, 1)));
    }

    public float getHyperStatFactor() {
        return this.hyperStatFactor <= 0.0F ? 1.75F : this.hyperStatFactor;
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

    public float[] getHealthGates() {
        return this.healthGates == null ? new float[0] : this.healthGates;
    }

    public int getFightAddPeriodTicks() {
        return Math.max(1, this.fightAddPeriodTicks);
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

        static InjectionEntry item(String itemId, int count, int weight) {
            return new InjectionEntry(itemId, null, null, count, count, false, false, weight);
        }

        static InjectionEntry boosterPack(String boosterPackId, int count, int weight) {
            return new InjectionEntry("the_vault:booster_pack", boosterPackId, null, count, count, false, false, weight);
        }

        static InjectionEntry greedCoins(int min, int max, int weight) {
            return new InjectionEntry("the_vault:greed_coin", null, null, min, max, true, false, weight);
        }

        /** The greater variant of one deck core (an id from config/the_vault/card/deck_modifiers.json). */
        static InjectionEntry greaterDeckCore(String deckCoreId, int weight) {
            return new InjectionEntry("the_vault:deck_socket", null, deckCoreId, 1, 1, false, false, weight);
        }

        /** A random identified etching, rolled at the receiving player's greed tier. */
        static InjectionEntry etching(int weight) {
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
