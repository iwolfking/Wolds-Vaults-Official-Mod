package xyz.iwolfking.woldsvaults.entities.projectiles;

import iskallia.vault.entity.boss.ArtifactBossEntity;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.entity.FloatingItemEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.event.PlayerActiveFlags;
import iskallia.vault.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.mixins.accessors.EvokerFangsAccessor;

public class CustomFangEntity extends EvokerFangs {
    private float customDamage = 6.0F;
    private float executeThreshold = 0.0F;
    private int effectDuration = 180;
    private int effectAmplifier = 0;
    private boolean isMaw = false;
    private boolean isProcFang = false;



    public CustomFangEntity(Level level, double x, double y, double z, float yRot, LivingEntity owner, float damage, float executeThreshold, int effectDuration, int effectAmplifier, boolean isMaw, boolean isProcFang) {
        this(ModEntities.CUSTOM_FANGS, level);
        this.setPos(x, y, z);
        this.setYRot(yRot);
        this.customDamage = damage;
        this.executeThreshold = executeThreshold;
        this.isMaw = isMaw;
        this.isProcFang = isProcFang;
        this.effectDuration = effectDuration;
        this.effectAmplifier = effectAmplifier;
        this.setOwner(owner);
    }

    public CustomFangEntity(EntityType<?> entityEntityType, Level level) {
        super((EntityType<? extends EvokerFangs>) entityEntityType, level);
    }

    public void tick() {
        if (this.level.isClientSide) {
            ((EvokerFangsAccessor)this).setLifeTicks(((EvokerFangsAccessor) this).getLifeTicks() - 1);
            if (((EvokerFangsAccessor)this).getClientSideAttackStarted()) {
                if (((EvokerFangsAccessor)this).getLifeTicks() == 14) {
                    for(int i = 0; i < 12; ++i) {
                        double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                        double d1 = this.getY() + 0.05D + this.random.nextDouble();
                        double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                        double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                        double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                        this.level.addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
                    }
                }
            }
        }
        else if (((EvokerFangsAccessor)this).getWarmupDelayTicks() - 1 < 0) {
            if (((EvokerFangsAccessor)this).getWarmupDelayTicks() == -8) {
                for(LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
                    this.dealDamageTo(livingentity);
                }
            }

            if (!((EvokerFangsAccessor)this).getSentSpikeEvent()) {
                this.level.broadcastEntityEvent(this, (byte)4);
                ((EvokerFangsAccessor)this).setSentSpikeEvent(true);
            }

            if (((EvokerFangsAccessor)this).getLifeTicks() - 1 < 0) {
                this.discard();
            }
        }
        ((EvokerFangsAccessor)this).setWarmupDelayTicks(((EvokerFangsAccessor) this).getWarmupDelayTicks() - 1);
        ((EvokerFangsAccessor)this).setLifeTicks(((EvokerFangsAccessor) this).getLifeTicks() - 1);
    }

    private void dealDamageTo(LivingEntity pTarget) {
        LivingEntity owner = this.getOwner();

        if(pTarget == owner || pTarget instanceof Player || !pTarget.isAlive() || pTarget.isInvulnerable() || !(owner instanceof Player player)) {
            return;
        }

        if (this.isMaw) {
            double distance = owner.position().distanceTo(pTarget.position());

            if (distance > 1.5) {
                Vec3 pullDir = owner.position().subtract(pTarget.position()).normalize().scale(0.95);
                pTarget.setDeltaMovement(new Vec3(pullDir.x, 0.2, pullDir.z));
                pTarget.hurtMarked = true;
            }

            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, effectDuration, effectAmplifier + 2));

            if (distance >= 5.0 && distance < 10.0) {
                pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effectDuration, effectAmplifier + 1));
                ((ServerLevel)this.level).sendParticles(ParticleTypes.DRAGON_BREATH, pTarget.getX(), pTarget.getEyeY(), pTarget.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
            }

            else if (distance >= 10.0) {
                pTarget.addEffect(new MobEffectInstance(iskallia.vault.init.ModEffects.VULNERABLE, effectDuration, effectAmplifier));

                ((ServerLevel)this.level).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pTarget.getX(), pTarget.getEyeY(), pTarget.getZ(), 5, 0.2, 0.2, 0.2, 0.03);
            }

            if(owner.getRandom().nextFloat() <= executeThreshold) {
                spawnHeartFragment(pTarget);
            }
        }

        WoldActiveFlags.IS_UNLUCKY_ATTACK.maybeRunWithFlag(isProcFang, () ->
            WoldActiveFlags.IS_PROC_FANG_ATTACKING.maybeRunWithFlag(isProcFang, () ->
                WoldActiveFlags.IS_FANG_ATTACKING.runWithFlag(() -> {
                    pTarget.invulnerableTime = 0;
                    pTarget.hurt(DamageSource.playerAttack(player), customDamage);
                })
            )
        );

        if (!isMaw) {

            float targetHealthPercent = pTarget.getHealth() / pTarget.getMaxHealth();

            if (this.executeThreshold > 0 && targetHealthPercent <= this.executeThreshold) {

                if(pTarget instanceof TheVesselEntity || pTarget instanceof VaultBossBaseEntity) {
                    spawnHeartFragment(pTarget);
                    return;
                }

                ServerLevel serverLevel = (ServerLevel) this.level;
                serverLevel.sendParticles(ParticleTypes.SOUL, pTarget.getX(), pTarget.getY() + 1.0, pTarget.getZ(), 20, 0.2, 0.5, 0.2, 0.05);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pTarget.getX(), pTarget.getY() + 1.0, pTarget.getZ(), 10, 0.3, 0.3, 0.3, 0.02);

                serverLevel.playSound(null, pTarget.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 2.0F);

                WoldActiveFlags.IS_UNLUCKY_ATTACK.runWithFlag(() ->
                    WoldActiveFlags.IS_PROC_FANG_ATTACKING.runWithFlag(() ->
                        WoldActiveFlags.IS_FANG_ATTACKING.runWithFlag(() ->
                            pTarget.hurt(DamageSource.playerAttack(player), Float.MAX_VALUE)
                        )
                    )
                );

                spawnHeartFragment(pTarget);

            }

        }
    }

    private void spawnHeartFragment(LivingEntity target) {
        ItemStack heartFragment = new ItemStack(ModItems.HEART_FRAGMENT);

        FloatingItemEntity floatingItem = FloatingItemEntity.create(
                target.level,
                new BlockPos(target.getX(), target.getY() + 0.4F, target.getZ()),
                heartFragment
        ).setDroppingParticles(true);

        floatingItem.setColor(11540247, 7669511);
        target.level.addFreshEntity(floatingItem);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("CustomDamage", this.customDamage);
        nbt.putFloat("ExecuteThreshold", this.executeThreshold);
        nbt.putInt("EffectDuration", this.effectDuration);
        nbt.putInt("EffectAmplifier", this.effectAmplifier);
        nbt.putBoolean("IsMaw", this.isMaw);
        nbt.putBoolean("IsProcFang", this.isProcFang);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.customDamage = nbt.getFloat("CustomDamage");
        this.executeThreshold = nbt.getFloat("ExecuteThreshold");
        this.effectDuration = nbt.getInt("EffectDuration");
        this.effectAmplifier = nbt.getInt("EffectAmplifier");
        this.isMaw = nbt.getBoolean("IsMaw");
        this.isProcFang = nbt.getBoolean("IsProcFang");
    }
}