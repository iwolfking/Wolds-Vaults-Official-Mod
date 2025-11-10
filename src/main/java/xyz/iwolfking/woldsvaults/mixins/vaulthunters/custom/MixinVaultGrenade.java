package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.entity.entity.VaultGrenade;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;

@Mixin(value = VaultGrenade.class, remap = false)
public abstract class MixinVaultGrenade extends ThrowableItemProjectile {
    public MixinVaultGrenade(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultGrenade;createSafeExplosion(Lnet/minecraft/server/level/ServerPlayer;DDDF)V", ordinal = 0))
    private void explodeEffectClouds(CallbackInfo ci) {
        if(this.getOwner() instanceof ServerPlayer player) {
            WoldAttributeHelper.withSnapshot(
                    player,
                    true,
                    (playerEntity, snapshot) -> snapshot.getAttributeValue(ModGearAttributes.EFFECT_CLOUD_WHEN_HIT, VaultGearAttributeTypeMerger.asList())
                            .forEach(cloud -> {
                                MobEffect effect = cloud.getPrimaryEffect();
                                if (effect != null) {
                                        EffectCloudEntity cloudEntity = new EffectCloudEntity(playerEntity.getLevel(), this.getX(), this.getY(), this.getZ());
                                        cloud.apply(cloudEntity);
                                        cloudEntity.setRadius(AreaOfEffectHelper.adjustAreaOfEffect(playerEntity, null, cloudEntity.getRadius()));
                                        cloudEntity.setOwner(playerEntity);
                                        cloudEntity.setDuration(EffectDurationHelper.adjustEffectDurationFloor(playerEntity, cloudEntity.getDuration()));
                                        playerEntity.getLevel().addFreshEntity(cloudEntity);
                                }
                            })
            );

            WoldAttributeHelper.withSnapshot(
                    player,
                    true,
                    (playerEntity, snapshot) -> snapshot.getAttributeValue(ModGearAttributes.EFFECT_CLOUD, VaultGearAttributeTypeMerger.asList())
                            .forEach(cloud -> {
                                MobEffect effect = cloud.getPrimaryEffect();
                                if (effect != null) {
                                    EffectCloudEntity cloudEntity = new EffectCloudEntity(playerEntity.getLevel(), this.getX(), this.getY(), this.getZ());
                                    cloud.apply(cloudEntity);
                                    cloudEntity.setRadius(AreaOfEffectHelper.adjustAreaOfEffect(playerEntity, null, cloudEntity.getRadius()));
                                    cloudEntity.setOwner(playerEntity);
                                    cloudEntity.setDuration(EffectDurationHelper.adjustEffectDurationFloor(playerEntity, cloudEntity.getDuration()));
                                    playerEntity.getLevel().addFreshEntity(cloudEntity);
                                }
                            })
            );
        }
    }


}
