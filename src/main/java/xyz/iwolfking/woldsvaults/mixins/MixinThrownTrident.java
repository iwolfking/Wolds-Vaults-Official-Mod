package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;
import xyz.iwolfking.woldsvaults.mixins.accessors.AbstractArrowAccessor;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.ThrownTridentAttackDamage;

import java.util.Random;

@Mixin(ThrownTrident.class)
public abstract class MixinThrownTrident extends AbstractArrow implements ThrownTridentAttackDamage {
    @Shadow public int clientSideReturnTridentTickCount;
    @Shadow private ItemStack tridentItem;
    @Shadow private boolean dealtDamage;

    private static Random random = new Random();

    @Unique private int woldsVaults$pierced;
    @Unique private float woldsVaults$throwAttackDamage;
    @Unique private static boolean woldsVaults$warnedThrowAttackDamage;

    protected MixinThrownTrident(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickVaultTrident(CallbackInfo ci) {
        if(this.tridentItem != null && this.tridentItem.getItem() instanceof VaultTridentItem) {
            if (this.inGroundTime > 4) {
                this.dealtDamage = true;
            }

            Entity entity = this.getOwner();

            int i = VaultGearData.read(this.tridentItem).get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRIDENT_LOYALTY, VaultGearAttributeTypeMerger.intSum());
            if (entity != null && i > 0 && (this.dealtDamage || this.isNoPhysics()) ) {
                if (!this.isAcceptibleReturnOwner()) {
                    if (!this.level.isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                        this.spawnAtLocation(this.getPickupItem(), 0.1F);
                    }

                    this.discard();
                } else {
                    this.setNoPhysics(true);
                    Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                    this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015D * i, this.getZ());
                    if (this.level.isClientSide) {
                        this.yOld = this.getY();
                    }

                    double d0 = 0.05D * i;
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3.normalize().scale(d0)));
                    if (this.clientSideReturnTridentTickCount == 0) {
                        this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                    }

                    ++this.clientSideReturnTridentTickCount;
                }
            }

            super.tick();
            ci.cancel();
        }

    }
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void onHitEntityWithVaultTrident(EntityHitResult pResult, CallbackInfo ci) {
        if(this.tridentItem.getItem() instanceof VaultTridentItem) {
            if(!(pResult.getEntity() instanceof LivingEntity)) {
                woldsVaults$ignore(pResult.getEntity());
                ci.cancel();
                return;
            }
            Entity entity = pResult.getEntity();
            VaultGearData data = VaultGearData.read(tridentItem);
            Double f = data.get(ModGearAttributes.ATTACK_DAMAGE, VaultGearAttributeTypeMerger.doubleSum());


            Entity entity1 = this.getOwner();

            int pierce = !this.level.isClientSide && entity1 instanceof LivingEntity owner ? LightningFiend.pierce(owner) : 0;
            boolean piercing = pierce > 0 && this.woldsVaults$pierced < pierce;
            if (!piercing) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
            }
            float f1 = 1.0F;
            float throwDamage = entity1 instanceof LivingEntity owner ? woldsVaults$throwAttackDamage(owner) : woldsVaults$ownerlessDamage(f.floatValue());
            float strikeDamage = throwDamage * (entity1 instanceof LivingEntity owner ? LightningFiend.channelingMultiplier(owner) : 1.0F);
            if (entity1 instanceof ServerPlayer player && entity instanceof LivingEntity living) {
                LightningFiend.onThrowHit(player, living);
            }
            if (this.level instanceof ServerLevel && VaultTridentItem.isVaultTridentChanneling(this.tridentItem)) {
                if (piercing && entity1 instanceof ServerPlayer player && entity instanceof LivingEntity living) {
                    LightningFiend.strike(player, living, strikeDamage);
                    LightningFiend.chain(player, living, strikeDamage);
                } else if(VaultTridentItem.shouldTriggerChanneling(data)) {
                    woldsVaults$triggerChannelingStrike(entity, entity1, strikeDamage);
                    if (entity1 instanceof ServerPlayer player && entity instanceof LivingEntity living) {
                        LightningFiend.chain(player, living, strikeDamage);
                    }

                    if (entity1 instanceof LivingEntity livingEntity) {
                        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(livingEntity);
                        float judgementValue = snapshot.getAttributeValue(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SECOND_JUDGEMENT, VaultGearAttributeTypeMerger.floatSum());

                        if (random.nextFloat() < judgementValue) {
                            woldsVaults$triggerChannelingStrike(entity, entity, strikeDamage);
                        }
                    }
                }
            }


            DamageSource damagesource = DamageSource.trident(this, entity1 == null ? this : entity1);
            if (piercing) {
                this.woldsVaults$pierced++;
                this.setPierceLevel((byte) Math.max(this.getPierceLevel(), pierce));
                AbstractArrowAccessor arrow = (AbstractArrowAccessor) (Object) this;
                if (arrow.getPiercingIgnoreEntityIds() == null) {
                    arrow.setPiercingIgnoreEntityIds(new IntOpenHashSet(5));
                }
                arrow.getPiercingIgnoreEntityIds().add(entity.getId());
            } else {
                this.dealtDamage = true;
                this.setPierceLevel((byte) 0);
            }
            SoundEvent soundevent = SoundEvents.TRIDENT_HIT;
            if (entity.hurt(damagesource, throwDamage)) {

                if (entity instanceof LivingEntity livingentity) {
                    if (entity1 instanceof LivingEntity livingentity1) {
                        EnchantmentHelper.doPostHurtEffects(livingentity, livingentity1);
                        EnchantmentHelper.doPostDamageEffects(livingentity1, livingentity);
                    }

                    this.doPostHurtEffects(livingentity);
                }
            }

            this.playSound(soundevent, f1, 1.0F);
            ci.cancel();
        }
    }

    /**
     * Records an entity the trident passed without hitting, so the arrow hit loop moves past it instead of finding it
     * again every iteration once the trident pierces.
     */
    @Unique
    private void woldsVaults$ignore(Entity entity) {
        AbstractArrowAccessor arrow = (AbstractArrowAccessor) (Object) this;
        if (arrow.getPiercingIgnoreEntityIds() == null) {
            arrow.setPiercingIgnoreEntityIds(new IntOpenHashSet(5));
        }
        arrow.getPiercingIgnoreEntityIds().add(entity.getId());
    }

    /**
     * The base damage of this throw's impact and channeling strikes: the thrower's attack damage at the throw. A trident
     * thrown without a recorded attack damage falls back to the owner's current attack damage.
     */
    @Unique
    private float woldsVaults$throwAttackDamage(LivingEntity owner) {
        float attackDamage = this.woldsVaults$throwAttackDamage;
        if (attackDamage <= 0.0F) {
            attackDamage = LightningFiend.attackDamage(owner);
            if (!woldsVaults$warnedThrowAttackDamage) {
                woldsVaults$warnedThrowAttackDamage = true;
                WoldsVaults.LOGGER.warn("A vault trident hit without a recorded throw attack damage; using the owner's current attack damage {}.", attackDamage);
            }
        }
        return attackDamage;
    }

    /** A vault trident with no living owner hits for its own attack damage roll. */
    @Unique
    private static float woldsVaults$ownerlessDamage(float tridentDamage) {
        WoldsVaults.LOGGER.warn("A vault trident hit with no living owner; using its own attack damage roll {}.", tridentDamage);
        return tridentDamage;
    }

    @Override
    public void woldsVaults$setThrowAttackDamage(float attackDamage) {
        this.woldsVaults$throwAttackDamage = attackDamage;
    }

    @Override
    public float woldsVaults$getThrowAttackDamage() {
        return this.woldsVaults$throwAttackDamage;
    }

    @Unique
    private void woldsVaults$triggerChannelingStrike(Entity target, Entity causeEntity, float damage) {
        BlockPos blockpos = target.blockPosition();
        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(this.level);
        if(lightningbolt != null) {
            lightningbolt.setDamage(damage);
        }
        lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
        lightningbolt.setCause(causeEntity instanceof ServerPlayer ? (ServerPlayer)causeEntity : null);
        this.level.addFreshEntity(lightningbolt);
        SoundEvent soundevent = SoundEvents.TRIDENT_THUNDER;
        float f1 = 5.0F;
        LivingEntity attacker = (LivingEntity) this.getOwner();

        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(attacker);
        (snapshot.getAttributeValue(ModGearAttributes.EFFECT_CLOUD, VaultGearAttributeTypeMerger.asList())).forEach(cloud -> {

                    MobEffect effect = cloud.getPrimaryEffect();
                    if (effect == null) {
                        return;
                    }
                    EffectCloudEntity cloudEntity = new EffectCloudEntity(attacker.getLevel(), blockpos.getX(), blockpos.getY(), blockpos.getZ());
                    cloud.apply(cloudEntity);
                    cloudEntity.setOwner(attacker);
                    attacker.getLevel().addFreshEntity(cloudEntity);
                }
        );

        this.playSound(soundevent, f1, 1.0F);
    }

    @Shadow protected abstract boolean isAcceptibleReturnOwner();
}
