package xyz.iwolfking.woldsvaults.entities.projectiles;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.init.ModEntities;

import java.util.UUID;

/**
 * The hyperboss's Magic Missile: a gravity-free homing orb that steers toward its target with a
 * capped per-tick turn rate, so a hard strafe past it forces an overshoot — juke it or die. The
 * renderer is a no-op; the visual is the entity's own particle cloud (a dark-blue dust orb with
 * a soul-flame core). It detonates on player contact, on any block, or when its flight time
 * runs out, and every detonation path deals its launch-time damage snapshot to all players
 * within the blast radius as PLAIN PHYSICAL damage — armor and dodge apply in full (hyper
 * ability damage is six figures raw; bypassing armor deletes tank builds outright).
 * Only players block the ray — arena mobs cannot bodyguard the boss. Spawned exclusively by
 * HyperBossManager, so the ability cannot appear outside hyper vaults.
 */
public class MagicMissileEntity extends Projectile {
    public static final Vector3f PARTICLE_COLOR = new Vector3f(0.16F, 0.22F, 0.95F);

    private float aoeDamage = 1.0F;
    private float aoeRadius = 2.0F;
    private float speed = 0.65F;
    private float turnRadiansPerTick = (float) Math.toRadians(5.0D);
    private int lifetimeTicks = 120;
    private int age = 0;
    private UUID targetId = null;

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public MagicMissileEntity(Level level, LivingEntity owner, LivingEntity target, Vec3 initialDirection,
                              float aoeDamage, float aoeRadius, float speed, double turnDegreesPerTick, int lifetimeTicks) {
        this(ModEntities.MAGIC_MISSILE, level);
        this.setOwner(owner);
        this.targetId = target.getUUID();
        this.aoeDamage = aoeDamage;
        this.aoeRadius = aoeRadius;
        this.speed = speed;
        this.turnRadiansPerTick = (float) Math.toRadians(turnDegreesPerTick);
        this.lifetimeTicks = lifetimeTicks;
        this.moveTo(owner.getX(), owner.getY() + owner.getBbHeight() * 0.75D, owner.getZ(), 0.0F, 0.0F);
        this.setDeltaMovement(initialDirection.normalize().scale(speed));
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            spawnTrailParticles();
            return;
        }
        if (++this.age >= this.lifetimeTicks) {
            detonate();
            return;
        }
        steerTowardTarget();
        HitResult hit = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) {
            detonate();
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof Player && super.canHitEntity(entity);
    }

    private void steerTowardTarget() {
        Vec3 motion = this.getDeltaMovement();
        Vec3 direction = motion.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : motion.normalize();
        Entity target = this.targetId == null ? null : ((ServerLevel) this.level).getEntity(this.targetId);
        if (target instanceof LivingEntity living && living.isAlive() && !living.isSpectator() && living.level == this.level) {
            Vec3 aim = living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D).subtract(this.position());
            if (aim.lengthSqr() > 1.0E-6D) {
                direction = rotateToward(direction, aim.normalize(), this.turnRadiansPerTick);
            }
        }
        this.setDeltaMovement(direction.scale(this.speed));
    }

    /**
     * Rotates the current flight direction toward the desired one by at most maxRadians
     * (Rodrigues rotation about their shared normal) — the capped turn rate that makes the
     * missile jukeable instead of a guaranteed hit.
     */
    private static Vec3 rotateToward(Vec3 current, Vec3 desired, float maxRadians) {
        double dot = Mth.clamp(current.dot(desired), -1.0D, 1.0D);
        double angle = Math.acos(dot);
        if (angle <= maxRadians) {
            return desired;
        }
        Vec3 axis = current.cross(desired);
        if (axis.lengthSqr() < 1.0E-8D) {
            axis = Math.abs(current.y) < 0.99D ? new Vec3(0.0D, 1.0D, 0.0D).cross(current) : new Vec3(1.0D, 0.0D, 0.0D).cross(current);
        }
        axis = axis.normalize();
        double cos = Math.cos(maxRadians);
        double sin = Math.sin(maxRadians);
        return current.scale(cos)
                .add(axis.cross(current).scale(sin))
                .add(axis.scale(axis.dot(current) * (1.0D - cos)))
                .normalize();
    }

    private void spawnTrailParticles() {
        Vec3 pos = this.position();
        for (int i = 0; i < 3; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.35D;
            double oy = (this.random.nextDouble() - 0.5D) * 0.35D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.35D;
            this.level.addParticle(new DustParticleOptions(PARTICLE_COLOR, 1.4F), pos.x + ox, pos.y + oy, pos.z + oz, 0.0D, 0.0D, 0.0D);
        }
        this.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
    }

    private void detonate() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }
        Vec3 pos = this.position();
        Entity owner = this.getOwner();
        DamageSource source = new IndirectEntityDamageSource("woldsvaults.magic_missile", this, owner);
        double radius = this.aoeRadius;
        AABB area = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area,
                p -> !p.isSpectator() && p.isAlive() && closestDistanceSqr(p.getBoundingBox(), pos) <= radius * radius)) {
            player.hurt(source, this.aoeDamage);
        }
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 2, 0.2D, 0.2D, 0.2D, 0.0D);
        serverLevel.sendParticles(new DustParticleOptions(PARTICLE_COLOR, 2.0F), pos.x, pos.y, pos.z, 40, radius * 0.5D, radius * 0.5D, radius * 0.5D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 16, 0.4D, 0.4D, 0.4D, 0.08D);
        serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.2F, 1.1F);
        this.discard();
    }

    private static double closestDistanceSqr(AABB box, Vec3 point) {
        double dx = Mth.clamp(point.x, box.minX, box.maxX) - point.x;
        double dy = Mth.clamp(point.y, box.minY, box.maxY) - point.y;
        double dz = Mth.clamp(point.z, box.minZ, box.maxZ) - point.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("AoeDamage", this.aoeDamage);
        tag.putFloat("AoeRadius", this.aoeRadius);
        tag.putFloat("Speed", this.speed);
        tag.putFloat("TurnRadians", this.turnRadiansPerTick);
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
        tag.putInt("Age", this.age);
        if (this.targetId != null) {
            tag.putUUID("Target", this.targetId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.aoeDamage = tag.getFloat("AoeDamage");
        this.aoeRadius = tag.contains("AoeRadius") ? tag.getFloat("AoeRadius") : 2.0F;
        this.speed = tag.contains("Speed") ? tag.getFloat("Speed") : 0.65F;
        this.turnRadiansPerTick = tag.contains("TurnRadians") ? tag.getFloat("TurnRadians") : (float) Math.toRadians(5.0D);
        this.lifetimeTicks = tag.contains("LifetimeTicks") ? tag.getInt("LifetimeTicks") : 120;
        this.age = tag.getInt("Age");
        this.targetId = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
