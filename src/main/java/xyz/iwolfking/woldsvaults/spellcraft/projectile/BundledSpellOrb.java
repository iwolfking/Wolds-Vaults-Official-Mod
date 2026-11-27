package xyz.iwolfking.woldsvaults.spellcraft.projectile;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.spellcraft.ability.CastOrigin;
import xyz.iwolfking.woldsvaults.spellcraft.cast.SpellCaster;
import xyz.iwolfking.woldsvaults.spellcraft.spell.SpellModel;

import java.util.List;

/**
 * The Bundled Spell projectile: flies straight for its rolled flight time and, on hitting an entity or block, casts its
 * payload at the landing point, or where it is when its orbit ends. An ethereal bundle passes through blocks and lands
 * only on entities. It is never saved with the world, since its payload lives only in memory.
 */
public class BundledSpellOrb extends Projectile {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BundledSpellOrb.class, EntityDataSerializers.FLOAT);
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.75F, 0.35F, 1.0F), 1.0F);

    private List<SpellModel.Step> payload;
    private float damageFactor;
    private int lifetimeTicks;
    private boolean ethereal;
    private int age;

    public BundledSpellOrb(EntityType<? extends BundledSpellOrb> type, Level level) {
        super(type, level);
    }

    public BundledSpellOrb(Level level, ServerPlayer owner, List<SpellModel.Step> payload, float damageFactor, int lifetimeTicks,
                               float scale, boolean ethereal) {
        this(ModEntities.BUNDLED_SPELL_ORB, level);
        setOwner(owner);
        this.payload = payload;
        this.damageFactor = damageFactor;
        this.lifetimeTicks = lifetimeTicks;
        this.ethereal = ethereal;
        this.entityData.set(SCALE, scale);
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (SCALE.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(this.entityData.get(SCALE));
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            if (this.payload == null || ModConfigs.SPELLCRAFT == null || ++this.age > this.lifetimeTicks) {
                discard();
                return;
            }
        }
        Vec3 motion = getDeltaMovement();
        if (!this.level.isClientSide) {
            HitResult hit = this.ethereal ? findEntityHit(motion) : ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hit != null && hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) {
                onHit(hit);
                if (isRemoved()) {
                    return;
                }
            }
        }
        setPos(position().add(motion));
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        if (this.level.isClientSide) {
            spawnTrail();
        }
    }

    private HitResult findEntityHit(Vec3 motion) {
        Vec3 start = position();
        AABB swept = getBoundingBox().expandTowards(motion).inflate(1.0);
        return ProjectileUtil.getEntityHitResult(this.level, this, start, start.add(motion), swept, this::canHitEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        land(hit.getEntity().getBoundingBox().getCenter());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        Vec3 face = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.25);
        land(hit.getLocation().add(face));
    }

    void holdFor(int ticks) {
        this.lifetimeTicks = Math.max(this.lifetimeTicks, this.age + ticks);
    }

    void landHere() {
        land(position());
    }

    private void land(Vec3 position) {
        if (getOwner() instanceof ServerPlayer owner && owner.isAlive() && this.payload != null) {
            SpellCaster.runPayload(owner, this.payload, new CastOrigin(position, getDeltaMovement()), this.damageFactor);
        }
        discard();
    }

    private void spawnTrail() {
        float scale = this.entityData.get(SCALE);
        int count = Math.max(1, Math.round(3 * scale));
        double spread = 0.1 * scale;
        for (int i = 0; i < count; i++) {
            this.level.addParticle(TRAIL,
                    getX() + (this.random.nextDouble() - 0.5) * spread,
                    getY() + getBbHeight() / 2 + (this.random.nextDouble() - 0.5) * spread,
                    getZ() + (this.random.nextDouble() - 0.5) * spread, 0, 0, 0);
        }
    }
}
