package xyz.iwolfking.woldsvaults.entities.simon;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.modifiers.gear.simon.SimonsSet;

import javax.annotation.Nullable;

/**
 * A prism seed in flight: shot by a Simon's Set wearer's right click, or thrown off a crystal that budded. It
 * passes through creatures and plants a Seed-stage crystal ({@link SimonCrystalEntity}) on the face of the first
 * block it hits; if it hits nothing before its time runs out it fizzles. Not saved with the world.
 */
public class PrismSeedEntity extends ThrowableProjectile implements IEntityAdditionalSpawnData {
    private SimonCrystalEntity.Color color = SimonCrystalEntity.Color.RED;
    private boolean natural;
    private boolean prismatic;
    private float gravity = 0.03F;
    private int maxAge = 100;
    @Nullable
    public Vec3 target;

    public PrismSeedEntity(EntityType<? extends PrismSeedEntity> type, Level level) {
        super(type, level);
    }

    /** Creates a seed leaving the player's eyes. */
    public PrismSeedEntity(Level level, ServerPlayer owner, SimonCrystalEntity.Color color, float gravity, int maxAge) {
        super(ModEntities.PRISM_SEED, owner, level);
        this.color = color;
        this.gravity = gravity;
        this.maxAge = maxAge;
    }

    /** Creates a seed thrown off a crystal that budded. */
    public PrismSeedEntity(Level level, ServerPlayer owner, Vec3 from, SimonCrystalEntity.Color color, boolean prismatic, float gravity, int maxAge) {
        super(ModEntities.PRISM_SEED, from.x, from.y, from.z, level);
        this.setOwner(owner);
        this.color = color;
        this.natural = true;
        this.prismatic = prismatic;
        this.gravity = gravity;
        this.maxAge = maxAge;
    }

    public SimonCrystalEntity.Color getColor() {
        return this.color;
    }

    public boolean isNatural() {
        return this.natural;
    }

    public boolean isPrismatic() {
        return this.prismatic;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return this.gravity;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!this.level.isClientSide && !this.isRemoved()) {
            SimonsSet.plant(this, hit);
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.level.addParticle(new DustParticleOptions(this.color.vector(), 0.8F), this.getX(), this.getY() + 0.1, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.tickCount > this.maxAge && !this.isRemoved()) {
            SimonsSet.seedExpired(this);
            this.discard();
        }
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
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeByte(this.color.ordinal());
        buffer.writeBoolean(this.natural);
        buffer.writeBoolean(this.prismatic);
        buffer.writeFloat(this.gravity);
        Vec3 motion = this.getDeltaMovement();
        buffer.writeDouble(motion.x);
        buffer.writeDouble(motion.y);
        buffer.writeDouble(motion.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        SimonCrystalEntity.Color[] colors = SimonCrystalEntity.Color.values();
        this.color = colors[Mth.clamp(buffer.readByte(), 0, colors.length - 1)];
        this.natural = buffer.readBoolean();
        this.prismatic = buffer.readBoolean();
        this.gravity = buffer.readFloat();
        this.setDeltaMovement(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
