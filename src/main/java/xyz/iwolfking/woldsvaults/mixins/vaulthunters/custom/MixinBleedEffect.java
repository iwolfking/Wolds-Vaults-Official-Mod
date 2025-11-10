package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.effect.BleedEffect;
import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.api.util.VaultMobUtils;

@Mixin(value = BleedEffect.class, remap = false)
public class MixinBleedEffect {
    /**
     * @author iwolfking
     * @reason Change Bleed to do a portion of max health per damage tick instead.
     */
    @Overwrite(remap = true)
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide && !entity.isDeadOrDying()) {
            MobEffectInstance instance = entity.getEffect(ModEffects.BLEED);
            if (instance != null) {
                if (instance.getDuration() % 40 == 0) {
                    float maxHealthPercent = instance.getAmplifier() * 0.025F;

                    if(VaultMobUtils.isSpecialMob(entity)) {
                        maxHealthPercent = maxHealthPercent * 0.5f;
                    }

                    float healthReduction = Math.max((entity.getMaxHealth() * maxHealthPercent), instance.getAmplifier() + 1);

                    entity.setHealth(entity.getHealth() - healthReduction);
                    if (entity.isDeadOrDying()) {
                        entity.die(DamageSource.MAGIC);
                    }
                }

            }
        }
    }
}
