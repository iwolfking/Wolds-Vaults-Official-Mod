package xyz.iwolfking.woldsvaults.effect.mobeffects;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import javax.annotation.Nullable;
import java.util.UUID;

public class PercentBurnEffect extends MobEffect {

    public static final String BURN_ATTACK_SNAPSHOT = "PercentBurnAttackDamage";
    public static final String BURN_SOURCE_UUID = "PercentBurnSourceUUID";

    public PercentBurnEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4500);
        this.setRegistryName(WoldsVaults.id("burn"));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level.isClientSide) return;

        CompoundTag data = target.getPersistentData();
        if (!data.contains(BURN_ATTACK_SNAPSHOT)) {
            WoldsVaults.LOGGER.info("No burn snapshot");
            return;
        }

        float attackSnapshot = data.getFloat(BURN_ATTACK_SNAPSHOT);
        if (attackSnapshot <= 0.0f) {
            WoldsVaults.LOGGER.info("Snapshot of 0 damage");
            return;
        }

        float percentPerSecond = 0.10f + (0.10f * amplifier);
        float damage = attackSnapshot * percentPerSecond;

        DamageSource source;

        if (data.hasUUID(BURN_SOURCE_UUID) && target.level instanceof ServerLevel serverLevel) {
            UUID sourceId = data.getUUID(BURN_SOURCE_UUID);
            Entity sourceEntity = serverLevel.getEntity(sourceId);

            if (sourceEntity instanceof ServerPlayer serverPlayer) {
                source = DamageSource.playerAttack(serverPlayer);
            } else if (sourceEntity instanceof LivingEntity livingSource) {
                source = DamageSource.mobAttack(livingSource);
            } else {
                source = DamageSource.MAGIC;
            }
        } else {
            source = DamageSource.MAGIC;
        }

        WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE.runWithFlag(() -> {
            ActiveFlags.IS_AP_ATTACKING.push();
            target.hurt(source, damage);
            ActiveFlags.IS_AP_ATTACKING.pop();
        });

        spawnFireParticles(target);
    }

    private void spawnFireParticles(LivingEntity entity) {
        if (!(entity.level instanceof ServerLevel level)) return;

        level.sendParticles(
                ParticleTypes.FLAME,
                entity.getX(),
                entity.getY() + entity.getBbHeight() * 0.6,
                entity.getZ(),
                2,
                0.2, 0.3, 0.2,
                0.01
        );
    }

    public static void applyBurnEffect(LivingEntity target, int durationTicks, float snapshotValue, @Nullable LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(
                ModEffects.BURN,
                durationTicks,
                0,
                false,
                true,
                true
        ));

        CompoundTag data = target.getPersistentData();
        data.putFloat(BURN_ATTACK_SNAPSHOT, snapshotValue);

        if (attacker != null) {
            data.putUUID(BURN_SOURCE_UUID, attacker.getUUID());
        }
    }

    public static void applyPercentBurn(LivingEntity target, LivingEntity attacker, int durationTicks, double snapshotValue) {
        applyBurnEffect(target, durationTicks, (float) snapshotValue, attacker);
    }

    public static void applyPercentBurn(LivingEntity target, LivingEntity attacker, int durationTicks, Attribute scalingAttribute) {
        double damage = attacker != null ? attacker.getAttributeValue(scalingAttribute) : 0.0;
        applyPercentBurn(target, attacker, durationTicks, damage);
    }

    public static void applyPercentBurn(LivingEntity target, LivingEntity attacker, int durationTicks) {
        applyPercentBurn(target, attacker, durationTicks, Attributes.ATTACK_DAMAGE);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        CompoundTag data = entity.getPersistentData();
        data.remove(BURN_ATTACK_SNAPSHOT);
        data.remove(BURN_SOURCE_UUID);
    }
}