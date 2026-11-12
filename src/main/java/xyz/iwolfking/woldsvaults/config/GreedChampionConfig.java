package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;

import javax.annotation.Nullable;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything the Vault Champion is made of, generated as {@code vault_configs/greed/champion/greed_champion.json}. It is a
 * re-used {@code TheVesselEntity}, so this table is applied at spawn, keyed by the summoning medallion.
 */
public class GreedChampionConfig extends Config implements WoldsTable {

    /** One rank's stat block. Health is absolute; the rest are multipliers. */
    public static class Rank {
        @Expose public double healthPool;
        @Expose public double damageMultiplier;
        @Expose public double movementSpeedMultiplier;
        @Expose public double attackSpeedMultiplier;

        public static Rank of(double healthPool, double damageMultiplier,
                       double movementSpeedMultiplier, double attackSpeedMultiplier) {
            Rank rank = new Rank();
            rank.healthPool = healthPool;
            rank.damageMultiplier = damageMultiplier;
            rank.movementSpeedMultiplier = movementSpeedMultiplier;
            rank.attackSpeedMultiplier = attackSpeedMultiplier;
            return rank;
        }
    }

    public static class Scaling {
        @Expose public double baseAttackDamage;
        @Expose public int levelAnchor;
        @Expose public double movementSpeedCap;
        @Expose public boolean scaleTeleportCooldown;
        @Expose public boolean inheritVaultModifiers;
        @Expose public float sizeMultiplier;
    }

    /** Where the Champion's health bar sits, relative to the vault objective's own HUD. */
    public static class Hud {
        @Expose public int offsetX;
        @Expose public int offsetY;
    }

    public static class Hunt {
        @Expose public double aggroRadius;
        @Expose public double leashDistance;
        @Expose public double spawnRingMax;
        @Expose public int dormantTicks;
        @Expose public int orphanTicks;
        @Expose public int managerIntervalTicks;
    }

    /** The three out-of-position conditions that charge a forced lightning storm. */
    public static class Lightning {
        @Expose public double aboveBlocks;
        @Expose public double belowBlocks;
        @Expose public double farBlocks;
        @Expose public int chargeTicks;
    }

    /** The irreducible second hit both the Champion and greed assassins land; still dodgeable and blockable. */
    public static class TrueDamage {
        @Expose public float champion;
        @Expose public float assassin;
    }

    /** The effect-piercing aura carried by Champions and assassins alike. */
    public static class Aura {
        @Expose public double radius;
        @Expose public boolean assassinsCarryIt;
    }

    /** Rage accrual, decay, and the arm-then-fire spawn roll. */
    public static class Rage {
        @Expose public double decayPerTick;
        @Expose public double chestLooted;
        @Expose public double oreMined;
        @Expose public double hordeKill;
        @Expose public double assassinKill;
        @Expose public double tankKill;
        @Expose public double eliteChampionMultiplier;
        @Expose public double baseThreshold;
        @Expose public double thresholdMultiplierPerKill;
        @Expose public int rollIntervalTicks;
        @Expose public double baseChance;
        @Expose public double legendBaseChance;
    }

    /** What a defeated Champion pays, per medallion rank index. */
    public static class Rewards {
        @Expose public int greedCoinsPerRank;
        @Expose public int greedyTicketsPerRank;
    }

    @Expose public Map<String, Rank> ranks;
    @Expose public Scaling scaling;
    @Expose public Hunt hunt;
    @Expose public Hud hud;
    @Expose public Lightning lightning;
    @Expose public TrueDamage trueDamage;
    @Expose public Aura aura;
    @Expose public Rage rage;
    @Expose public Rewards rewards;

    /** A config with every block present and zeroed and no ranks: no Champion can spawn on it. */
    public static GreedChampionConfig inert() {
        GreedChampionConfig config = new GreedChampionConfig();
        config.ranks = new LinkedHashMap<>();
        config.scaling = new Scaling();
        config.hunt = new Hunt();
        config.hud = new Hud();
        config.lightning = new Lightning();
        config.trueDamage = new TrueDamage();
        config.aura = new Aura();
        config.rage = new Rage();
        config.rewards = new Rewards();
        return config;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_champion";
    }

    @Nullable
    @Override
    public String validate() {
        if (this.ranks == null || this.ranks.isEmpty()) {
            return "it lists no ranks";
        }
        if (this.scaling == null || this.hunt == null || this.hud == null || this.lightning == null
                || this.trueDamage == null || this.aura == null || this.rage == null || this.rewards == null) {
            return "a block the spawner reads is missing";
        }
        return null;
    }

    public Rank getRank(int rankIndex) {
        return this.ranks == null ? null : this.ranks.get(String.valueOf(rankIndex));
    }

    public Scaling getScaling() {
        return this.scaling;
    }

    public Hunt getHunt() {
        return this.hunt;
    }

    public Hud getHud() {
        return this.hud;
    }

    public Lightning getLightning() {
        return this.lightning;
    }

    public TrueDamage getTrueDamage() {
        return this.trueDamage;
    }

    public Aura getAura() {
        return this.aura;
    }

    public Rage getRage() {
        return this.rage;
    }

    public Rewards getRewards() {
        return this.rewards;
    }

    @Override
    protected void reset() {
    }
}
