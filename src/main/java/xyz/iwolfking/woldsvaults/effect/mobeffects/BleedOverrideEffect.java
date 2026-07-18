package xyz.iwolfking.woldsvaults.effect.mobeffects;

import iskallia.vault.VaultMod;
import iskallia.vault.init.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.effect.SourceMobEffect;
import xyz.iwolfking.woldsvaults.api.util.MaxHealthDamageHelper;

import java.util.UUID;

public class BleedOverrideEffect extends SourceMobEffect {

    public BleedOverrideEffect() {
        super(MobEffectCategory.HARMFUL, 16711680);
        this.setRegistryName(VaultMod.id("bleed"));
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(ModEffects.BLEED);
        if (instance != null) {
            int duration = instance.getDuration();
            UUID sourceId = getSources().get(entity.getUUID());
            DamageSource source;
            if (sourceId != null && entity.level instanceof ServerLevel) {
                Entity sourceEntity = ((ServerLevel)entity.level).getEntity(sourceId);
                if (sourceEntity instanceof ServerPlayer) {
                    source = DamageSource.playerAttack((ServerPlayer)sourceEntity);
                } else if (sourceEntity != null) {
                    source = DamageSource.mobAttack((LivingEntity)sourceEntity);
                } else {
                    source = DamageSource.MAGIC;
                }
            } else {
                source = DamageSource.MAGIC;
            }


            if (duration % 40 == 0) {
                WoldsVaults.LOGGER.info("APPLYING BLEEEEED");
                MaxHealthDamageHelper.applyBleedDamage(instance, entity, source.getEntity());
            }


            if (duration <= 1) {
                removeSource(entity);
            }
        }
    }
}
