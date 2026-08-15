package xyz.iwolfking.woldsvaults.gods.trees.velara;

/**
 * Every tuned number of the Velara tree, transcribed from the God Tree Nodes sheet.
 *
 * <p>Two-value stat nodes are two-point nodes: the arrays hold the total value at one point and
 * at two points, not a per-point increment, so a two-point Tough is +50% health rather than +75%.
 */
public final class VelaraValues {
    public static final float[] TOUGH_HEALTH_PERCENT = {0.25F, 0.50F};
    public static final float[] ARMORED_ARMOR_PERCENT = {0.25F, 0.50F};
    public static final float[] IMMUNE_AVOIDANCE = {0.05F, 0.10F};
    public static final float[] HEALTHY_HEALING = {0.20F, 0.40F};
    public static final float[] FAST_REFLEXES_DODGE = {0.02F, 0.05F};
    public static final float[] GUARDED_BLOCK = {0.02F, 0.05F};
    public static final float[] THORNY_FLAT = {25.0F, 50.0F};
    public static final int PIOUS_DEVOTION_PIETY_PER_POINT = 10;

    public static final float COUNTERSTRIKE_CHANCE = 0.5F;
    public static final float MAGIC_ARMOR_EFFICIENCY = 0.5F;
    public static final float DEFENDER_PER_CHARM = 0.05F;
    public static final float PERSERVERENCE_TIMER_BONUS = 0.5F;
    public static final float ADAPTIVE_ARMOR_PER_STACK = 0.10F;
    public static final int ADAPTIVE_ARMOR_MAX_STACKS = 6;
    public static final float BOUNCE_BACK_MULTIPLIER = 2.0F;
    public static final float BOUNCE_BACK_HEALTH_THRESHOLD = 0.10F;
    public static final int INDOMITABLE_REGENERATION_LEVELS = 3;
    public static final float FIELD_MEDIC_MULTIPLIER = 1.5F;
    public static final float STONEWALL_SPEED_MULTIPLIER = 0.33F;
    public static final float STONEWALL_ARMOR_MULTIPLIER = 1.5F;
    public static final float CACTUS_ARMOR_MULTIPLIER = 0.33F;
    public static final float CACTUS_THORNS_MULTIPLIER = 2.0F;
    public static final float MALEDICTION_FORCED_HEALING = 0.5F;
    public static final int FLEETING_IMMUNE_TICKS = 200;
    public static final int FLEETING_VULNERABLE_TICKS = 400;
    public static final int FLEETING_CYCLE_TICKS = FLEETING_IMMUNE_TICKS + FLEETING_VULNERABLE_TICKS;
    public static final float FLEETING_DAMAGE_MULTIPLIER = 3.0F;
    public static final float STEADFAST_KNOCKBACK_FLOOR = 1.0F;
    public static final float STEADFAST_ARMOR_PER_EXCESS = 1.0F;
    public static final float SANITATION_RADIUS = 50.0F;
    public static final int SANITATION_DURATION_DIVISOR = 3;
    public static final float PRESENCE_RADIUS = 100.0F;
    public static final float PRESENCE_RESISTANCE = 0.10F;
    public static final float PRESENCE_HEALING = 1.00F;
    public static final int PRESENCE_REGENERATION_LEVELS = 2;
    public static final float HEALING_FLOW_PER_MANA_REGEN = 0.01F / 0.25F;
    public static final int UTILIZED_ABILITY_LEVELS = 3;

    public static final float SACRIFICE_SYPHON = 0.66F;
    public static final float SACRIFICE_RESISTANCE = 0.33F;

    public static final float IMMORTAL_HEALTH_MULTIPLIER = 1.33F;
    public static final float IMMORTAL_ARMOR_MULTIPLIER = 1.33F;
    public static final float IMMORTAL_HEALING_MULTIPLIER = 1.5F;
    public static final float IMMORTAL_FLAT_HEALTH = 20.0F;
    public static final float IMMORTAL_FLAT_ARMOR = 50.0F;
    public static final int IMMORTAL_REGENERATION_LEVELS = 10;
    public static final float IMMORTAL_DAMAGE_MULTIPLIER = 0.5F;
    public static final int IMMORTAL_REVIVE_COOLDOWN_TICKS = 6000;

    private VelaraValues() {
    }

    /** The value of a ranked stat node at {@code points} invested; 0 when nothing is invested. */
    public static float atRank(float[] table, int points) {
        if (points <= 0) {
            return 0.0F;
        }
        return table[Math.min(points, table.length) - 1];
    }
}
