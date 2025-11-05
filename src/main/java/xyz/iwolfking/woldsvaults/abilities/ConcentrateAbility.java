package xyz.iwolfking.woldsvaults.abilities;

import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConcentrateAbility extends InstantManaAbility {
    public ConcentrateAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
    }

    public ConcentrateAbility() {
    }

    @Override
    protected ActionResult doAction(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(serverPlayer -> {
            ServerLevel level = (ServerLevel) serverPlayer.level;

            double radius = 8.0;
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    serverPlayer.getBoundingBox().inflate(radius),
                    e -> e != serverPlayer
            );

            for (LivingEntity entity : entities) {
                for (MobEffectInstance effectInstance : entity.getActiveEffects()) {
                    MobEffect effect = effectInstance.getEffect();
                    MobEffect positive = getPositiveEquivalent(effect);

                    spawnParticleLine(level, entity.position(), serverPlayer.position(), ParticleTypes.HAPPY_VILLAGER, 20);

                    if (positive != null) {
                        serverPlayer.addEffect(new MobEffectInstance(
                                positive,
                                200,
                                0,
                                false,
                                true
                        ));
                    }
                }
            }
        });


        return super.doAction(context);
    }

    private static void spawnParticleLine(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int steps) {
        Vec3 diff = to.subtract(from);
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = from.add(diff.scale(progress));
            level.sendParticles(particle, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private static MobEffect getPositiveEquivalent(MobEffect effect) {
        if (effect == MobEffects.MOVEMENT_SLOWDOWN) return MobEffects.MOVEMENT_SPEED;
        if (effect == MobEffects.WEAKNESS) return MobEffects.DAMAGE_BOOST;
        if (effect == MobEffects.POISON) return MobEffects.REGENERATION;
        return MobEffects.REGENERATION;
    }
}
