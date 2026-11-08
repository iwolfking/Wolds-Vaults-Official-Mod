package xyz.iwolfking.woldsvaults.augments.cast;

import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

/**
 * The combined effect of the augments on one ability step, with the table's strengths applied. Area and cooldown
 * reduction go into VH's own stats (so its caps apply); the damage multiplier carries Strengthen, Charge and the
 * Shotgun split factor.
 */
public record CastModifiers(float areaBonus, float quickenBonus, float discountFraction, boolean echo,
                            float damageMultiplier, float chargeMultiplier, float speedMultiplier, boolean homing,
                            boolean orbit, float gravityReduction, boolean ethereal, int shotgunSplits, float scale) {

    public static final CastModifiers NONE = new CastModifiers(0, 0, 0, false, 1, 1, 1, false, false, 0, false, 0, 1);

    public static CastModifiers of(FocusAugmentsConfig config, SpellPlan.Cast cast) {
        float charge = cast.count(AugmentType.CHARGE) > 0
                ? 1.0F + cast.count(AugmentType.CHARGE) * config.strength(AugmentType.CHARGE) : 1.0F;
        return new CastModifiers(
                cast.count(AugmentType.AMPLIFY_AREA) * config.strength(AugmentType.AMPLIFY_AREA),
                cast.count(AugmentType.QUICKEN) * config.strength(AugmentType.QUICKEN),
                Math.min(1.0F, cast.count(AugmentType.DISCOUNT) * config.strength(AugmentType.DISCOUNT)),
                cast.count(AugmentType.ECHO) > 0,
                1.0F + cast.count(AugmentType.STRENGTHEN) * config.strength(AugmentType.STRENGTHEN),
                charge,
                1.0F + cast.count(AugmentType.ACCELERATE) * config.strength(AugmentType.ACCELERATE),
                cast.count(AugmentType.HOMING) > 0,
                cast.count(AugmentType.ORBIT) > 0,
                Math.min(1.0F, cast.count(AugmentType.LOW_GRAVITY) * config.strength(AugmentType.LOW_GRAVITY)),
                cast.count(AugmentType.ETHEREAL) > 0,
                cast.count(AugmentType.SHOTGUN),
                scale(config, cast.count(AugmentType.SIZE_UP), cast.count(AugmentType.SIZE_DOWN)));
    }

    /** Size Up and Size Down combined, clamped to the table's size limits. */
    public static float scale(FocusAugmentsConfig config, int sizeUps, int sizeDowns) {
        double scale = Math.pow(1 + config.strength(AugmentType.SIZE_UP), sizeUps)
                * Math.pow(Math.max(0.0, 1 - config.strength(AugmentType.SIZE_DOWN)), sizeDowns);
        return (float) Math.max(config.tuning.sizeDownMinScale, Math.min(config.tuning.sizeUpMaxScale, scale));
    }

    /** The damage factor every hit of this cast gets: Strengthen times Charge times any outside factor. */
    public float totalDamage() {
        return this.damageMultiplier * this.chargeMultiplier;
    }

    public boolean charged() {
        return this.chargeMultiplier > 1.0F;
    }

    public CastModifiers withDamageFactor(float factor) {
        return new CastModifiers(areaBonus, quickenBonus, discountFraction, echo, damageMultiplier * factor, chargeMultiplier,
                speedMultiplier, homing, orbit, gravityReduction, ethereal, shotgunSplits, scale);
    }

    /** The same modifiers without Charge, for Echo when the table says echoes don't keep it. */
    public CastModifiers withoutCharge() {
        return new CastModifiers(areaBonus, quickenBonus, discountFraction, echo, damageMultiplier, 1.0F,
                speedMultiplier, homing, orbit, gravityReduction, ethereal, shotgunSplits, scale);
    }

    /** Whether projectiles spawned by the cast need to be tagged or steered. */
    public boolean affectsProjectiles() {
        return totalDamage() != 1.0F || speedMultiplier != 1.0F || homing || orbit || areaBonus > 0
                || gravityReduction > 0 || ethereal || scale != 1.0F;
    }
}
