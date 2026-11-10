package xyz.iwolfking.woldsvaults.augments.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;
import xyz.iwolfking.woldsvaults.augments.cast.ShotgunHits;

import javax.annotation.Nullable;

/**
 * The glyph state a projectile carries in its persistent data: damage and area multipliers, size, Low Gravity, the
 * Ethereal and Orbit durations, Homing's range and turn rate, the ability spec that cast it (to tell its own hits from
 * procs) and its Shotgun volley member. Child projectiles a tagged projectile spawns during its own tick (Scatter
 * javelin splits, ricochets) copy it, so glyphs follow the whole cast.
 */
public final class AugmentTags {
    public static final String DAMAGE = "woldsvaults:augment_damage";
    public static final String AREA = "woldsvaults:augment_area";
    public static final String SCALE = "woldsvaults:augment_scale";
    public static final String GRAVITY = "woldsvaults:augment_gravity";
    public static final String ETHEREAL = "woldsvaults:augment_ethereal";
    public static final String HOMING_RANGE = "woldsvaults:augment_homing_range";
    public static final String HOMING_TURN = "woldsvaults:augment_homing_turn";
    public static final String ORBIT = "woldsvaults:augment_orbit";
    public static final String SPEC = "woldsvaults:augment_spec";
    public static final String SHOT_GROUP = "woldsvaults:augment_shot_group";
    public static final String SHOT_MEMBER = "woldsvaults:augment_shot_member";
    private static final String[] KEYS = {DAMAGE, AREA, SCALE, GRAVITY, ETHEREAL, HOMING_RANGE, HOMING_TURN, ORBIT, SPEC, SHOT_GROUP, SHOT_MEMBER};

    private AugmentTags() {
    }

    public static void write(Projectile projectile, CastModifiers modifiers, @Nullable String spec, @Nullable ShotgunHits.Shot shot) {
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
        } else if (modifiers.homing()) {
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
    }

    @Nullable
    public static String spec(@Nullable Entity entity) {
        return entity != null && entity.getPersistentData().contains(SPEC) ? entity.getPersistentData().getString(SPEC) : null;
    }

    @Nullable
    public static ShotgunHits.Shot shot(@Nullable Entity entity) {
        if (entity == null || !entity.getPersistentData().contains(SHOT_GROUP)) {
            return null;
        }
        return new ShotgunHits.Shot(entity.getPersistentData().getLong(SHOT_GROUP), entity.getPersistentData().getInt(SHOT_MEMBER));
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
