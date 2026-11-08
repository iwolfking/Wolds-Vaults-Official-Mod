package xyz.iwolfking.woldsvaults.augments.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import xyz.iwolfking.woldsvaults.augments.cast.CastModifiers;

/**
 * The augment state a projectile carries in its persistent data. Child projectiles a tagged projectile spawns
 * during its own tick (Scatter javelin splits, ricochets) copy it, so augments follow the whole cast.
 */
public final class AugmentTags {
    public static final String DAMAGE = "woldsvaults:augment_damage";
    public static final String AREA = "woldsvaults:augment_area";
    public static final String SCALE = "woldsvaults:augment_scale";
    public static final String GRAVITY = "woldsvaults:augment_gravity";
    public static final String ETHEREAL = "woldsvaults:augment_ethereal";
    public static final String HOMING = "woldsvaults:augment_homing";
    public static final String ORBIT = "woldsvaults:augment_orbit";
    private static final String[] KEYS = {DAMAGE, AREA, SCALE, GRAVITY, ETHEREAL, HOMING, ORBIT};

    private AugmentTags() {
    }

    public static void write(Projectile projectile, CastModifiers modifiers) {
        CompoundTag data = projectile.getPersistentData();
        if (modifiers.totalDamage() != 1.0F) {
            data.putFloat(DAMAGE, modifiers.totalDamage());
        }
        if (modifiers.areaBonus() > 0) {
            data.putFloat(AREA, modifiers.areaBonus());
        }
        if (modifiers.scale() != 1.0F) {
            data.putFloat(SCALE, modifiers.scale());
        }
        if (modifiers.gravityReduction() > 0) {
            data.putFloat(GRAVITY, modifiers.gravityReduction());
        }
        if (modifiers.ethereal()) {
            data.putBoolean(ETHEREAL, true);
        }
        if (modifiers.orbit()) {
            data.putBoolean(ORBIT, true);
        } else if (modifiers.homing()) {
            data.putBoolean(HOMING, true);
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

    public static float damage(Entity entity) {
        return entity.getPersistentData().contains(DAMAGE) ? entity.getPersistentData().getFloat(DAMAGE) : 1.0F;
    }

    public static float area(Entity entity) {
        return entity.getPersistentData().getFloat(AREA);
    }

    public static float scale(Entity entity) {
        return entity.getPersistentData().contains(SCALE) ? entity.getPersistentData().getFloat(SCALE) : 1.0F;
    }

    public static float gravity(Entity entity) {
        return entity.getPersistentData().getFloat(GRAVITY);
    }

    public static boolean ethereal(Entity entity) {
        return entity.getPersistentData().getBoolean(ETHEREAL);
    }

    public static boolean homing(Entity entity) {
        return entity.getPersistentData().getBoolean(HOMING);
    }

    public static boolean orbit(Entity entity) {
        return entity.getPersistentData().getBoolean(ORBIT);
    }
}
