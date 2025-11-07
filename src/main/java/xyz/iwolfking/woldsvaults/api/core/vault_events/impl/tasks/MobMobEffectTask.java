package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import xyz.iwolfking.woldsvaults.api.util.ref.Effect;

import java.util.List;
import java.util.Objects;

public class MobMobEffectTask implements VaultEventTask {

    private final WeightedList<Effect> effects;

    private final boolean shouldGrantAllEffects;

    private final int randomEffectCount;

    private final double effectRadius;

    public MobMobEffectTask(WeightedList<Effect> effects, boolean shouldGrantAllEffects, int randomEffectCount, double effectRadius) {
        this.effects = effects;
        this.shouldGrantAllEffects = shouldGrantAllEffects;
        this.randomEffectCount = randomEffectCount;
        this.effectRadius = effectRadius;
    }

    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        List<LivingEntity> nearbyEntities = player.level.getEntitiesOfClass(LivingEntity.class, Objects.requireNonNull(player).getBoundingBox().inflate(effectRadius));

        nearbyEntities.forEach(livingEntity -> {
            if(!(livingEntity instanceof Player) && shouldGrantAllEffects) {
                effects.forEach((mobEffectInstance, aDouble) -> mobEffectInstance.apply(livingEntity));
            }
            else {
                for(int i = 0; i < randomEffectCount; i++) {
                    if(!(livingEntity instanceof Player)) {
                        effects.getRandom().ifPresent(effect -> {
                            effect.apply(livingEntity);
                        });
                    }

                }
            }

        });
    }

    public static class Builder {
        private final WeightedList<Effect> effects = new WeightedList<>();

        private boolean shouldGrantAllEffects = false;

        private int randomEffectCount = 1;

        private double effectRadius = 32.0;

        public Builder effect(MobEffect effect, int amplifier, int duration, double weight) {
            this.effects.add(new Effect(effect, amplifier, duration), weight);
            return this;
        }

        public Builder effect(MobEffect effect, int amplifier, int duration) {
            this.effects.add(new Effect(effect, amplifier, duration), 1.0);
            return this;
        }

        public Builder grantAll() {
            this.shouldGrantAllEffects = true;
            return this;
        }

        public Builder amount(int count) {
            this.randomEffectCount = count;
            return this;
        }

        public Builder radius(double radius) {
            this.effectRadius = radius;
            return this;
        }

        public MobMobEffectTask build() {
            return new MobMobEffectTask(effects, shouldGrantAllEffects, randomEffectCount, effectRadius);
        }
    }
}
