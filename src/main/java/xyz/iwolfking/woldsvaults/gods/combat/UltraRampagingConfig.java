package xyz.iwolfking.woldsvaults.gods.combat;

import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;

/**
 * Every tuned number behind Ultra Rampaging, read from {@code god_node_effects_idona.json}.
 * Component names are the config keys verbatim; the codec binds by component name.
 *
 * @param fury_per_hp_fraction_lost Fury per full bar of maximum health lost, scaled by the fraction lost.
 * @param cdm_additive_divisor     Divisor inside the cube root of the additive term.
 * @param cdm_multiplier_base      Per-100-Fury base of the exponential half of the multiplier.
 * @param cdm_sqrt_from            Fury at which the multiplier hands over to the square root.
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

    /** The fallback before the registry loads; must match the block {@code GodNodeEffectDefaults#idona} writes. */
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
