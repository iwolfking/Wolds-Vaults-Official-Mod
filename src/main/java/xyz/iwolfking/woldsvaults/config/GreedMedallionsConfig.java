package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;

import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a greed medallion does to the vault it rides - {@code config/the_vault/gods/greed_medallions.json}.
 * Holds the eight numeric columns of every medallion tier, the rank each assassin behaviour and
 * deferred Vault Champion behaviour switches on at, and the assassin spawn-rate curve.
 *
 * <p>{@link xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier} is the only reader; the enum
 * keeps the sixteen tiers as identities - registry paths, textures, the NBT int riding the crystal -
 * and takes every number from here, the same split {@code MilestoneRankLadder} has with
 * {@code greed_ranks.json}.
 *
 * <p>Tiers are keyed by rank number, matching {@code godLevelGates} in {@code greed_ranks.json},
 * because a rank's band name is itself config and so cannot be a stable key. Rank 1 is the first
 * medallion (Scavenger 1 as shipped) and rank 16 the last (Legend).
 */
public class GreedMedallionsConfig extends PackAuthoredConfig {

    /** One tier's vault-side numbers. Percent columns are whole percents; 25 is +25%. */
    public static class Tier {
        @Expose public int baseGreedCoins;
        @Expose public int mobHealthDamageBonus;
        @Expose public int crateLootBonus;
        @Expose public int greedCrateLootTier;
        @Expose public int chestRollsBonus;
        @Expose public int trapDisarmPenalty;
        @Expose public int mobSpawnBonus;
        @Expose public int objectiveDifficultyBonus;

        static Tier of(int baseGreedCoins, int mobHealthDamageBonus, int crateLootBonus, int greedCrateLootTier,
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

    /** The assassin spawn-rate curve. Deliberately separable from the tier table it scales. */
    public static class Spawn {
        @Expose public double hordeBaseChance;
        @Expose public double eliteBaseChance;
        @Expose public double rankChanceStep;
        @Expose public double decayFactor;
        @Expose public double recoveryTicks;
    }

    @Expose private Map<String, String> documentation;
    @Expose private Map<String, Tier> tiers;
    @Expose private Gates gates;
    @Expose private Spawn spawn;

    /**
     * A config instance holding the shipped table without touching the disk. The medallion items
     * are registered from the tier enum during mod construction, long before configs are read, so
     * the enum has to have numbers to run on from its own class init.
     */
    public static GreedMedallionsConfig defaults() {
        GreedMedallionsConfig config = new GreedMedallionsConfig();
        config.reset();
        return config;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_medallions";
    }

    @Override
    protected void reset() {
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("tiers", "One entry per medallion, keyed by the greed rank it belongs to. Rank 1 is the first medallion and rank 16 the last; the band names come from greed_ranks.json");
        this.documentation.put("baseGreedCoins", "Greed coins an assassin killed in this medallion's vault pays, and the per-crate base the completion crate pays");
        this.documentation.put("mobHealthDamageBonus", "Percent added to vault mob health and damage. Multiplicative with every other source");
        this.documentation.put("crateLootBonus", "Percent added to the item count of completion crates");
        this.documentation.put("greedCrateLootTier", "Which greed_crate_loot_N table the completion crate additionally rolls. 0 rolls none");
        this.documentation.put("chestRollsBonus", "Percent added to the roll count of tiered chests. Barrels and treasure chests are not tiered and are unaffected");
        this.documentation.put("trapDisarmPenalty", "Percent subtracted from trap disarming. A magnitude, so 33 means -33%");
        this.documentation.put("mobSpawnBonus", "Percent added to the sustained vault mob population ceiling");
        this.documentation.put("objectiveDifficultyBonus", "Percent added to objective difficulty. Reaches brutal boss pillars and bosses only, the one numeric difficulty axis in the game");
        this.documentation.put("gates", "The medallion rank each behaviour switches on at. Vault Champion behaviours are tabled but not implemented");
        this.documentation.put("spawn", "Assassin spawn chance per attempt is base * (1 + rankChanceStep * (rank - 1)), decaying by decayFactor per spawn and recovering over recoveryTicks");
        this.tiers = new LinkedHashMap<>();
        this.tiers.put("1", Tier.of(2, 25, 0, 0, 0, 0, 0, 0));
        this.tiers.put("2", Tier.of(3, 45, 5, 0, 0, 0, 0, 0));
        this.tiers.put("3", Tier.of(3, 60, 5, 1, 0, 0, 0, 0));
        this.tiers.put("4", Tier.of(4, 90, 10, 1, 0, 0, 0, 0));
        this.tiers.put("5", Tier.of(5, 125, 15, 1, 10, 50, 33, 0));
        this.tiers.put("6", Tier.of(5, 150, 20, 2, 10, 75, 33, 0));
        this.tiers.put("7", Tier.of(6, 225, 25, 2, 15, 100, 33, 0));
        this.tiers.put("8", Tier.of(7, 300, 25, 3, 15, 125, 50, 25));
        this.tiers.put("9", Tier.of(8, 400, 30, 4, 20, 175, 50, 25));
        this.tiers.put("10", Tier.of(9, 550, 35, 4, 20, 175, 50, 25));
        this.tiers.put("11", Tier.of(10, 700, 35, 4, 25, 200, 60, 35));
        this.tiers.put("12", Tier.of(11, 950, 40, 5, 25, 200, 60, 35));
        this.tiers.put("13", Tier.of(12, 1250, 40, 5, 30, 250, 60, 40));
        this.tiers.put("14", Tier.of(13, 1600, 45, 6, 30, 275, 65, 45));
        this.tiers.put("15", Tier.of(14, 2000, 45, 6, 35, 300, 70, 45));
        this.tiers.put("16", Tier.of(15, 2500, 50, 7, 40, 350, 75, 50));
        this.gates = new Gates();
        this.gates.assassinsInheritVaultModifiers = 4;
        this.gates.assassinsInflictNegativeEffects = 7;
        this.gates.assassinsGainBuffingAuras = 9;
        this.gates.assassinsGainInfernalModifiers = 13;
        this.gates.vaultChampionHunts = 10;
        this.gates.vaultChampionEnraged = 16;
        this.spawn = new Spawn();
        this.spawn.hordeBaseChance = 2.0E-4D;
        this.spawn.eliteBaseChance = 0.01D;
        this.spawn.rankChanceStep = 0.15D;
        this.spawn.decayFactor = 0.5D;
        this.spawn.recoveryTicks = 6000.0D;
    }

    /**
     * A tier table missing a rank would leave that medallion silently doing nothing, so the whole
     * file is refused and the shipped table used instead rather than half of it applied.
     */
    @Override
    protected boolean isValid() {
        return this.tiers != null && !this.tiers.isEmpty() && this.gates != null && this.spawn != null;
    }

    public Map<String, Tier> getTiers() {
        return this.tiers;
    }

    @Nullable
    public Gates getGates() {
        return this.gates;
    }

    @Nullable
    public Spawn getSpawn() {
        return this.spawn;
    }
}
