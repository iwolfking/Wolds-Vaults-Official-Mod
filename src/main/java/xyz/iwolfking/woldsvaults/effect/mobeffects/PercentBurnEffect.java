package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModEffects;

public class PercentBurnEffect extends MobEffect {

    public static final String BURN_ATTACK_SNAPSHOT = "PercentBurnAttackDamage";


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

        var data = target.getPersistentData();
        if (!data.contains(BURN_ATTACK_SNAPSHOT)) return;

        float attackSnapshot = data.getFloat(BURN_ATTACK_SNAPSHOT);

        float percentPerSecond = 0.10f  + (0.1F * amplifier);
        float damage = attackSnapshot * percentPerSecond;

        target.hurt(DamageSource.MAGIC, damage);
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

    public static void applyPercentBurn(
            LivingEntity target,
            LivingEntity attacker,
            int durationTicks
    ) {
        double attackDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

        target.getPersistentData().putFloat(
                BURN_ATTACK_SNAPSHOT,
                (float) attackDamage
        );

        target.addEffect(new MobEffectInstance(
                ModEffects.BURN,
                durationTicks,
                0,
                false,
                true,
                true
        ));
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity entity,
            AttributeMap attributes,
            int amplifier
    ) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        entity.getPersistentData().remove(BURN_ATTACK_SNAPSHOT);
    }

}
