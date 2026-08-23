package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything the Vault Champion is made of - {@code config/the_vault/gods/greed_champion.json}. It is a
 * re-used {@code TheVesselEntity}, so this table is applied at spawn, keyed by the summoning medallion.
 */
public class GreedChampionConfig extends PackAuthoredConfig {

    /** One rank's stat block. Health is absolute; the rest are multipliers. */
    public static class Rank {
        @Expose public double healthPool;
        @Expose public double damageMultiplier;
        @Expose public double movementSpeedMultiplier;
        @Expose public double attackSpeedMultiplier;

        static Rank of(double healthPool, double damageMultiplier,
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

    @Expose private Map<String, String> documentation;
    @Expose private Map<String, Rank> ranks;
    @Expose private Scaling scaling;
    @Expose private Hunt hunt;
    @Expose private Hud hud;
    @Expose private Lightning lightning;
    @Expose private TrueDamage trueDamage;
    @Expose private Aura aura;
    @Expose private Rage rage;
    @Expose private Rewards rewards;

    /** The shipped table without touching disk, for the fallback path and for datagen. */
    public static GreedChampionConfig defaults() {
        GreedChampionConfig config = new GreedChampionConfig();
        config.reset();
        return config;
    }

    @Override
    public String getName() {
        return "gods" + File.separator + "greed_champion";
    }

    /** Rejects a file that does not describe every block the spawner reads. */
    @Override
    public boolean isValid() {
        return this.ranks != null && !this.ranks.isEmpty() && this.scaling != null && this.hunt != null
                && this.hud != null && this.lightning != null && this.trueDamage != null
                && this.aura != null && this.rage != null && this.rewards != null;
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
        this.documentation = new LinkedHashMap<>();
        this.documentation.put("ranks", "One entry per medallion tier that can summon a Champion, keyed by the tier's rank index - this is the medallion in the vault, never the player's own greed rank, so anyone running a qualifying medallion vault can meet one. Tiers with no entry never spawn one; as shipped that is every medallion below Master 1 (10)");
        this.documentation.put("healthPool", "Damage the Champion must absorb before it is defeated, at the level anchor and before difficulty, medallion and vault modifiers. It is a damage pool, not entity health: the Vessel cannot be killed, so emptying the pool discards it");
        this.documentation.put("damageMultiplier", "Multiplier on baseAttackDamage. The Vessel's own time ramp (1.2x/min for five minutes then 1.35x/min) applies on top");
        this.documentation.put("movementSpeedMultiplier", "Multiplier on the Vessel's 0.25 base movement speed, then clamped to movementSpeedCap");
        this.documentation.put("attackSpeedMultiplier", "Divisor on every attack cooldown. 2.0 halves the gap between swings. Animation lengths are untouched");
        this.documentation.put("baseAttackDamage", "The Vessel's authored attack damage, scaled by vault level over levelAnchor before the rank multiplier");
        this.documentation.put("levelAnchor", "Vault level at which healthPool and baseAttackDamage are quoted verbatim. Crystals clamp to level 100 internally, so this is normally the ceiling too");
        this.documentation.put("movementSpeedCap", "Hard ceiling on effective movement speed. Vanilla pathfinding degrades badly above roughly 1.0; the leash teleport closes distance instead");
        this.documentation.put("scaleTeleportCooldown", "Whether attackSpeedMultiplier also shortens the 200-tick teleport cooldown. Off by default: scaling it puts the grab-and-stun on a very short cycle at Legend");
        this.documentation.put("inheritVaultModifiers", "Replay the crystal's mob attribute modifiers onto the Champion. Max-health modifiers are excluded regardless, to keep the pool from compounding into float overflow");
        this.documentation.put("sizeMultiplier", "Scales the Champion's model and hitbox through the base mod's entity-scale capability, the same one the Grow effect uses. 1.0 is the Vessel's own size - a 0.9 x 3.5 hitbox drawn at the renderer's fixed 1.5x model scale. Clamped by that capability to 8.0");
        this.documentation.put("hud", "Pixel offset of the health bar from the vault objective's HUD, which the bar hangs underneath. Following that module rather than the screen means the bar tracks it wherever the player has dragged and rescaled it; offsetY is just the gap below");
        this.documentation.put("aggroRadius", "How far the Champion will acquire a target on its own");
        this.documentation.put("leashDistance", "Beyond this the Champion teleports in front of its quarry, ignoring every cooldown. This is what makes the hunt inescapable");
        this.documentation.put("spawnRingMax", "Fallback search radius. The Champion lands on the player; this is only how far out it looks when there is no room to stand there");
        this.documentation.put("dormantTicks", "How long the Champion stands inert after spawning before it activates");
        this.documentation.put("orphanTicks", "How long a Champion with no runner left in the vault survives before it is discarded");
        this.documentation.put("managerIntervalTicks", "How often the server re-binds the hunt target, drags the Champion back onto its quarry, decays rage and pushes the health bar");
        this.documentation.put("lightning", "Being out of position for chargeTicks forces a lightning storm. One shared counter: above, below or far all charge the same one");
        this.documentation.put("trueDamage", "Flat damage landed as a second event on every melee hit. Nothing reduces it - armor, resistance, gear and god nodes are all bypassed or floored out - but it can still be dodged, blocked or refused by an immunity like any other hit. Deliberately does not scale with anything");
        this.documentation.put("aura", "Inside this radius of a Champion or assassin, players cannot avoid harmful effects. Time Acceleration is exempt");
        this.documentation.put("rage", "Per-player, per-vault. Rises on the listed actions, decays every tick. Once rage reaches the threshold a roll every rollIntervalTicks can ARM a spawn, which then fires on the player's next rage-increasing action");
        this.documentation.put("rollIntervalTicks", "How often that arming roll is made. Independent of managerIntervalTicks: this is the whole spawn cadence, so halving it doubles how often a Champion turns up");
        this.documentation.put("baseThreshold", "Rage needed before rolls begin. Multiplied by thresholdMultiplierPerKill for each Champion this player has already defeated this vault");
        this.documentation.put("rewards", "Multiplied by the medallion's rank index. Rank 10 (Master 1) with the shipped values pays 50 greed coins and 20 tickets");

        this.ranks = new LinkedHashMap<>();
        this.ranks.put("10", Rank.of(2_000_000.0D, 1.0D, 1.5D, 2.0D));
        this.ranks.put("11", Rank.of(2_000_000.0D, 2.0D, 1.8D, 2.4D));
        this.ranks.put("12", Rank.of(2_000_000.0D, 4.0D, 2.1D, 2.8D));
        this.ranks.put("13", Rank.of(10_000_000.0D, 8.0D, 2.5D, 3.3D));
        this.ranks.put("14", Rank.of(10_000_000.0D, 15.0D, 3.0D, 3.9D));
        this.ranks.put("15", Rank.of(10_000_000.0D, 30.0D, 3.5D, 4.4D));
        this.ranks.put("16", Rank.of(100_000_000.0D, 100.0D, 4.0D, 5.0D));

        this.scaling = new Scaling();
        this.scaling.baseAttackDamage = 24.0D;
        this.scaling.levelAnchor = 100;
        this.scaling.movementSpeedCap = 1.0D;
        this.scaling.scaleTeleportCooldown = false;
        this.scaling.inheritVaultModifiers = true;
        this.scaling.sizeMultiplier = 1.5F;

        this.hud = new Hud();
        this.hud.offsetX = 0;
        this.hud.offsetY = 30;

        this.hunt = new Hunt();
        this.hunt.aggroRadius = 40.0D;
        this.hunt.leashDistance = 35.0D;
        this.hunt.spawnRingMax = 8.0D;
        this.hunt.dormantTicks = 40;
        this.hunt.orphanTicks = 200;
        this.hunt.managerIntervalTicks = 20;

        this.lightning = new Lightning();
        this.lightning.aboveBlocks = 1.5D;
        this.lightning.belowBlocks = 4.0D;
        this.lightning.farBlocks = 25.0D;
        this.lightning.chargeTicks = 40;

        this.trueDamage = new TrueDamage();
        this.trueDamage.champion = 15.0F;
        this.trueDamage.assassin = 10.0F;

        this.aura = new Aura();
        this.aura.radius = 10.0D;
        this.aura.assassinsCarryIt = true;

        this.rage = new Rage();
        this.rage.decayPerTick = 6.0D;
        this.rage.chestLooted = 200.0D;
        this.rage.oreMined = 200.0D;
        this.rage.hordeKill = 100.0D;
        this.rage.assassinKill = 300.0D;
        this.rage.tankKill = 700.0D;
        this.rage.eliteChampionMultiplier = 2.0D;
        this.rage.baseThreshold = 100_000.0D;
        this.rage.thresholdMultiplierPerKill = 4.0D;
        this.rage.rollIntervalTicks = 20;
        this.rage.baseChance = 0.001D;
        this.rage.legendBaseChance = 0.0025D;

        this.rewards = new Rewards();
        this.rewards.greedCoinsPerRank = 5;
        this.rewards.greedyTicketsPerRank = 2;
    }
}
