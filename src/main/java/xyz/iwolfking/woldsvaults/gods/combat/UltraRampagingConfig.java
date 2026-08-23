package xyz.iwolfking.woldsvaults.gods.combat;

import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;

/**
 * Every tuned number behind Ultra Rampaging, read from {@code god_node_effects_idona.json}.
 *
 * <p>Component names are the config keys verbatim, because the codec binds by component name.
 * It lives in {@code gods.combat} rather than nested in {@code IdonaNodeHandlers} with the tree's
 * other params records, because the Fury system in this package reads it on the server tick
 * without a node context, and the dependency between the two packages already runs
 * {@code trees.idona -> gods.combat}.
 *
 * @param fury_per_hit             Fury for one qualifying hit on an ordinary enemy.
 * @param fury_per_kill            Fury for a kill, paid on top of the hit that caused it.
 * @param fury_boss_hit_multiplier Multiplier on {@code fury_per_hit} against a boss-type enemy.
 * @param fury_boss_kill_multiplier Multiplier on {@code fury_per_kill} against a boss-type enemy.
 * @param fury_per_hp_fraction_lost Fury per full bar of maximum health lost, scaled by the fraction
 *                                 actually lost.
 * @param fury_decay_per_tick      Per-tick retained fraction at or below the scaling threshold.
 * @param fury_decay_scale_from    Fury above which the decay exponent starts scaling.
 * @param fury_decay_scale_divisor Divisor turning Fury into that exponent.
 * @param fury_floor               Fury below which the pool is dropped to zero outright.
 * @param fury_cap                 Hard backstop on the pool.
 * @param cdm_additive_divisor     Divisor inside the cube root of the additive term.
 * @param cdm_multiplier_base      Per-100-Fury base of the exponential half of the multiplier.
 * @param cdm_sqrt_from            Fury at which the multiplier hands over to the square root.
 * @param incoming_divisor         Divisor inside the square root of the incoming-damage drawback.
 * @param incoming_scale           Scale on that square root.
 */
public record UltraRampagingConfig(float fury_per_hit, float fury_per_kill,
                                   float fury_boss_hit_multiplier, float fury_boss_kill_multiplier,
                                   float fury_per_hp_fraction_lost,
                                   float fury_decay_per_tick, float fury_decay_scale_from,
                                   float fury_decay_scale_divisor, float fury_floor, float fury_cap,
                                   float cdm_additive_divisor, float cdm_multiplier_base,
                                   float cdm_sqrt_from,
                                   float incoming_divisor, float incoming_scale)
        implements GodEffectParams {

    /**
     * The values the node ships with, used only as the fallback if the effect is somehow read
     * before the god node registry has loaded - {@link UltraRampaging#config()} logs when that
     * happens rather than falling back silently.
     *
     * <p>These must match the block {@code GodNodeEffectDefaults#idona} writes. Nothing enforces
     * that, so changing one means changing the other; a drift would show as the node behaving
     * differently before and after the config file is first read.
     */
    public static UltraRampagingConfig defaults() {
        return new UltraRampagingConfig(100.0F, 200.0F, 2.0F, 10.0F, 6000.0F,
                0.985F, 6000.0F, 2000.0F, 25.0F, 15000.0F,
                6500.0F, 1.03F, 6000.0F,
                3000.0F, 0.5F);
    }

    @Override
    public void validate(String effectId) {
        positive(effectId, "fury_per_hit", this.fury_per_hit);
        positive(effectId, "fury_per_hp_fraction_lost", this.fury_per_hp_fraction_lost);
        positive(effectId, "fury_decay_scale_divisor", this.fury_decay_scale_divisor);
        positive(effectId, "fury_cap", this.fury_cap);
        positive(effectId, "cdm_additive_divisor", this.cdm_additive_divisor);
        positive(effectId, "incoming_divisor", this.incoming_divisor);
        if (this.fury_decay_per_tick <= 0.0F || this.fury_decay_per_tick >= 1.0F) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' field 'fury_decay_per_tick' must be "
                    + "in (0, 1) so Fury decays; got " + this.fury_decay_per_tick);
        }
        if (this.cdm_multiplier_base < 1.0F) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' field 'cdm_multiplier_base' must be at "
                    + "least 1 so the multiplier never reduces damage; got " + this.cdm_multiplier_base);
        }
        if (this.cdm_sqrt_from <= 0.0F) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' field 'cdm_sqrt_from' must be positive; "
                    + "got " + this.cdm_sqrt_from);
        }
        if (this.fury_cap < this.cdm_sqrt_from) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' has 'fury_cap' (" + this.fury_cap
                    + ") below 'cdm_sqrt_from' (" + this.cdm_sqrt_from + "), so the square-root half of the damage "
                    + "curve could never be reached");
        }
        if (this.fury_floor < 0.0F || this.fury_floor >= this.fury_cap) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' field 'fury_floor' must be in "
                    + "[0, fury_cap); got " + this.fury_floor);
        }
    }

    private static void positive(String effectId, String field, float value) {
        if (value <= 0.0F) {
            throw GodTreeConfigException.fail("God effect '" + effectId + "' field '" + field
                    + "' must be positive; got " + value);
        }
    }

    public float decayPerTick() {
        return this.fury_decay_per_tick;
    }

    public float decayScaleFrom() {
        return this.fury_decay_scale_from;
    }

    public float decayScaleDivisor() {
        return this.fury_decay_scale_divisor;
    }

    public float furyFloor() {
        return this.fury_floor;
    }

    public float furyCap() {
        return this.fury_cap;
    }
}
