package xyz.iwolfking.woldsvaults.augments.cast;

import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import java.util.List;

/**
 * What the glyphs on one step (an ability or a Bundled Spell orb) do, from their rolled potencies: multipliers on
 * damage, area, projectile speed and size; Homing (range, turn rate), Orbit and Ethereal durations; Low Gravity; Shotgun
 * copies and their per-copy damage; Echo power; and Charge's full-charge bonus. Dormant glyphs are left out.
 */
public record CastModifiers(float damage, float area, float speed, float scale, float homingRange, float homingTurn,
                            float orbitSeconds, float gravityReduction, float etherealSeconds, int shotgunCopies,
                            float echoPower, float chargeBonus) {

    public static final CastModifiers NONE = new CastModifiers(1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);

    public static CastModifiers of(FocusAugmentsConfig config, List<SpellModel.Glyph> glyphs) {
        float damage = 1.0F;
        float area = 1.0F;
        float speed = 1.0F;
        float scale = 1.0F;
        float homingRange = 0.0F;
        float homingTurn = 0.0F;
        float orbit = 0.0F;
        float gravity = 0.0F;
        float ethereal = 0.0F;
        int copies = 0;
        float echo = 0.0F;
        float charge = 0.0F;
        for (SpellModel.Glyph glyph : glyphs) {
            if (glyph.dormant()) {
                continue;
            }
            FocusAugmentsConfig.Glyph spec = config.glyph(glyph.type());
            for (FocusAugmentsConfig.Effect effect : spec.step) {
                float factor = effect.factor(glyph.potency());
                switch (effect.stat) {
                    case "damage" -> damage *= factor;
                    case "area" -> area *= factor;
                    case "speed" -> speed *= factor;
                    case "scale" -> scale *= factor;
                    default -> {
                    }
                }
            }
            switch (glyph.type()) {
                case HOMING -> {
                    homingRange = glyph.potency();
                    homingTurn = linked(spec, glyph.quality());
                }
                case ORBIT -> orbit = glyph.potency();
                case LOW_GRAVITY -> gravity = Math.min(1.0F, glyph.potency());
                case ETHEREAL -> ethereal = glyph.potency();
                case SHOTGUN -> {
                    copies++;
                    damage *= glyph.potency();
                }
                case ECHO -> echo = glyph.potency();
                case CHARGE -> charge = glyph.potency();
                default -> {
                }
            }
        }
        scale = Math.max(config.tuning.sizeDownMinScale, Math.min(config.tuning.sizeUpMaxScale, scale));
        return new CastModifiers(damage, area, speed, scale, homingRange, homingTurn, orbit, gravity, ethereal, copies, echo, charge);
    }

    private static float linked(FocusAugmentsConfig.Glyph spec, float quality) {
        if (spec.potency.linked == null || spec.potency.linked.isEmpty()) {
            return 0.0F;
        }
        FocusAugmentsConfig.Linked linked = spec.potency.linked.get(0);
        return linked.min + (linked.max - linked.min) * quality;
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
        return new CastModifiers(damage * factor, area, speed, scale, homingRange, homingTurn, orbitSeconds, gravityReduction,
                etherealSeconds, shotgunCopies, echoPower, chargeBonus);
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
