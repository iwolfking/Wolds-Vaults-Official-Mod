package xyz.iwolfking.woldsvaults.spellcraft.scope;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

import javax.annotation.Nullable;

/**
 * The glyph state a projectile carries in its persistent data. Child projectiles a tagged projectile spawns during its
 * own tick (Scatter javelin splits, ricochets, Volley bounces) copy it, so glyphs follow the whole cast and an
 * inherited orbit keeps its end tick. A child marked with {@link #leaveOrbit} copies everything except the orbit, and
 * still homes.
 */
public final class ProjectileGlyphTags {
    public static final String DAMAGE = "woldsvaults:augment_damage";
    public static final String AREA = "woldsvaults:augment_area";
    public static final String SCALE = "woldsvaults:augment_scale";
    public static final String GRAVITY = "woldsvaults:augment_gravity";
    public static final String ETHEREAL = "woldsvaults:augment_ethereal";
    public static final String HOMING_RANGE = "woldsvaults:augment_homing_range";
    private static final String HOMING_TURN = "woldsvaults:augment_homing_turn";
    public static final String ORBIT = "woldsvaults:augment_orbit";
    public static final String ORBIT_END = "woldsvaults:augment_orbit_end";
    private static final String NO_ORBIT = "woldsvaults:augment_no_orbit";
    public static final String SPEC = "woldsvaults:augment_spec";
    static final String SHOT_GROUP = "woldsvaults:augment_shot_group";
    static final String SHOT_MEMBER = "woldsvaults:augment_shot_member";
    private static final String[] KEYS = {DAMAGE, AREA, SCALE, GRAVITY, ETHEREAL, HOMING_RANGE, HOMING_TURN, ORBIT, ORBIT_END, SPEC, SHOT_GROUP, SHOT_MEMBER};

    private ProjectileGlyphTags() {
    }

    public static void write(Projectile projectile, CastModifiers modifiers, @Nullable String spec, @Nullable ShotgunShot shot) {
        CompoundTag data = projectile.getPersistentData();
        if (spec != null) {
            data.putString(SPEC, spec);
        }
        if (shot != null) {
            data.putLong(SHOT_GROUP, shot.group());
            data.putInt(SHOT_MEMBER, shot.member());
        }
        if (modifiers.damage() != 1.0F) {
            data.putFloat(DAMAGE, modifiers.damage());
        }
        if (modifiers.area() != 1.0F) {
            data.putFloat(AREA, modifiers.area());
        }
        if (modifiers.scale() != 1.0F) {
            data.putFloat(SCALE, modifiers.scale());
        }
        if (modifiers.gravityReduction() > 0) {
            data.putFloat(GRAVITY, modifiers.gravityReduction());
        }
        if (modifiers.ethereal()) {
            data.putFloat(ETHEREAL, modifiers.etherealSeconds());
        }
        if (modifiers.orbit()) {
            data.putFloat(ORBIT, modifiers.orbitSeconds());
        }
        if (modifiers.homingRange() > 0) {
            data.putFloat(HOMING_RANGE, modifiers.homingRange());
            data.putFloat(HOMING_TURN, modifiers.homingTurn());
        }
    }

    public static boolean isTagged(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        for (String key : KEYS) {
            if (data.contains(key)) {
                return true;
            }
        }
        return false;
    }

    public static void copy(Entity from, Entity to) {
        CompoundTag source = from.getPersistentData();
        CompoundTag target = to.getPersistentData();
        for (String key : KEYS) {
            if (source.contains(key)) {
                target.put(key, source.get(key).copy());
            }
        }
        if (target.getBoolean(NO_ORBIT)) {
            target.remove(ORBIT);
            target.remove(ORBIT_END);
        }
    }

    /** Marks a projectile about to be spawned so the tags it inherits from its parent leave out the orbit. */
    public static void leaveOrbit(Entity entity) {
        entity.getPersistentData().putBoolean(NO_ORBIT, true);
    }

    @Nullable
    public static String spec(@Nullable Entity entity) {
        return entity != null && entity.getPersistentData().contains(SPEC) ? entity.getPersistentData().getString(SPEC) : null;
    }

    @Nullable
    public static ShotgunShot shot(@Nullable Entity entity) {
        if (entity == null || !entity.getPersistentData().contains(SHOT_GROUP)) {
            return null;
        }
        return new ShotgunShot(entity.getPersistentData().getLong(SHOT_GROUP), entity.getPersistentData().getInt(SHOT_MEMBER));
    }

    public static float damage(Entity entity) {
        return entity.getPersistentData().contains(DAMAGE) ? entity.getPersistentData().getFloat(DAMAGE) : 1.0F;
    }

    public static float area(Entity entity) {
        return entity.getPersistentData().contains(AREA) ? entity.getPersistentData().getFloat(AREA) : 1.0F;
    }

    public static float scale(Entity entity) {
        return entity.getPersistentData().contains(SCALE) ? entity.getPersistentData().getFloat(SCALE) : 1.0F;
    }

    public static float gravity(Entity entity) {
        return entity.getPersistentData().getFloat(GRAVITY);
    }

    public static float etherealSeconds(Entity entity) {
        return entity.getPersistentData().getFloat(ETHEREAL);
    }

    public static float homingRange(Entity entity) {
        return entity.getPersistentData().getFloat(HOMING_RANGE);
    }

    public static float homingTurn(Entity entity) {
        return entity.getPersistentData().getFloat(HOMING_TURN);
    }

    public static float orbitSeconds(Entity entity) {
        return entity.getPersistentData().getFloat(ORBIT);
    }
}
