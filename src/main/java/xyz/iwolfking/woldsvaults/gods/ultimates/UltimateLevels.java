package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The four per-level ultimate tables from the {@code God Ultimates} sheet, plus the single seam
 * that decides which level an ultimate casts at.
 *
 * <p>Ultimate level is deliberately <em>not</em> the ability's {@code TieredSkill} tier. Every
 * ultimate ships as a one-tier skill and reads its numbers from here instead, which is what makes
 * the design rule "ability-boosting nodes do not affect god ultimates" true without a mixin on the
 * hot {@code TieredSkill.updateBonusTier} path: gear that grants {@code all_abilities} levels can
 * raise {@code bonusTier} all it likes and {@code getChild} still clamps to the only tier there is.
 *
 * <p>Levels run 1 to {@value #MAX_LEVEL}: the sheet lists 1-8 by hand and then a per-level
 * increment, and every ability in this pack caps at 30. No source of ultimate levels exists yet,
 * so the default provider returns {@link #DEFAULT_LEVEL} for everyone; when one arrives it feeds
 * in through {@link #setLevelProvider(LevelProvider)} and nothing else changes.
 */
public final class UltimateLevels {
    public static final int DEFAULT_LEVEL = 1;
    public static final int MAX_LEVEL = 30;
    public static final int COOLDOWN_TICKS = 6000;

    private static final int EYES_RADIUS_CAP = 9;

    private static volatile LevelProvider levelProvider = (player, god) -> DEFAULT_LEVEL;

    private UltimateLevels() {
    }

    /**
     * Replaces the ultimate-level source. The default returns {@link #DEFAULT_LEVEL} for every
     * player and god; whatever eventually grants ultimate levels installs the real one.
     */
    public static void setLevelProvider(LevelProvider provider) {
        if (provider == null) {
            WoldsVaults.LOGGER.error("Refusing to install a null ultimate level provider; keeping the current one.");
            return;
        }
        levelProvider = provider;
    }

    public static int resolveLevel(ServerPlayer player, VaultGod god) {
        int level = levelProvider.getUltimateLevel(player, god);
        if (level < 1) {
            WoldsVaults.LOGGER.error("Ultimate level provider returned {} for {} / {}; falling back to level {}.",
                    level, player == null ? "null" : player.getGameProfile().getName(), god, DEFAULT_LEVEL);
            return DEFAULT_LEVEL;
        }
        if (level > MAX_LEVEL) {
            WoldsVaults.LOGGER.error("Ultimate level provider returned {} for {} / {}, above the cap of {}; clamping.",
                    level, player == null ? "null" : player.getGameProfile().getName(), god, MAX_LEVEL);
            return MAX_LEVEL;
        }
        return level;
    }

    /**
     * The level the ability screen renders its numbers at. Descriptions are built on the client
     * from the ability instance alone, which carries no player, so this is deliberately separate
     * from {@link #resolveLevel(ServerPlayer, VaultGod)}. While every cast is
     * {@link #DEFAULT_LEVEL} the two agree; a real level source has to sync the cast level onto
     * the ability before the description can follow it.
     */
    public static int displayLevel() {
        return DEFAULT_LEVEL;
    }

    public static Cope cope(int level) {
        int steps = level - 1;
        float multiplier = 1.5F + 0.05F * steps;
        return new Cope(
                multiplier,
                multiplier,
                Math.min(0.5F + 0.05F * steps, 0.85F),
                0.05F + 0.01F * steps,
                300);
    }

    public static Savior savior(int level) {
        int steps = level - 1;
        float amount = 100.0F + 50.0F * steps;
        return new Savior(
                amount,
                amount,
                Math.min(0.20F + 0.05F * steps, 0.55F),
                Math.min(100 + 20 * steps, 240));
    }

    /**
     * Radius 5 at levels 1-3, 6 at 4-6, 7 at 7-9, then one more every three levels. The whole ladder
     * sits one above the {@code God Ultimates} sheet, which starts at 4 - a deliberate buff, and the
     * cap moved with it. The sheet's "Level+ 0.33 (rounded down)" is unbounded; it is capped at
     * {@value #EYES_RADIUS_CAP} here because the reveal resolves every region in a
     * {@code (2r+1)^2} block in a single tick.
     */
    public static int eyesRadius(int level) {
        return Math.min(5 + (level - 1) / 3, EYES_RADIUS_CAP);
    }

    public static BulletTime bulletTime(int level) {
        int steps = level - 1;
        return new BulletTime(
                0.50F + 0.10F * steps,
                Math.min(0.70F + 0.02F * steps, 0.85F),
                200 + 10 * steps,
                0.20F + 0.05F * steps,
                0.20F + 0.05F * steps);
    }

    /** Supplies the level an ultimate casts at, per player and per god. */
    @FunctionalInterface
    public interface LevelProvider {
        int getUltimateLevel(ServerPlayer player, VaultGod god);
    }

    /**
     * @param abilityPowerMultiplier ability-power multiplier for the infusion
     * @param attackDamageMultiplier attack-damage multiplier for the infusion
     * @param resistance             own-source damage reduction, multiplicative with armour and resistance
     * @param compoundingPerHit      additive stack added per landed hit, and the dash's share of the accumulator
     * @param durationTicks          infusion length, 15 s at every level
     */
    public record Cope(float abilityPowerMultiplier, float attackDamageMultiplier, float resistance,
                       float compoundingPerHit, int durationTicks) {
    }

    public record Savior(float healing, float absorption, float resistance, int durationTicks) {
    }

    /**
     * @param timerStretch the sheet's "vault timer stretch"; applied as a clock-rate factor of
     *                     {@code 1 + stretch}, i.e. the {@code 1/(1+s)} speed reading, so a stretch
     *                     at or above 100% slows the clock instead of freezing or reversing it
     */
    public record BulletTime(float timerStretch, float dodgeChance, int durationTicks, float attackSpeed,
                             float movementSpeed) {
    }
}
