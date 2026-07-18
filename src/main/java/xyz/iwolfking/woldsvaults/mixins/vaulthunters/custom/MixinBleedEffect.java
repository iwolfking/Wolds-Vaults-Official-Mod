package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.effect.BleedEffect;
import iskallia.vault.entity.boss.TheVesselEntity;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.damage.DamageUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.api.util.BleedSourceRegistry;
import xyz.iwolfking.woldsvaults.api.util.VaultMobUtils;

@Mixin(value = BleedEffect.class, remap = false)
public class MixinBleedEffect {
    /**
     * @author iwolfking
     * @reason Change Bleed to do a portion of max health per damage tick instead, attributed
     * to the applier for kill credit and scaled down against major bosses and the Vessel.
     */
    @Overwrite(remap = true)
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide && !entity.isDeadOrDying()) {
            MobEffectInstance instance = entity.getEffect(ModEffects.BLEED);
            if (instance != null) {
                if (instance.getDuration() % 40 == 0) {
                    float maxHealthPercent = instance.getAmplifier() * 0.025F;

                    if (entity instanceof TheVesselEntity) {
                        maxHealthPercent = maxHealthPercent * 0.01F;
                    }
                    else if (VaultMobUtils.isMajorBoss(entity)) {
                        maxHealthPercent = maxHealthPercent * 0.1F;
                    }
                    else if (VaultMobUtils.isSpecialMob(entity)) {
                        maxHealthPercent = maxHealthPercent * 0.5F;
                    }

                    float healthReduction = Math.max((entity.getMaxHealth() * maxHealthPercent), instance.getAmplifier() + 1);
                    DamageSource source = BleedSourceRegistry.resolveDamageSource(entity);
                    Runnable damageTask = () -> DamageUtil.shotgunAttack(entity, e -> e.hurt(source, healthReduction));

                    if (source.getEntity() instanceof ServerPlayer) {
                        ActiveFlags.IS_AP_ATTACKING.runIfNotSet(() -> ActiveFlags.IS_EFFECT_ATTACKING.runIfNotSet(damageTask));
                    } else {
                        ActiveFlags.IS_EFFECT_ATTACKING.runIfNotSet(damageTask);
                    }
                }

                if (instance.getDuration() <= 1) {
                    BleedSourceRegistry.removeSource(entity);
                }
            }
        }
    }
}
