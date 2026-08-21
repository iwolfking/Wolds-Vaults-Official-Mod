package xyz.iwolfking.woldsvaults.gods.trees.velara;

import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * Every tuned number of the Velara tree, read from
 * {@code config/the_vault/gods/god_node_effects_velara.json}.
 *
 * <p>A banded stat is two effects, not one: the shallow placements pay the base id's value and
 * the deep ones pay the {@code _ii} id's, each linear in its own star count with no ceiling -
 * see {@link #banded}.
 */
public final class VelaraValues {
    private VelaraValues() {
    }

    /** The {@code {shallow, deep}} pair of a banded stat, as {@link #banded} expects it. */
    public static float[] bands(VelaraNode lesser, VelaraNode greater) {
        return GodNodeValues.bands(lesser.getId(), greater.getId());
    }

    public static int piousDevotionPietyPerPoint() {
        return Math.round(GodNodeValues.value(VelaraNode.PIOUS_DEVOTION.getId()));
    }

    public static float counterstrikeChance() {
        return GodNodeValues.number(VelaraNode.COUNTERSTRIKE.getId(), "chance");
    }

    public static float magicArmorEfficiency() {
        return GodNodeValues.number(VelaraNode.MAGIC_ARMOR.getId(), "efficiency");
    }

    public static float defenderPerCharm() {
        return GodNodeValues.number(VelaraNode.DEFENDER_OF_THE_FAITH.getId(), "per_charm");
    }

    public static float perserverenceTimerBonus() {
        return GodNodeValues.number(VelaraNode.PERSERVERENCE.getId(), "timer_bonus");
    }

    public static float adaptiveArmorPerStack() {
        return GodNodeValues.number(VelaraNode.ADAPTIVE_ARMOR.getId(), "per_stack");
    }

    public static int adaptiveArmorMaxStacks() {
        return GodNodeValues.count(VelaraNode.ADAPTIVE_ARMOR.getId(), "max_stacks");
    }

    public static float bounceBackMultiplier() {
        return GodNodeValues.number(VelaraNode.BOUNCE_BACK.getId(), "multiplier");
    }

    public static float bounceBackHealthThreshold() {
        return GodNodeValues.number(VelaraNode.BOUNCE_BACK.getId(), "health_threshold");
    }

    public static int indomitableRegenerationLevels() {
        return GodNodeValues.count(VelaraNode.INDOMITABLE.getId(), "regeneration_levels");
    }

    public static float fieldMedicMultiplier() {
        return GodNodeValues.number(VelaraNode.FIELD_MEDIC.getId(), "multiplier");
    }

    public static float stonewallSpeedMultiplier() {
        return GodNodeValues.number(VelaraNode.THE_STONEWALL.getId(), "speed_multiplier");
    }

    public static float stonewallArmorMultiplier() {
        return GodNodeValues.number(VelaraNode.THE_STONEWALL.getId(), "armor_multiplier");
    }

    public static float cactusArmorMultiplier() {
        return GodNodeValues.number(VelaraNode.CACTUS.getId(), "armor_multiplier");
    }

    public static float cactusThornsMultiplier() {
        return GodNodeValues.number(VelaraNode.CACTUS.getId(), "thorns_multiplier");
    }

    public static float maledictionForcedHealing() {
        return GodNodeValues.number(VelaraNode.MALEDICTION.getId(), "forced_healing");
    }

    public static int fleetingImmuneTicks() {
        return GodNodeValues.count(VelaraNode.FLEETING_PHYSICALITY.getId(), "immune_ticks");
    }

    public static int fleetingVulnerableTicks() {
        return GodNodeValues.count(VelaraNode.FLEETING_PHYSICALITY.getId(), "vulnerable_ticks");
    }

    /** One full immune-then-vulnerable rotation, so the two halves can never drift apart. */
    public static int fleetingCycleTicks() {
        return fleetingImmuneTicks() + fleetingVulnerableTicks();
    }

    public static float fleetingDamageMultiplier() {
        return GodNodeValues.number(VelaraNode.FLEETING_PHYSICALITY.getId(), "damage_multiplier");
    }

    public static float steadfastKnockbackFloor() {
        return GodNodeValues.number(VelaraNode.STEADFAST.getId(), "knockback_floor");
    }

    public static float steadfastArmorPerExcess() {
        return GodNodeValues.number(VelaraNode.STEADFAST.getId(), "armor_per_excess");
    }

    public static float sanitationRadius() {
        return GodNodeValues.number(VelaraNode.SANITATION.getId(), "radius");
    }

    public static int sanitationDurationDivisor() {
        return GodNodeValues.count(VelaraNode.SANITATION.getId(), "duration_divisor");
    }

    public static float presenceRadius() {
        return GodNodeValues.number(VelaraNode.PRESENCE.getId(), "radius");
    }

    public static float presenceResistance() {
        return GodNodeValues.number(VelaraNode.PRESENCE.getId(), "resistance");
    }

    public static float presenceHealing() {
        return GodNodeValues.number(VelaraNode.PRESENCE.getId(), "healing");
    }

    public static int presenceRegenerationLevels() {
        return GodNodeValues.count(VelaraNode.PRESENCE.getId(), "regeneration_levels");
    }

    public static float healingFlowPerManaRegen() {
        return GodNodeValues.number(VelaraNode.HEALING_FLOW.getId(), "per_mana_regen");
    }

    public static int utilizedAbilityLevels() {
        return GodNodeValues.count(VelaraNode.UTILIZED.getId(), "ability_levels");
    }

    public static float sacrificeSyphon() {
        return GodNodeValues.number(VelaraNode.SACRIFICE.getId(), "syphon");
    }

    public static float sacrificeResistance() {
        return GodNodeValues.number(VelaraNode.SACRIFICE.getId(), "resistance");
    }

    public static float immortalHealthMultiplier() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "health_multiplier");
    }

    public static float immortalArmorMultiplier() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "armor_multiplier");
    }

    public static float immortalHealingMultiplier() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "healing_multiplier");
    }

    public static float immortalFlatHealth() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "flat_health");
    }

    public static float immortalFlatArmor() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "flat_armor");
    }

    public static int immortalRegenerationLevels() {
        return GodNodeValues.count(VelaraNode.IMMORTAL.getId(), "regeneration_levels");
    }

    public static float immortalDamageMultiplier() {
        return GodNodeValues.number(VelaraNode.IMMORTAL.getId(), "damage_multiplier");
    }

    public static int immortalReviveCooldownTicks() {
        return GodNodeValues.count(VelaraNode.IMMORTAL.getId(), "revive_cooldown_ticks");
    }

    /**
     * Total value of a banded stat: {@code bands[0]} for every shallow star owned plus
     * {@code bands[1]} for every deep one. Neither band caps.
     */
    public static float banded(float[] bands, int lesser, int greater) {
        return bands[0] * Math.max(lesser, 0) + bands[1] * Math.max(greater, 0);
    }
}
