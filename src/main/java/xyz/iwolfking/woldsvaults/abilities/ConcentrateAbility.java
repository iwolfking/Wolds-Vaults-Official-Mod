package xyz.iwolfking.woldsvaults.abilities;

import com.mojang.math.Vector3f;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.util.DelayedExecutionHelper;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.List;
import java.util.function.Supplier;

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

                    if(shouldIgnoreEffect(effect)) {
                        continue;
                    }

                    entity.removeEffect(effect);

                    MobEffect positive = getPositiveEquivalent(effect);

                    Vector3f[] colors = getEffectColors(positive);

                    spawnColoredParticleLine(
                            level,
                            entity.position(),
                            serverPlayer::position,
                            colors[0],
                            colors[1],
                            30,
                            2,
                            () -> {

                                if (positive != null) {
                                    if(serverPlayer.hasEffect(positive)) {
                                        MobEffectInstance currentEffectInstance = serverPlayer.getEffect(positive);

                                        if(currentEffectInstance == null) {
                                            return;
                                        }

                                        currentEffectInstance.update(
                                                new MobEffectInstance(
                                                        positive,
                                                        currentEffectInstance.getDuration() + 200,
                                                        currentEffectInstance.getAmplifier(),
                                                        false,
                                                        true
                                                )
                                        );
                                    }
                                    else {
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
                    );
                }
            }
        });


        return super.doAction(context);
    }


    private static void spawnColoredParticleLine(
            ServerLevel level,
            Vec3 from,
            Supplier<Vec3> toSupplier,
            Vector3f startColor,
            Vector3f endColor,
            int steps,
            int ticksBetween,
            Runnable onArrival
    ) {
        Vec3 mid = from.add(0, 1.0, 0);
        Vec3 controlOffset = new Vec3(
                (level.random.nextDouble() - 0.5),
                1.5 + level.random.nextDouble(),
                (level.random.nextDouble() - 0.5)
        );

        double speedExponent = 2.0;

        for (int i = 0; i <= steps; i++) {
            final int stepIndex = i;

            double linearT = (double) stepIndex / steps;
            double t = Math.pow(linearT, 1.0 / speedExponent);

            DelayedExecutionHelper.schedule(level, stepIndex * ticksBetween, () -> {
                Vec3 to = toSupplier.get();
                Vec3 diff = to.subtract(from);
                Vec3 control = mid.add(controlOffset);

                Vec3 pos = from.scale((1 - t) * (1 - t))
                        .add(control.scale(2 * (1 - t) * t))
                        .add(to.scale(t * t));

                float r = (float) (startColor.x() + t * (endColor.x() - startColor.x()));
                float g = (float) (startColor.y() + t * (endColor.y() - startColor.y()));
                float b = (float) (startColor.z() + t * (endColor.z() - startColor.z()));
                DustParticleOptions particle = new DustParticleOptions(new Vector3f(r, g, b), 1.0F);

                level.sendParticles(particle, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0);

                if (stepIndex == steps && onArrival != null) {
                    onArrival.run();
                }
            });
        }
    }

    private static MobEffect getPositiveEquivalent(MobEffect effect) {
        if (effect == MobEffects.MOVEMENT_SLOWDOWN) return MobEffects.MOVEMENT_SPEED;
        if (effect == MobEffects.WEAKNESS) return MobEffects.DAMAGE_BOOST;
        if (effect == MobEffects.POISON) return MobEffects.REGENERATION;
        return MobEffects.REGENERATION;
    }

    private static Vector3f[] getEffectColors(MobEffect effect) {
        if (effect == MobEffects.REGENERATION) {
            return new Vector3f[] {
                    new Vector3f(0.3F, 0.9F, 0.3F),
                    new Vector3f(1.0F, 0.84F, 0.0F)
            };
        }
        if (effect == MobEffects.MOVEMENT_SPEED) {
            return new Vector3f[] {
                    new Vector3f(0.2F, 0.7F, 1.0F),
                    new Vector3f(1.0F, 1.0F, 1.0F)
            };
        }
        if (effect == MobEffects.DAMAGE_BOOST) {
            return new Vector3f[] {
                    new Vector3f(1.0F, 0.2F, 0.2F),
                    new Vector3f(1.0F, 0.6F, 0.1F)
            };
        }
        if (effect == MobEffects.ABSORPTION) {
            return new Vector3f[] {
                    new Vector3f(1.0F, 0.84F, 0.0F),
                    new Vector3f(0.2F, 0.8F, 1.0F)
            };
        }
        return new Vector3f[] {
                new Vector3f(1.0F, 1.0F, 1.0F),
                new Vector3f(1.0F, 0.9F, 0.4F)
        };
    }

    private static boolean shouldIgnoreEffect(MobEffect effect) {
        return effect.equals(ModEffects.REAVING);
    }
}
