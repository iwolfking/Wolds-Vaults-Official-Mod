package xyz.iwolfking.woldsvaults.modifiers.gear.unique;

import iskallia.vault.entity.entity.VaultFireball;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.entities.projectiles.VaultMeteor;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Per-explosion state for player fireball explosions ({@code VaultFireball.explode}), which runs its hits in
 * static lambdas that cannot see the fireball. Holds the Steel Inferno roll, the explosion centre and the
 * final radius so the target filter and the hurt call can apply the centre falloff.
 */
public final class SteelInferno {
    private static final ThreadLocal<Explosion> CURRENT = new ThreadLocal<>();
    private static final AtomicBoolean WARNED_NO_SCOPE = new AtomicBoolean();
    private static final AtomicBoolean WARNED_NO_RADIUS = new AtomicBoolean();

    private SteelInferno() {
    }

    @Nullable
    public static Explosion current() {
        return CURRENT.get();
    }

    public static void set(@Nullable Explosion explosion) {
        if (explosion == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(explosion);
        }
    }

    /**
     * The explosion state for {@code fireball} exploding at {@code pos}: meteors are marked and never get Steel Inferno;
     * other fireballs take the Steel Core glyph of the cast that threw them, if it had one.
     */
    public static Explosion forFireball(VaultFireball fireball, Vec3 pos) {
        if (fireball instanceof VaultMeteor) {
            return new Explosion(pos, null, true);
        }
        return new Explosion(pos, GearGlyphs.steelCore(fireball), false);
    }

    /** Squared distance from {@code pos} to the nearest point of {@code entity}'s hitbox. */
    public static double hitboxDistanceSqr(LivingEntity entity, Vec3 pos) {
        AABB box = entity.getBoundingBox();
        double dx = pos.x - Mth.clamp(pos.x, box.minX, box.maxX);
        double dy = pos.y - Mth.clamp(pos.y, box.minY, box.maxY);
        double dz = pos.z - Mth.clamp(pos.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    /** Whether the hurt call of the running explosion should ignore i-frames: every player fireball except meteors. */
    public static boolean ignoresIFrames() {
        Explosion explosion = CURRENT.get();
        if (explosion == null) {
            if (WARNED_NO_SCOPE.compareAndSet(false, true)) {
                WoldLogger.error("Fireball explosion hit ran outside its explode scope; it keeps vanilla i-frames and no Steel Inferno falloff.");
            }
            return false;
        }
        return !explosion.meteor();
    }

    /** {@code damage} times {@code 1 + B(1 - (d/R)^2)} under Steel Inferno, with d measured to the target's hitbox. */
    public static float scaleDamage(LivingEntity entity, float damage) {
        Explosion explosion = CURRENT.get();
        if (explosion == null || explosion.attribute() == null) {
            return damage;
        }
        float radius = explosion.radius();
        if (radius <= 0.0F) {
            if (WARNED_NO_RADIUS.compareAndSet(false, true)) {
                WoldLogger.error("Steel Inferno explosion had no radius recorded; dealing flat damage without the centre bonus.");
            }
            return damage;
        }
        double distance = Math.min(Math.sqrt(hitboxDistanceSqr(entity, explosion.pos())), radius);
        double ratio = distance / radius;
        return (float) (damage * (1.0D + explosion.attribute().getCentreBonus() * (1.0D - ratio * ratio)));
    }

    public static final class Explosion {
        private final Vec3 pos;
        @Nullable
        private final SteelInfernoAttribute attribute;
        private final boolean meteor;
        private float radius;

        private Explosion(Vec3 pos, @Nullable SteelInfernoAttribute attribute, boolean meteor) {
            this.pos = pos;
            this.attribute = attribute;
            this.meteor = meteor;
        }

        public Vec3 pos() {
            return this.pos;
        }

        @Nullable
        public SteelInfernoAttribute attribute() {
            return this.attribute;
        }

        public boolean meteor() {
            return this.meteor;
        }

        public float radius() {
            return this.radius;
        }

        /** Scales the explosion's radius by the Steel Inferno roll, if any, and records the result. */
        public float applyRadius(float radius) {
            this.radius = this.attribute == null ? radius : radius * this.attribute.getRadiusMultiplier();
            return this.radius;
        }
    }
}
