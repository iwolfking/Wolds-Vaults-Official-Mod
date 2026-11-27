package xyz.iwolfking.woldsvaults.spellcraft.scope;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStacking;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphStats;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * What the glyphs on one step (an ability or a Bundled Spell orb) do, from their rolled potencies: stat multipliers
 * stacked by {@link GlyphStacking}, the Homing, Orbit, Ethereal, Low Gravity, Shotgun, Echo and Charge values, and the
 * step's active unique glyphs ({@link GlyphGrants}). {@code durationUpside} is the duration factor without Intensify's
 * duration cost, which a toggle's timed part takes. Homing's turn rate is in degrees per second, and dormant glyphs are
 * left out.
 */
public record CastModifiers(float damage, float area, float speed, float scale, float intensity, float duration, float drain,
                            float healMana, float durationUpside, float homingRange, float homingTurn, float orbitSeconds, float gravityReduction,
                            float etherealSeconds, int shotgunCopies, float echoPower, float chargeBonus, GlyphGrants grants) {
    private static final String DAMAGE = "damage";
    private static final String AREA = "area";
    private static final String SPEED = "speed";
    private static final String SCALE = "scale";
    private static final String INTENSITY = "intensity";
    private static final String DURATION = "duration";
    private static final String DRAIN = "drain";
    private static final String HEAL_MANA = "heal_mana";
    private static final String DURATION_UPSIDE = "duration_upside";
    private static final String HOMING_RANGE = "homing_range";
    private static final String HOMING_TURN = "homing_turn";
    private static final String ORBIT = "orbit";
    private static final String GRAVITY = "gravity";
    private static final String ETHEREAL = "ethereal";
    private static final String ECHO = "echo";
    private static final String CHARGE = "charge";
    private static final String SHOTGUN_COPIES = "shotgun_copies";
    private static final String GRANTS = "grants";
    private static final String[] REQUIRED = {DAMAGE, AREA, SPEED, SCALE, INTENSITY, DURATION, DRAIN, HEAL_MANA, DURATION_UPSIDE,
            HOMING_RANGE, HOMING_TURN, ORBIT, GRAVITY, ETHEREAL, ECHO, CHARGE, SHOTGUN_COPIES};

    public static final CastModifiers NONE = new CastModifiers(1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, GlyphGrants.NONE);

    public static CastModifiers of(SpellcraftConfig config, List<PlacedGlyph> glyphs) {
        return of(config::glyph, config.tuning, glyphs);
    }

    /**
     * The step's modifiers from its glyphs: the stacked step stats ({@link PlacedGlyph#pool}), times each Shotgun
     * copy's damage factor, with size clamped to the table's limits.
     */
    public static CastModifiers of(Function<GlyphType, SpellcraftConfig.GlyphDefinition> definitions, SpellcraftConfig.Tuning tuning,
                                   List<PlacedGlyph> glyphs) {
        GlyphStacking.Pool pool = PlacedGlyph.pool(definitions, glyphs);
        float shotgunDamage = 1.0F;
        float homingRange = 0.0F;
        float homingTurn = 0.0F;
        float orbit = 0.0F;
        float gravity = 0.0F;
        float ethereal = 0.0F;
        int copies = 0;
        float echo = 0.0F;
        float charge = 0.0F;
        for (PlacedGlyph glyph : glyphs) {
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
        float scale = Math.max(tuning.sizeDownMinScale, Math.min(tuning.sizeUpMaxScale, pool.total(GlyphStats.SCALE)));
        GlyphStacking.Stat duration = pool.stat(GlyphStats.DURATION);
        float durationUpside = duration == null ? 1.0F : 1.0F + Math.max(0.0F, duration.pool());
        return new CastModifiers(pool.total(GlyphStats.DAMAGE) * shotgunDamage, pool.total(GlyphStats.AREA), pool.total(GlyphStats.SPEED), scale,
                pool.total(GlyphStats.INTENSITY), pool.total(GlyphStats.DURATION), pool.total(GlyphStats.DRAIN), pool.total(GlyphStats.HEAL_MANA),
                durationUpside, homingRange, homingTurn,
                orbit, gravity, ethereal, copies, echo, charge, GlyphGrants.of(definitions, glyphs));
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

    /**
     * The same modifiers with every hit scaled by an outside factor (Echo power, a Charge bonus, a payload's copies).
     */
    public CastModifiers withDamageFactor(float factor) {
        return new CastModifiers(damage * factor, area, speed, scale, intensity, duration, drain, healMana, durationUpside, homingRange, homingTurn,
                orbitSeconds, gravityReduction, etherealSeconds, shotgunCopies, echoPower, chargeBonus, grants);
    }

    void save(CompoundTag tag) {
        tag.putFloat(DAMAGE, this.damage);
        tag.putFloat(AREA, this.area);
        tag.putFloat(SPEED, this.speed);
        tag.putFloat(SCALE, this.scale);
        tag.putFloat(INTENSITY, this.intensity);
        tag.putFloat(DURATION, this.duration);
        tag.putFloat(DRAIN, this.drain);
        tag.putFloat(HEAL_MANA, this.healMana);
        tag.putFloat(DURATION_UPSIDE, this.durationUpside);
        tag.putFloat(HOMING_RANGE, this.homingRange);
        tag.putFloat(HOMING_TURN, this.homingTurn);
        tag.putFloat(ORBIT, this.orbitSeconds);
        tag.putFloat(GRAVITY, this.gravityReduction);
        tag.putFloat(ETHEREAL, this.etherealSeconds);
        tag.putFloat(ECHO, this.echoPower);
        tag.putFloat(CHARGE, this.chargeBonus);
        tag.putInt(SHOTGUN_COPIES, this.shotgunCopies);
        if (!this.grants.isEmpty()) {
            tag.put(GRANTS, this.grants.write());
        }
    }

    @Nullable
    static String missingKey(CompoundTag tag) {
        for (String key : REQUIRED) {
            if (!tag.contains(key)) {
                return key;
            }
        }
        return null;
    }

    @Nullable
    static CastModifiers load(CompoundTag tag) {
        if (missingKey(tag) != null) {
            return null;
        }
        return new CastModifiers(
                tag.getFloat(DAMAGE),
                tag.getFloat(AREA),
                tag.getFloat(SPEED),
                tag.getFloat(SCALE),
                tag.getFloat(INTENSITY),
                tag.getFloat(DURATION),
                tag.getFloat(DRAIN),
                tag.getFloat(HEAL_MANA),
                tag.getFloat(DURATION_UPSIDE),
                tag.getFloat(HOMING_RANGE),
                tag.getFloat(HOMING_TURN),
                tag.getFloat(ORBIT),
                tag.getFloat(GRAVITY),
                tag.getFloat(ETHEREAL),
                tag.getInt(SHOTGUN_COPIES),
                tag.getFloat(ECHO),
                tag.getFloat(CHARGE),
                tag.contains(GRANTS, Tag.TAG_COMPOUND) ? GlyphGrants.read(tag.getCompound(GRANTS)) : GlyphGrants.NONE);
    }

    /** Whether projectiles spawned by the cast need tags, steering or physics. */
    public boolean affectsProjectiles() {
        return this.damage != 1.0F || this.speed != 1.0F || homing() || orbit() || this.area != 1.0F
                || this.gravityReduction > 0 || ethereal() || this.scale != 1.0F;
    }
}
