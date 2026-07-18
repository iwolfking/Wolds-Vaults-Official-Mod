package xyz.iwolfking.woldsvaults.abilities;

import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingGearAttributes;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;
import xyz.iwolfking.woldsvaults.api.util.DelayedExecutionHelper;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class ConcentrateAbility extends InstantManaAbility {

    private double radius;
    private int baseEffectDuration;
    private int baseAmplitude;
    private float amplitudeScaleChance;
    private int particleSteps;
    private int particleDelay;

    private final int MAX_EFFECT_DURATION = 600;
    private static final List<MobEffect> EFFECTS = List.of(ModEffects.QUICKENING, ModEffects.EMPOWER, ModEffects.STEADFAST, ModEffects.ARMORED, ModEffects.BLITZ);

    public ConcentrateAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost, double radius, int baseEffectDuration, int baseAmplitude, float amplitudeScaleChance, int particleSteps, int particleDelay) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.radius = radius;
        this.baseEffectDuration = baseEffectDuration;
        this.baseAmplitude = baseAmplitude;
        this.amplitudeScaleChance = amplitudeScaleChance;
        this.particleSteps = particleSteps;
        this.particleDelay = particleDelay;
    }



    public ConcentrateAbility() {
    }

    @Override
    protected ActionResult doAction(SkillContext context) {
        context.getSource().as(ServerPlayer.class).map(serverPlayer -> {

            ServerLevel level = (ServerLevel) serverPlayer.level;

            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.8F, 1.2F);


            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    serverPlayer.getBoundingBox().inflate(AreaOfEffectHelper.adjustAreaOfEffect(serverPlayer, this, (float)getRadius())),
                    e -> !(e instanceof ServerPlayer || e instanceof PetEntity)
            );

            if(entities.isEmpty()) {
                return ActionResult.fail();
            }

            VaultGearAttributeInstance<Float> drainEtchingAttribute = EtchingHelper.getEtchings(serverPlayer, ModEtchingGearAttributes.CONCENTRATE_DRAIN).stream().findFirst().orElse(null);

            List<Pair<Vec3, MobEffect>> effectsToProcess = new ArrayList<>();
            for (LivingEntity entity : entities) {
                List<MobEffect> effectsToRemove = new ArrayList<>();
                for (MobEffectInstance effect : entity.getActiveEffects()) {
                    if (shouldIgnoreEffect(effect.getEffect())) continue;
                    effectsToRemove.add(effect.getEffect());
                    MobEffect positive = getEffectConversion(effect.getEffect(), level.getRandom());
                    if (positive != null) {
                        effectsToProcess.add(Pair.of(entity.position(), positive));
                    }
                }
                boolean drainDenied = drainEtchingAttribute != null && HyperVaultEvents.isHyperBoss(entity);
                if(drainDenied && !effectsToRemove.isEmpty()) {
                    WoldsVaults.LOGGER.info("Skipped Concentrated Drain etching damage on a hyperboss ({} effect(s) drained).", effectsToRemove.size());
                }
                for(MobEffect effect : effectsToRemove) {
                    entity.removeEffect(effect);
                    if(drainEtchingAttribute != null && !drainDenied) {
                        entity.hurt(DamageSource.MAGIC, Math.min(entity.getHealth() * drainEtchingAttribute.getValue(), 3 * AbilityHelper.getAbilityLevel(serverPlayer, "Concentrate_Base")));
                        serverPlayer.heal(1);
                    }
                }
            }

            for (Pair<Vec3, MobEffect> p : effectsToProcess) {
                Vec3 from = p.getLeft();
                MobEffect effect = p.getRight();
                Vector3f[] colors = getEffectColors(effect);
                spawnColoredParticleLine(level, from, serverPlayer::position, colors[0], colors[1], particleSteps, particleDelay, () -> {
                    level.playSound(null, serverPlayer.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.1F);
                    if (serverPlayer.hasEffect(effect)) {
                        MobEffectInstance currentEffectInstance = serverPlayer.getEffect(effect);

                        if (currentEffectInstance == null) {
                            return;
                        }

                        int currentAmp = currentEffectInstance.getAmplifier();

                        float currentAmplitudeScaleChance = amplitudeScaleChance / (1.0F + (0.1F * currentAmp));

                        if (currentAmplitudeScaleChance > 1.0F) {
                            for (float i = 1.0F; currentAmplitudeScaleChance >= 1.0F; i += 1.0F) {
                                currentAmp += 1;
                                currentAmplitudeScaleChance -= i;
                            }
                        }

                        if (level.getRandom().nextFloat() <= currentAmplitudeScaleChance) {
                            currentAmp += 1;
                        }

                        int incomingDuration = (int) EffectDurationHelper.adjustEffectDuration(serverPlayer, baseEffectDuration);
                        int currentDuration = currentEffectInstance.getDuration();

                        if (currentDuration < MAX_EFFECT_DURATION) {
                            double fullness = (double) currentDuration / MAX_EFFECT_DURATION;

                            double diminishingMultiplier = Math.max(0.0, 1.0 - fullness);

                            int modifiedIncoming = (int) Math.max(1, incomingDuration * diminishingMultiplier);

                            int finalDuration = Math.min(MAX_EFFECT_DURATION, currentDuration + modifiedIncoming);

                            serverPlayer.forceAddEffect(new MobEffectInstance(
                                    effect,
                                    finalDuration,
                                    currentAmp,
                                    false,
                                    true
                            ), serverPlayer);
                        }

                    } else {
                        int initialDuration = Math.min(MAX_EFFECT_DURATION, (int) EffectDurationHelper.adjustEffectDuration(serverPlayer, baseEffectDuration));
                        serverPlayer.addEffect(new MobEffectInstance(
                                effect,
                                initialDuration,
                                baseAmplitude,
                                false,
                                true
                        ));
                    }
                });
            }

            context.getSource().getMana().ifPresent(manaPlayer -> {
                manaPlayer.decreaseMana(ManaAction.PLAYER_ACTION, this.getManaCost());
            });

            this.putOnCooldown(context);
            return ActionResult.successCooldownImmediate();
        }).orElse(ActionResult.fail());

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

    private static MobEffect getEffectConversion(MobEffect effect, Random random) {
        int value = random.nextInt(0, EFFECTS.size());
        return EFFECTS.get(value);
    }

    private static Vector3f[] getEffectColors(MobEffect effect) {
        if (effect == ModEffects.QUICKENING) {
            return new Vector3f[]{
                    new Vector3f(0.2F, 0.7F, 1.0F),
                    new Vector3f(1.0F, 1.0F, 1.0F)
            };
        }
        if (effect == ModEffects.EMPOWER) {
            return new Vector3f[]{
                    new Vector3f(1.0F, 0.2F, 0.2F),
                    new Vector3f(1.0F, 0.6F, 0.1F)
            };
        }
        if (effect == iskallia.vault.init.ModEffects.PURIFYING_AURA) {
            return new Vector3f[]{
                    new Vector3f(0.4F, 1.0F, 0.7F),
                    new Vector3f(1.0F, 0.95F, 0.6F)
            };
        }
        if (effect == MobEffects.DAMAGE_RESISTANCE) {
            return new Vector3f[]{
                    new Vector3f(0.6F, 0.6F, 0.8F),
                    new Vector3f(0.3F, 0.5F, 1.0F)
            };
        }
        if (effect == MobEffects.FIRE_RESISTANCE) {
            return new Vector3f[]{
                    new Vector3f(1.0F, 0.5F, 0.0F),
                    new Vector3f(0.9F, 0.2F, 0.0F)
            };
        }
        if (effect == AMEffectRegistry.SOULSTEAL) {
            return new Vector3f[]{
                    new Vector3f(0.3F, 0.0F, 0.5F),
                    new Vector3f(0.1F, 0.9F, 0.9F)
            };
        }
        if (effect == MobEffects.LUCK) {
            return new Vector3f[]{
                    new Vector3f(0.0F, 0.9F, 0.3F),
                    new Vector3f(0.9F, 0.9F, 0.2F)
            };
        }
        if (effect == MobEffects.REGENERATION) {
            return new Vector3f[]{
                    new Vector3f(0.3F, 0.9F, 0.3F),
                    new Vector3f(1.0F, 0.84F, 0.0F)
            };
        }
        if (effect == MobEffects.SATURATION) {
            return new Vector3f[]{
                    new Vector3f(0.9F, 0.3F, 0.4F),
                    new Vector3f(1.0F, 0.85F, 0.3F)
            };
        }
        if (effect == ModEffects.GROWING) {
            return new Vector3f[]{
                    new Vector3f(0.1F, 0.6F, 0.1F),
                    new Vector3f(0.7F, 1.0F, 0.4F)
            };
        }
        if (effect == MobEffects.CONDUIT_POWER) {
            return new Vector3f[]{
                    new Vector3f(0.0F, 0.8F, 0.9F),
                    new Vector3f(0.8F, 1.0F, 1.0F)
            };
        }

        return new Vector3f[]{
                new Vector3f(1.0F, 1.0F, 1.0F),
                new Vector3f(1.0F, 0.9F, 0.4F)
        };
    }

    private static boolean shouldIgnoreEffect(MobEffect effect) {
        return effect.equals(ModEffects.REAVING) || effect.equals(ModEffects.ECHOING) || effect.equals(iskallia.vault.init.ModEffects.NOVA_DOT) || effect.equals(iskallia.vault.init.ModEffects.NO_AI) || effect.equals(iskallia.vault.init.ModEffects.GLACIAL_SHATTER);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.DOUBLE.writeBits(this.radius, buffer);
        Adapters.INT.writeBits(this.baseEffectDuration, buffer);
        Adapters.INT.writeBits(this.baseAmplitude, buffer);
        Adapters.FLOAT.writeBits(this.amplitudeScaleChance, buffer);
        Adapters.INT.writeBits(this.particleSteps, buffer);
        Adapters.INT.writeBits(this.particleDelay, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.radius = Adapters.DOUBLE.readBits(buffer).orElseThrow();
        this.baseEffectDuration = Adapters.INT.readBits(buffer).orElseThrow();
        this.baseAmplitude = Adapters.INT.readBits(buffer).orElseThrow();
        this.amplitudeScaleChance = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.particleSteps = Adapters.INT.readBits(buffer).orElseThrow();
        this.particleDelay = Adapters.INT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.DOUBLE.writeNbt(this.radius).ifPresent(tag -> nbt.put("radius",tag));
            Adapters.INT.writeNbt(this.baseEffectDuration).ifPresent(tag -> nbt.put("baseEffectDuration",tag));
            Adapters.INT.writeNbt(this.baseAmplitude).ifPresent(tag -> nbt.put("baseAmplitude",tag));
            Adapters.FLOAT.writeNbt(this.amplitudeScaleChance).ifPresent(tag -> nbt.put("amplitudeScaleChance",tag));
            Adapters.INT.writeNbt(this.particleSteps).ifPresent(tag -> nbt.put("particleSteps",tag));
            Adapters.INT.writeNbt(this.particleDelay).ifPresent(tag -> nbt.put("particleDelay",tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.radius = Adapters.DOUBLE.readNbt(nbt.get("radius")).orElse(0.0);
        this.baseEffectDuration = Adapters.INT.readNbt(nbt.get("baseEffectDuration")).orElse(0);
        this.baseAmplitude = Adapters.INT.readNbt(nbt.get("baseAmplitude")).orElse(0);
        this.amplitudeScaleChance = Adapters.FLOAT.readNbt(nbt.get("amplitudeScaleChance")).orElse(0F);
        this.particleSteps = Adapters.INT.readNbt(nbt.get("particleSteps")).orElse(0);
        this.particleDelay = Adapters.INT.readNbt(nbt.get("particleDelay")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.DOUBLE.writeJson(this.radius).ifPresent(jsonElement -> json.add("radius", jsonElement));
            Adapters.INT.writeJson(this.baseEffectDuration).ifPresent(jsonElement -> json.add("baseEffectDuration", jsonElement));
            Adapters.INT.writeJson(this.baseAmplitude).ifPresent(jsonElement -> json.add("baseAmplitude", jsonElement));
            Adapters.FLOAT.writeJson(this.amplitudeScaleChance).ifPresent(jsonElement -> json.add("amplitudeScaleChance", jsonElement));
            Adapters.INT.writeJson(this.particleSteps).ifPresent(jsonElement -> json.add("particleSteps", jsonElement));
            Adapters.INT.writeJson(this.particleDelay).ifPresent(jsonElement -> json.add("particleDelay", jsonElement));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.radius = Adapters.DOUBLE.readJson(json.get("radius")).orElse(0.0);
        this.baseEffectDuration = Adapters.INT.readJson(json.get("baseEffectDuration")).orElse(0);
        this.baseAmplitude = Adapters.INT.readJson(json.get("baseAmplitude")).orElse(0);
        this.amplitudeScaleChance = Adapters.FLOAT.readJson(json.get("amplitudeScaleChance")).orElse(0F);
        this.particleSteps = Adapters.INT.readJson(json.get("particleSteps")).orElse(0);
        this.particleDelay = Adapters.INT.readJson(json.get("particleDelay")).orElse(0);
    }
    public double getRadius() {
        return radius;
    }

    public int getBaseEffectDuration() {
        return baseEffectDuration;
    }

    public int getBaseAmplitude() {
        return baseAmplitude;
    }

    public float getAmplitudeScaleChance() {
        return amplitudeScaleChance;
    }

    public int getParticleSteps() {
        return particleSteps;
    }

    public int getParticleDelay() {
        return particleDelay;
    }

}
