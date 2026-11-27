package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

final class Steered {
    private static final double RESEND_COS = Math.cos(Math.toRadians(2.0));
    private static final int RESEND_TICKS = 10;

    final Projectile projectile;
    final ServerPlayer owner;
    final boolean orbit;
    final double angleOffset;
    final double speed;
    final double range;
    final double turnRate;
    final long orbitEnd;
    int age;
    Vec3 lastPosition;
    int stillTicks;
    LivingEntity target;
    LivingEntity preferred;
    Vec3 sentDirection;
    float sentSpeed;
    int sentAge;

    Steered(Projectile projectile, ServerPlayer owner, boolean orbit, double angleOffset, double range, double turnRate, long orbitEnd) {
        this.range = range;
        this.turnRate = turnRate;
        this.orbitEnd = orbitEnd;
        this.projectile = projectile;
        this.owner = owner;
        this.orbit = orbit;
        this.angleOffset = angleOffset;
        this.speed = projectile.getDeltaMovement().length();
        this.lastPosition = projectile.position();
    }

    boolean isStuck() {
        Vec3 position = this.projectile.position();
        this.stillTicks = position.distanceToSqr(this.lastPosition) < 1.0E-4 ? this.stillTicks + 1 : 0;
        this.lastPosition = position;
        return this.age > 2 && this.stillTicks >= 2;
    }

    boolean needsResend(Vec3 direction, float speed) {
        if (this.sentDirection != null && this.age - this.sentAge < RESEND_TICKS && direction.dot(this.sentDirection) > RESEND_COS
                && Math.abs(speed - this.sentSpeed) <= this.sentSpeed * 0.05F) {
            return false;
        }
        this.sentDirection = direction;
        this.sentSpeed = speed;
        this.sentAge = this.age;
        return true;
    }
}
