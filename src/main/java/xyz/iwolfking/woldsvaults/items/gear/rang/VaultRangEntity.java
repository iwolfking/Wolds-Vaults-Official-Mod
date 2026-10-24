package xyz.iwolfking.woldsvaults.items.gear.rang;

import com.google.common.collect.Multimap;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import vazkii.quark.base.handler.QuarkSounds;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModSounds;
import xyz.iwolfking.woldsvaults.integration.bettercombat.DualWieldSnapshotManager;
import xyz.iwolfking.woldsvaults.items.gear.VaultRangItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultRangEntity extends Projectile {
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(VaultRangEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(VaultRangEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityType<?> TARGET_DUMMY = EntityType.byString("dummmmmmy:target_dummy").orElse(null);

    private static final String TAG_RETURNING = "returning";
    private static final String TAG_LIVE_TIME = "liveTime";
    private static final String TAG_BLOCKS_BROKEN = "hitCount";
    private static final String TAG_RETURN_SLOT = "returnSlot";
    private static final String TAG_ITEM_STACK = "itemStack";
    private static final String TAG_RICOCHETS_USED = "ricochetsUsed";
    private static final String TAG_CURRENT_RICOCHET_TARGET = "currentRicochetTarget";
    private static final String TAG_PENDING_RICOCHETS = "pendingRicochets";

    private static final double RICOCHET_SEARCH_RADIUS = 8.0D;
    private static final double RICOCHET_SPEED = 0.85D;

    protected LivingEntity owner;
    private UUID ownerId;

    private int liveTime;
    private int slot;
    private int blockHitCount;
    private int hits;
    private int ricochetsUsed;
    private float returningDamage = 0.0F;

    private IntOpenHashSet entitiesHit;
    private UUID currentRicochetTargetId;
    private final List<UUID> pendingRicochetTargets = new ArrayList<>();
    private boolean deferMovementThisTick;

    public VaultRangEntity(EntityType<? extends VaultRangEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public VaultRangEntity(Level worldIn, LivingEntity throwerIn) {
        super(ModEntities.VAULT_RANG, worldIn);
        Vec3 pos = throwerIn.position();
        this.setPos(pos.x, pos.y + throwerIn.getEyeHeight(), pos.z);
        this.ownerId = throwerIn.getUUID();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -Mth.sin(rotationYawIn * ((float)Math.PI / 180F)) * Mth.cos(rotationPitchIn * ((float)Math.PI / 180F));
        float f1 = -Mth.sin((rotationPitchIn + pitchOffset) * ((float)Math.PI / 180F));
        float f2 = Mth.cos(rotationYawIn * ((float)Math.PI / 180F)) * Mth.cos(rotationPitchIn * ((float)Math.PI / 180F));
        this.shoot(f, f1, f2, velocity, inaccuracy);
        Vec3 vector3d = entityThrower.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vector3d.x, entityThrower.isOnGround() ? 0.0D : vector3d.y, vector3d.z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec = new Vec3(x, y, z)
                .normalize()
                .add(
                        this.random.nextGaussian() * 0.0075F * inaccuracy,
                        this.random.nextGaussian() * 0.0075F * inaccuracy,
                        this.random.nextGaussian() * 0.0075F * inaccuracy
                )
                .scale(velocity);
        this.setDeltaMovement(vec);
        float f = (float) vec.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vec.x, vec.z) * (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec.y, f) * (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpMotion(double x, double y, double z) {
        this.setDeltaMovement(x, y, z);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = (float) Math.sqrt(x * x + z * z);
            this.setYRot((float) (Mth.atan2(x, z) * (180F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(y, f) * (180F / (float) Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    public void setThrowData(int slot, ItemStack stack) {
        this.slot = slot;
        this.setStack(stack.copy());
        this.returningDamage = VaultRangLogic.getReturningDamage(this.getStack());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(STACK, new ItemStack(ModItems.RANG));
        this.entityData.define(RETURNING, false);
    }

    protected void checkImpact() {
        if (this.level.isClientSide) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        Vec3 position = this.position();
        Vec3 rayEnd = position.add(motion);

        HitResult blockResult = this.level.clip(new ClipContext(position, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 entityRayEnd = blockResult.getType() == HitResult.Type.MISS ? rayEnd : blockResult.getLocation();

        boolean doEntities = true;
        int tries = 100;

        while (this.isAlive() && (!this.entityData.get(RETURNING) || this.returningDamage > 0.0F)) {
            if (doEntities) {
                EntityHitResult result = this.raycastEntities(position, entityRayEnd);
                if (result != null) {
                    this.onHit(result);
                    return;
                }
                doEntities = false;
            } else {
                if (blockResult.getType() == HitResult.Type.MISS) {
                    return;
                }

                this.onHit(blockResult);
                return;
            }

            if (tries-- <= 0) {
                return;
            }
        }
    }

    @Nullable
    protected EntityHitResult raycastEntities(Vec3 from, Vec3 to) {
        return ProjectileUtil.getEntityHitResult(
                this.level,
                this,
                from,
                to,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D),
                entity -> !entity.isSpectator()
                        && entity.isAlive()
                        && (entity.isPickable() || entity instanceof VaultRangEntity)
                        && entity != this.getThrower()
                        && (this.entitiesHit == null || !this.entitiesHit.contains(entity.getId()))
        );
    }

    @Override
    protected void onHit(@Nonnull HitResult result) {
        LivingEntity owner = this.getThrower();
        if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult hitResult) {
            BlockPos hit = hitResult.getBlockPos();
            BlockState state = this.level.getBlockState(hit);

            if (this.getPiercingModifier() == 0 || state.getMaterial().isSolidBlocking()) {
                this.addHit();
                Vec3 impactPos = hitResult.getLocation().add(Vec3.atLowerCornerOf(hitResult.getDirection().getNormal()).scale(0.15D));
                if (this.hasRicochetRemaining()) {
                    this.queueRicochetChain(null, impactPos);
                } else if (owner instanceof ServerPlayer) {
                    this.clank();
                }
            }
            return;
        }

        if (result.getType() != HitResult.Type.ENTITY || !(result instanceof EntityHitResult hitResult)) {
            return;
        }

        Entity hit = hitResult.getEntity();
        if (hit == owner) {
            return;
        }

        if (hit instanceof VaultRangEntity rang) {
            this.addHit(hit);
            rang.setReturning();
            this.clank();
            return;
        }

        this.addHit(hit);
        boolean clanked = this.attackHitEntity(hit, owner);
        Vec3 impactPos = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);

        if (this.hasRicochetRemaining()) {
            this.queueRicochetChain(hit instanceof LivingEntity living ? living : null, impactPos);
        } else {
            this.applyStandardPostHitMotion();
        }

        if (clanked && !this.entityData.get(RETURNING)) {
            this.clank();
        }
    }

    public void spark() {
        this.setReturning();
    }

    public void clank() {
        this.setReturning();
    }

    public void addHit(Entity entity) {
        if (this.entitiesHit == null) {
            this.entitiesHit = new IntOpenHashSet(5);
        }
        this.entitiesHit.add(entity.getId());
    }

    public void addHit() {
        this.blockHitCount++;
    }

    private void applyStandardPostHitMotion() {
        if ((this.entitiesHit == null ? 0 : this.entitiesHit.size()) + this.blockHitCount > this.getPiercingModifier()) {
            this.setReturning();
        } else if (this.getPiercingModifier() > 0) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        }
    }

    protected void setReturning() {
        this.clearRicochetState();
        this.entityData.set(RETURNING, true);
    }

    private void queueRicochetChain(@Nullable LivingEntity lastHit, Vec3 impactPos) {
        this.clearRicochetState();

        LivingEntity previousTarget = lastHit;
        Vec3 currentImpact = impactPos;
        IntOpenHashSet reservedFreshIds = this.entitiesHit == null ? new IntOpenHashSet() : new IntOpenHashSet(this.entitiesHit);
        int plannedRicochetsUsed = this.ricochetsUsed;

        while (plannedRicochetsUsed < this.getRicochetModifier()) {
            LivingEntity target = this.findRicochetTarget(previousTarget, currentImpact, reservedFreshIds);
            if (target == null) {
                reservedFreshIds.clear();
                target = this.findRicochetTarget(previousTarget, currentImpact, reservedFreshIds);
                if (target == null) {
                    break;
                }
            }

            this.pendingRicochetTargets.add(target.getUUID());
            reservedFreshIds.add(target.getId());
            plannedRicochetsUsed++;
            previousTarget = target;
            currentImpact = this.getRicochetAimPos(target);
        }

        if (this.pendingRicochetTargets.isEmpty()) {
            this.setReturning();
            return;
        }

        this.startNextRicochetHop(impactPos);
    }

    @Nullable
    private LivingEntity findRicochetTarget(@Nullable LivingEntity lastHit, Vec3 impactPos, @Nullable IntOpenHashSet excludedIds) {
        LivingEntity thrower = this.getThrower();
        AABB searchBox = new AABB(impactPos, impactPos).inflate(RICOCHET_SEARCH_RADIUS);
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : this.level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (!candidate.isAlive() || candidate.isSpectator() || candidate instanceof Player || candidate instanceof PetEntity) {
                continue;
            }
            if (candidate == thrower || candidate == lastHit) {
                continue;
            }
            if (excludedIds != null && excludedIds.contains(candidate.getId())) {
                continue;
            }

            Vec3 targetPos = this.getRicochetAimPos(candidate);
            Vec3 direction = targetPos.subtract(impactPos);
            if (direction.lengthSqr() < 1.0E-4D) {
                continue;
            }

            Vec3 start = impactPos.add(direction.normalize().scale(0.05D));
            HitResult obstruction = this.level.clip(new ClipContext(start, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (obstruction.getType() != HitResult.Type.MISS && obstruction.getLocation().distanceToSqr(start) > 0.01D) {
                continue;
            }

            double distance = impactPos.distanceToSqr(targetPos);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private Vec3 getRicochetAimPos(LivingEntity entity) {
        return entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D);
    }

    private boolean hasRicochetRemaining() {
        return !this.entityData.get(RETURNING) && this.ricochetsUsed < this.getRicochetModifier();
    }

    private boolean isRicocheting() {
        return this.currentRicochetTargetId != null;
    }

    private void startNextRicochetHop(Vec3 origin) {
        while (!this.pendingRicochetTargets.isEmpty()) {
            UUID targetId = this.pendingRicochetTargets.remove(0);
            LivingEntity target = this.getRicochetTarget(targetId);
            if (target == null || !target.isAlive()) {
                continue;
            }

            this.currentRicochetTargetId = targetId;
            Vec3 targetPos = this.getRicochetAimPos(target);
            Vec3 direction = targetPos.subtract(origin);
            if (direction.lengthSqr() < 1.0E-4D) {
                continue;
            }

            Vec3 normalized = direction.normalize();
            this.setPos(origin.add(normalized.scale(0.05D)));
            this.setDeltaMovement(normalized.scale(RICOCHET_SPEED));
            this.hasImpulse = true;
            this.deferMovementThisTick = true;
            return;
        }

        this.currentRicochetTargetId = null;
        this.setDeltaMovement(Vec3.ZERO);
        this.setReturning();
    }

    @Nullable
    private LivingEntity getRicochetTarget(UUID targetId) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(targetId);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void tickRicochetFlight() {
        LivingEntity target = this.currentRicochetTargetId == null ? null : this.getRicochetTarget(this.currentRicochetTargetId);
        if (target == null || !target.isAlive()) {
            this.startNextRicochetHop(this.position());
            return;
        }

        Vec3 pos = this.position();
        Vec3 targetPos = this.getRicochetAimPos(target);
        Vec3 direction = targetPos.subtract(pos);

        if (direction.lengthSqr() < 1.0E-4D || direction.length() <= RICOCHET_SPEED) {
            this.finishRicochetHop(target, targetPos);
            return;
        }

        Vec3 motion = direction.normalize().scale(RICOCHET_SPEED);
        this.setDeltaMovement(motion);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                this.level,
                this,
                pos,
                pos.add(motion),
                this.getBoundingBox().expandTowards(motion).inflate(0.6D),
                entity -> entity.getUUID().equals(this.currentRicochetTargetId)
        );

        if (entityHit != null) {
            this.finishRicochetHop(target, targetPos);
            return;
        }

        HitResult blockHit = this.level.clip(new ClipContext(pos, pos.add(motion), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.setReturning();
        }
    }

    private void finishRicochetHop(LivingEntity target, Vec3 impactPos) {
        this.currentRicochetTargetId = null;
        this.ricochetsUsed++;
        this.addHit(target);
        this.setPos(impactPos.x, impactPos.y, impactPos.z);
        this.level.playSound(null, impactPos.x, impactPos.y, impactPos.z, ModSounds.RANG_RICOCHET, SoundSource.PLAYERS, 0.7F, 0.95F + this.random.nextFloat() * 0.15F);
        this.attackHitEntity(target, this.getThrower());
        this.startNextRicochetHop(impactPos);
    }

    private void clearRicochetState() {
        this.currentRicochetTargetId = null;
        this.pendingRicochetTargets.clear();
        this.deferMovementThisTick = false;
    }

    private boolean attackHitEntity(Entity hit, @Nullable LivingEntity owner) {
        ItemStack rang = this.getStack();
        Multimap<Attribute, AttributeModifier> modifiers = rang.getAttributeModifiers(EquipmentSlot.MAINHAND);
        boolean clanked = false;

        if (owner != null) {
            ItemStack prev = owner.getMainHandItem();
            //owner.setItemInHand(InteractionHand.MAIN_HAND, rang);
            //owner.getAttributes().addTransientAttributeModifiers(modifiers);

            int ticksSinceLastSwing = owner.attackStrengthTicker;
            owner.attackStrengthTicker = (int) (1.0D / owner.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0D) + 1;

            float prevHealth = hit instanceof LivingEntity le ? le.getHealth() : 0.0F;

            VaultRangLogic.setActiveRang(this);

            if (owner instanceof Player player) {
                this.hits++;

                Runnable attack = () -> {
                    if (this.entityData.get(RETURNING) && this.hits > 1 && this.returningDamage > 0.0F) {
                        hit.hurt(DamageSource.playerAttack(player), (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * (1.0F + this.returningDamage)));
                    } else {
                        player.attack(hit);
                    }
                };

                if (owner instanceof ServerPlayer serverPlayer
                        && this.slot == player.getInventory().getContainerSize() - 1) {
                    DualWieldSnapshotManager.withOffhandSnapshot(serverPlayer, attack);
                } else {
                    attack.run();
                }
            } else {
                owner.doHurtTarget(hit);
            }

            if (hit instanceof LivingEntity le && le.getHealth() == prevHealth && hit.getType() != TARGET_DUMMY) {
                clanked = true;
            }

            VaultRangLogic.setActiveRang(null);

            owner.attackStrengthTicker = ticksSinceLastSwing;
            if (this.getLevel().dimension().location().getNamespace().equals("the_vault")) {
                if(owner instanceof ServerPlayer player) {
                    this.getStack().hurt(1, this.level.random, player);
                }
            }

            //this.setStack(owner.getMainHandItem());
            //owner.setItemInHand(InteractionHand.MAIN_HAND, prev);
            //owner.getAttributes().addTransientAttributeModifiers(modifiers);
        } else {
            AttributeSupplier.Builder mapBuilder = new AttributeSupplier.Builder();
            mapBuilder.add(Attributes.ATTACK_DAMAGE, 1);
            AttributeSupplier map = mapBuilder.build();
            AttributeMap manager = new AttributeMap(map);
            manager.addTransientAttributeModifiers(modifiers);

            ItemStack stack = this.getStack();
            this.setStack(stack);
            hit.hurt(new IndirectEntityDamageSource("player", this, this).setProjectile(), (float) manager.getValue(Attributes.ATTACK_DAMAGE));
        }

        return clanked;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void tick() {
        Vec3 pos = this.position();
        this.deferMovementThisTick = false;

        this.xOld = pos.x;
        this.yOld = pos.y;
        this.zOld = pos.z;
        super.tick();

        if (this.isRicocheting()) {
            this.tickRicochetFlight();
        } else if (!this.entityData.get(RETURNING) || this.returningDamage > 0.0F) {
            this.checkImpact();
        }

        if (!this.isAlive()) {
            return;
        }

        Vec3 ourMotion = this.getDeltaMovement();
        if (!this.deferMovementThisTick) {
            this.setPos(pos.x + ourMotion.x, pos.y + ourMotion.y, pos.z + ourMotion.z);
        }

        float f = (float) ourMotion.horizontalDistance();
        this.setYRot((float) (Mth.atan2(ourMotion.x, ourMotion.z) * (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(ourMotion.y, f) * (180F / (float) Math.PI)));

        while (this.getXRot() - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }
        while (this.getXRot() - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }
        while (this.getYRot() - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }
        while (this.getYRot() - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.setXRot(Mth.lerp(0.2F, this.xRotO, this.getXRot()));
        this.setYRot(Mth.lerp(0.2F, this.yRotO, this.getYRot()));

        float drag;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.level.addParticle(ParticleTypes.BUBBLE, pos.x - ourMotion.x * 0.25D, pos.y - ourMotion.y * 0.25D, pos.z - ourMotion.z * 0.25D, ourMotion.x, ourMotion.y, ourMotion.z);
            }
            drag = 0.8F;
        } else {
            drag = 0.99F;
        }

        this.setDeltaMovement(ourMotion.scale(drag));

        pos = this.position();
        this.setPos(pos.x, pos.y, pos.z);

        ItemStack stack = this.getStack();
        boolean returning = this.entityData.get(RETURNING);

        if (!this.isRicocheting()) {
            this.liveTime++;
        }

        LivingEntity owner = this.getThrower();
        if (owner == null || !owner.isAlive() || !(owner instanceof Player)) {
            if (!this.level.isClientSide) {
                this.discard();
            }
            return;
        }

        if (!returning) {
            if (!this.isRicocheting()) {
                if (this.liveTime > VaultRangLogic.getTimeout(stack)) {
                    this.setReturning();
                }
                if (!this.level.getWorldBorder().isWithinBounds(this.getBoundingBox())) {
                    this.spark();
                }
            }
            return;
        }

        this.noPhysics = true;

        List<ItemEntity> items = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(2));
        items.removeIf(itemEntity -> itemEntity.getTags().contains("fake_item") || itemEntity.getTags().contains("PreventMagnetMovement"));
        List<ExperienceOrb> xp = this.level.getEntitiesOfClass(ExperienceOrb.class, this.getBoundingBox().inflate(2));

        Vec3 ourPos = this.position();
        for (ItemEntity item : items) {
            if (item.isPassenger() || item instanceof FloatingItemEntity) {
                continue;
            }
            item.startRiding(this);
            item.setPickUpDelay(5);
        }

        for (ExperienceOrb xpOrb : xp) {
            if (xpOrb.isPassenger()) {
                continue;
            }
            xpOrb.startRiding(this);
        }

        Vec3 ownerPos = owner.position().add(0, 1, 0);
        Vec3 motion = ownerPos.subtract(ourPos);
        double motionMag = 3.25D + VaultRangLogic.getVelocity(stack) * 0.25D;

        if (motion.lengthSqr() < motionMag) {
            Player player = (Player) owner;
            Inventory inventory = player.getInventory();

            if (!this.level.isClientSide) {
                this.playSound(QuarkSounds.ENTITY_PICKARANG_PICKUP, 1, 1);

                if (!stack.isEmpty() && player.isAlive()) {
                    int placeholderSlot = this.findInFlightPlaceholderSlot(inventory);
                    if (placeholderSlot >= 0) {
                        inventory.setItem(placeholderSlot, stack);
                        if(stack.getItem() instanceof VaultRangItem rangItem) {
                            rangItem.addCooldown(stack, player);
                        }
                    }
                }

                if (player.isAlive()) {
                    for (ItemEntity item : items) {
                        if (item.isAlive()) {
                            this.giveItemToPlayer(player, item);
                        }
                    }

                    for (ExperienceOrb xpOrb : xp) {
                        if (xpOrb.isAlive()) {
                            xpOrb.playerTouch(player);
                        }
                    }

                    for (Entity riding : this.getPassengers()) {
                        if (!riding.isAlive()) {
                            continue;
                        }

                        if (riding instanceof ItemEntity itemEntity) {
                            this.giveItemToPlayer(player, itemEntity);
                        } else if (riding instanceof ExperienceOrb) {
                            riding.playerTouch(player);
                        }
                    }
                }

                this.discard();
            }
        } else {
            this.setDeltaMovement(motion.normalize().scale(0.7D + VaultRangLogic.getVelocity(stack) * 0.325F));
        }
    }

    private void giveItemToPlayer(Player player, ItemEntity itemEntity) {
        itemEntity.setPickUpDelay(0);
        itemEntity.playerTouch(player);

        if (itemEntity.isAlive()) {
            ItemStack drop = itemEntity.getItem();
            player.drop(drop, false);
            itemEntity.discard();
        }
    }

    private int findInFlightPlaceholderSlot(Inventory inventory) {
        UUID myId = this.getUUID();
        if (this.slot >= 0 && this.slot < inventory.getContainerSize()
                && myId.equals(VaultRangItem.getInFlightEntityId(inventory.getItem(this.slot)))) {
            return this.slot;
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (i == this.slot) {
                continue;
            }
            if (myId.equals(VaultRangItem.getInFlightEntityId(inventory.getItem(i)))) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    public LivingEntity getThrower() {
        if (this.owner == null && this.ownerId != null && this.level instanceof ServerLevel sWorld) {
            Entity entity = sWorld.getEntity(this.ownerId);
            if (entity instanceof LivingEntity livingEntity) {
                this.owner = livingEntity;
            } else {
                this.ownerId = null;
            }
        }

        return this.owner;
    }

    @Override
    protected boolean canAddPassenger(@Nonnull Entity passenger) {
        if (passenger.getTags().contains("fake_item") || passenger.getTags().contains("PreventMagnetMovement")) {
            return false;
        }

        return super.canAddPassenger(passenger) || passenger instanceof ItemEntity || passenger instanceof ExperienceOrb;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Nonnull
    @Override
    public SoundSource getSoundSource() {
        return SoundSource.PLAYERS;
    }

    public int getPiercingModifier() {
        return VaultRangLogic.getPiercing(this.getStack());
    }

    public int getRicochetModifier() {
        return VaultRangLogic.getRicochet(this.getStack());
    }

    public ItemStack getStack() {
        return this.entityData.get(STACK);
    }

    public void setStack(ItemStack stack) {
        this.entityData.set(STACK, stack);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        this.entityData.set(RETURNING, compound.getBoolean(TAG_RETURNING));
        this.liveTime = compound.getInt(TAG_LIVE_TIME);
        this.blockHitCount = compound.getInt(TAG_BLOCKS_BROKEN);
        this.slot = compound.getInt(TAG_RETURN_SLOT);
        this.ricochetsUsed = compound.getInt(TAG_RICOCHETS_USED);

        this.currentRicochetTargetId = compound.contains(TAG_CURRENT_RICOCHET_TARGET, Tag.TAG_INT_ARRAY)
                ? NbtUtils.loadUUID(compound.get(TAG_CURRENT_RICOCHET_TARGET))
                : null;

        this.pendingRicochetTargets.clear();
        ListTag pendingTargets = compound.getList(TAG_PENDING_RICOCHETS, Tag.TAG_INT_ARRAY);
        for (Tag pendingTarget : pendingTargets) {
            this.pendingRicochetTargets.add(NbtUtils.loadUUID(pendingTarget));
        }

        if (compound.contains(TAG_ITEM_STACK)) {
            this.setStack(ItemStack.of(compound.getCompound(TAG_ITEM_STACK)));
        } else {
            this.setStack(new ItemStack(ModItems.RANG));
        }

        if (compound.contains("owner", Tag.TAG_COMPOUND)) {
            Tag owner = compound.get("owner");
            if (owner != null) {
                this.ownerId = NbtUtils.loadUUID(owner);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        compound.putBoolean(TAG_RETURNING, this.entityData.get(RETURNING));
        compound.putInt(TAG_LIVE_TIME, this.liveTime);
        compound.putInt(TAG_BLOCKS_BROKEN, this.blockHitCount);
        compound.putInt(TAG_RETURN_SLOT, this.slot);
        compound.putInt(TAG_RICOCHETS_USED, this.ricochetsUsed);

        if (this.currentRicochetTargetId != null) {
            compound.put(TAG_CURRENT_RICOCHET_TARGET, NbtUtils.createUUID(this.currentRicochetTargetId));
        }

        ListTag pendingTargets = new ListTag();
        for (UUID pendingRicochetTarget : this.pendingRicochetTargets) {
            pendingTargets.add(NbtUtils.createUUID(pendingRicochetTarget));
        }
        compound.put(TAG_PENDING_RICOCHETS, pendingTargets);

        compound.put(TAG_ITEM_STACK, this.getStack().serializeNBT());
        if (this.ownerId != null) {
            compound.put("owner", NbtUtils.createUUID(this.ownerId));
        }
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}