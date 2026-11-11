package xyz.iwolfking.woldsvaults.augments.cast;

import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.spell.GlyphStacking;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.List;
import java.util.function.Function;

/**
 * What the glyphs on one step (an ability or a Bundled Spell orb) do, from their rolled potencies: multipliers on
 * damage, area, projectile speed and size, buff intensity and duration, toggle drain and heal mana (stacked by
 * {@link GlyphStacking}), the duration upside alone (a toggle's timed part takes Extend but not Intensify's duration
 * cost); Homing (lock-on range from the table, rolled turn rate in degrees per second), Orbit and
 * Ethereal durations; Low Gravity; Shotgun copies and their per-copy damage; Echo power; and Charge's full-charge
 * bonus. Dormant glyphs are left out.
 */
public record CastModifiers(float damage, float area, float speed, float scale, float intensity, float duration, float drain,
                            float healMana, float durationUpside, float homingRange, float homingTurn, float orbitSeconds, float gravityReduction,
                            float etherealSeconds, int shotgunCopies, float echoPower, float chargeBonus) {

    public static final CastModifiers NONE = new CastModifiers(1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);

    public static CastModifiers of(FocusAugmentsConfig config, List<SpellModel.Glyph> glyphs) {
        return of(config::glyph, config.tuning, glyphs);
    }

    /**
     * The step's modifiers from its glyphs: the stacked step stats ({@link #pool}), times each Shotgun copy's damage
     * factor, with size clamped to the table's limits.
     */
    public static CastModifiers of(Function<AugmentType, FocusAugmentsConfig.Glyph> specs, FocusAugmentsConfig.Tuning tuning,
                                   List<SpellModel.Glyph> glyphs) {
        GlyphStacking.Pool pool = pool(specs, glyphs);
        float shotgunDamage = 1.0F;
        float homingRange = 0.0F;
        float homingTurn = 0.0F;
        float orbit = 0.0F;
        float gravity = 0.0F;
        float ethereal = 0.0F;
        int copies = 0;
        float echo = 0.0F;
        float charge = 0.0F;
        for (SpellModel.Glyph glyph : glyphs) {
            if (!glyph.active()) {
                continue;
            }
            switch (glyph.type()) {
                case HOMING -> {
                    homingRange = tuning.homingRange;
                    homingTurn = glyph.potency();
                }
                case ORBIT -> orbit = glyph.potency();
                case LOW_GRAVITY -> gravity = Math.min(1.0F, glyph.potency());
                case ETHEREAL -> ethereal = glyph.potency();
                case SHOTGUN -> {
                    copies++;
                    shotgunDamage *= glyph.potency();
                }
                case ECHO -> echo = glyph.potency();
                case CHARGE -> charge = glyph.potency();
                default -> {
                }
            }
        }
        float scale = Math.max(tuning.sizeDownMinScale, Math.min(tuning.sizeUpMaxScale, pool.total("scale")));
        GlyphStacking.Stat duration = pool.stat("duration");
        float durationUpside = duration == null ? 1.0F : 1.0F + Math.max(0.0F, duration.pool());
        return new CastModifiers(pool.total("damage") * shotgunDamage, pool.total("area"), pool.total("speed"), scale,
                pool.total("intensity"), pool.total("duration"), pool.total("drain"), pool.total("heal_mana"), durationUpside, homingRange, homingTurn,
                orbit, gravity, ethereal, copies, echo, charge);
    }

    /** The stacked step stats of a step's active glyphs, before Shotgun's per-copy damage and the size clamp. */
    public static GlyphStacking.Pool pool(Function<AugmentType, FocusAugmentsConfig.Glyph> specs, List<SpellModel.Glyph> glyphs) {
        GlyphStacking.Pool pool = new GlyphStacking.Pool();
        for (SpellModel.Glyph glyph : glyphs) {
            if (glyph.active()) {
                FocusAugmentsConfig.Glyph spec = specs.apply(glyph.type());
                pool.add(spec, spec.step, glyph.potency());
            }
        }
        return pool;
    }

    public boolean homing() {
        return this.homingRange > 0 && this.orbitSeconds <= 0;
    }

    public boolean orbit() {
        return this.orbitSeconds > 0;
    }

    public boolean ethereal() {
        return this.etherealSeconds > 0;
    }

    /** The same modifiers with every hit scaled by an outside factor (Echo power, a Charge bonus, a payload's copies). */
    public CastModifiers withDamageFactor(float factor) {
        return new CastModifiers(damage * factor, area, speed, scale, intensity, duration, drain, healMana, durationUpside, homingRange, homingTurn,
                orbitSeconds, gravityReduction, etherealSeconds, shotgunCopies, echoPower, chargeBonus);
    }

    /** Whether projectiles spawned by the cast need tags, steering or physics. */
    public boolean affectsProjectiles() {
        return this.damage != 1.0F || this.speed != 1.0F || homing() || orbit() || this.area != 1.0F
                || this.gravityReduction > 0 || ethereal() || this.scale != 1.0F;
    }

    public boolean has(AugmentType type) {
        return switch (type) {
            case SHOTGUN -> this.shotgunCopies > 0;
            case ECHO -> this.echoPower > 0;
            case CHARGE -> this.chargeBonus > 0;
            default -> false;
        };
    }
}
