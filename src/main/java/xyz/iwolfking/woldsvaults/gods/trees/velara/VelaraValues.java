package xyz.iwolfking.woldsvaults.gods.trees.velara;

import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.AdaptiveArmorParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.BounceBackParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.CactusParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.CounterstrikeParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.DefenderOfTheFaithParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.FieldMedicParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.FleetingPhysicalityParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.HealingFlowParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.ImmortalParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.IndomitableParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.MagicArmorParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.MaledictionParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.PerserverenceParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.PresenceParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.SacrificeParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.SanitationParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.SteadfastParams;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraNodeHandlers.StonewallParams;

/**
 * Every tuned number Velara's listeners read, resolved through the node registry's typed
 * parameters rather than through Java constants.
 *
 * <p>Values are read through methods rather than held in {@code static final} fields because the
 * tree is class-loaded during mod construction, before the config pass runs: a constant
 * initialised from config would bake whatever was loaded at class-init time and never see a
 * config reload.
 *
 * <p>The stat nodes have no entry here at all. Their values are the handler's per-point table and
 * never reach Java, which is what makes a new stat node a config-only change.
 */
public final class VelaraValues {
    private VelaraValues() {
    }

    public static float counterstrikeChance() {
        return VelaraNodeHandlers.params(VelaraNodes.COUNTERSTRIKE, CounterstrikeParams.class).chance();
    }

    public static float magicArmorEfficiency() {
        return VelaraNodeHandlers.params(VelaraNodes.MAGIC_ARMOR, MagicArmorParams.class).efficiency();
    }

    public static float defenderPerCharm() {
        return VelaraNodeHandlers.params(VelaraNodes.DEFENDER_OF_THE_FAITH, DefenderOfTheFaithParams.class).per_charm();
    }

    public static float perserverenceTimerBonus() {
        return VelaraNodeHandlers.params(VelaraNodes.PERSERVERENCE, PerserverenceParams.class).timer_bonus();
    }

    public static float adaptiveArmorPerStack() {
        return VelaraNodeHandlers.params(VelaraNodes.ADAPTIVE_ARMOR, AdaptiveArmorParams.class).per_stack();
    }

    public static int adaptiveArmorMaxStacks() {
        return VelaraNodeHandlers.params(VelaraNodes.ADAPTIVE_ARMOR, AdaptiveArmorParams.class).max_stacks();
    }

    public static float bounceBackMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.BOUNCE_BACK, BounceBackParams.class).multiplier();
    }

    public static float bounceBackHealthThreshold() {
        return VelaraNodeHandlers.params(VelaraNodes.BOUNCE_BACK, BounceBackParams.class).health_threshold();
    }

    public static int indomitableRegenerationLevels() {
        return VelaraNodeHandlers.params(VelaraNodes.INDOMITABLE, IndomitableParams.class).regeneration_levels();
    }

    public static float fieldMedicMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.FIELD_MEDIC, FieldMedicParams.class).multiplier();
    }

    public static float stonewallSpeedMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.THE_STONEWALL, StonewallParams.class).speed_multiplier();
    }

    public static float stonewallArmorMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.THE_STONEWALL, StonewallParams.class).armor_multiplier();
    }

    public static float cactusArmorMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.CACTUS, CactusParams.class).armor_multiplier();
    }

    public static float cactusThornsMultiplier() {
        return VelaraNodeHandlers.params(VelaraNodes.CACTUS, CactusParams.class).thorns_multiplier();
    }

    public static float maledictionForcedHealing() {
        return VelaraNodeHandlers.params(VelaraNodes.MALEDICTION, MaledictionParams.class).forced_healing();
    }

    public static int fleetingImmuneTicks() {
        return fleeting().immune_ticks();
    }

    /** One full immune-then-vulnerable rotation, so the two halves can never drift apart. */
    public static int fleetingCycleTicks() {
        return fleeting().cycleTicks();
    }

    public static float fleetingDamageMultiplier() {
        return fleeting().damage_multiplier();
    }

    public static float steadfastKnockbackFloor() {
        return VelaraNodeHandlers.params(VelaraNodes.STEADFAST, SteadfastParams.class).knockback_floor();
    }

    public static float steadfastArmorPerExcess() {
        return VelaraNodeHandlers.params(VelaraNodes.STEADFAST, SteadfastParams.class).armor_per_excess();
    }

    public static float sanitationRadius() {
        return VelaraNodeHandlers.params(VelaraNodes.SANITATION, SanitationParams.class).radius();
    }

    public static int sanitationDurationDivisor() {
        return VelaraNodeHandlers.params(VelaraNodes.SANITATION, SanitationParams.class).duration_divisor();
    }

    public static float presenceRadius() {
        return presence().radius();
    }

    public static float presenceResistance() {
        return presence().resistance();
    }

    public static float presenceHealing() {
        return presence().healing();
    }

    public static int presenceRegenerationLevels() {
        return presence().regeneration_levels();
    }

    public static float healingFlowPerManaRegen() {
        return VelaraNodeHandlers.params(VelaraNodes.HEALING_FLOW, HealingFlowParams.class).per_mana_regen();
    }

    public static float sacrificeSyphon() {
        return VelaraNodeHandlers.params(VelaraNodes.SACRIFICE, SacrificeParams.class).syphon();
    }

    public static float sacrificeResistance() {
        return VelaraNodeHandlers.params(VelaraNodes.SACRIFICE, SacrificeParams.class).resistance();
    }

    public static float immortalHealthMultiplier() {
        return immortal().health_multiplier();
    }

    public static float immortalArmorMultiplier() {
        return immortal().armor_multiplier();
    }

    public static float immortalHealingMultiplier() {
        return immortal().healing_multiplier();
    }

    public static float immortalFlatHealth() {
        return immortal().flat_health();
    }

    public static float immortalFlatArmor() {
        return immortal().flat_armor();
    }

    public static int immortalRegenerationLevels() {
        return immortal().regeneration_levels();
    }

    public static float immortalDamageMultiplier() {
        return immortal().damage_multiplier();
    }

    public static int immortalReviveCooldownTicks() {
        return immortal().revive_cooldown_ticks();
    }

    private static FleetingPhysicalityParams fleeting() {
        return VelaraNodeHandlers.params(VelaraNodes.FLEETING_PHYSICALITY, FleetingPhysicalityParams.class);
    }

    private static PresenceParams presence() {
        return VelaraNodeHandlers.params(VelaraNodes.PRESENCE, PresenceParams.class);
    }

    private static ImmortalParams immortal() {
        return VelaraNodeHandlers.params(VelaraNodes.IMMORTAL, ImmortalParams.class);
    }
}
