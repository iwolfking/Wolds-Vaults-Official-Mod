package xyz.iwolfking.woldsvaults.gods.ultimates;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * The four per-level ultimate tables and the seam that decides which level an ultimate casts at. Ultimate
 * level is not the ability's {@code TieredSkill} tier: every ultimate ships as a one-tier skill and reads
 * its numbers from here, so ability-boosting nodes do not raise it. Levels run 1 to {@value #MAX_LEVEL},
 * and with no level source installed the default provider returns {@link #DEFAULT_LEVEL} for everyone.
 */
public final class UltimateLevels {
    public static final int DEFAULT_LEVEL = 1;
    public static final int MAX_LEVEL = 30;
    public static final int COOLDOWN_TICKS = 6000;

    private static final int EYES_RADIUS_CAP = 9;

    private static volatile LevelProvider levelProvider = (player, god) -> DEFAULT_LEVEL;

    private UltimateLevels() {
    }

    /** Replaces the ultimate-level source; a null provider is refused and logged. */
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
     * The level the ability screen renders at, separate from {@link #resolveLevel(ServerPlayer, VaultGod)} because
     * client descriptions carry no player.
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
     * Radius 5 at levels 1-3, then one more every three levels, capped at {@value #EYES_RADIUS_CAP}; the ladder
     * sits one above the sheet, whose own value is uncapped.
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

    @FunctionalInterface
    public interface LevelProvider {
        int getUltimateLevel(ServerPlayer player, VaultGod god);
    }

    /**
     * @param resistance        own-source damage reduction, multiplicative with armour and resistance
     * @param compoundingPerHit additive stack added per landed hit, and the dash's share of the accumulator
     */
    public record Cope(float abilityPowerMultiplier, float attackDamageMultiplier, float resistance,
                       float compoundingPerHit, int durationTicks) {
    }

    public record Savior(float healing, float absorption, float resistance, int durationTicks) {
    }

    /** @param timerStretch applied as a clock-rate factor of {@code 1 + stretch} */
    public record BulletTime(float timerStretch, float dodgeChance, int durationTicks, float attackSpeed,
                             float movementSpeed) {
    }
}
