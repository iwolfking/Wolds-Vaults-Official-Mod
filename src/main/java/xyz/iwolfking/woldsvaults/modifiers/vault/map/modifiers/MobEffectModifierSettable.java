package xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.EntityEffectModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;

import java.util.Map;
import java.util.Random;

public class MobEffectModifierSettable extends SettableValueVaultModifier<MobEffectModifierSettable.Properties> {
    public MobEffectModifierSettable(ResourceLocation id, Properties properties, Display display) {
        super(id, properties, display);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        CommonEvents.ENTITY_SPAWN.register(context.getUUID(), EventPriority.HIGHEST, event -> {
            if (event.getEntity().level == world) {
                if (event.getEntity() instanceof LivingEntity entity) {
                    if (!IModifierImmunity.of(entity).test(this)) {
                        Map<MobEffectInstance, Double> effects = ((EntityEffectModifier.ILivingEntityAccessor)entity).getEffects();
                        if (this.properties.filter.test(entity)) {
                            for (Map.Entry<MobEffectInstance, Double> entry : effects.entrySet()) {
                                if (entry.getKey().getEffect() == this.properties.effect) {
                                    effects.put(entry.getKey(), entry.getValue() + this.properties.getChance());
                                    return;
                                }
                            }

                            effects.put(new MobEffectInstance(this.properties.effect, 999999, this.properties.getAmplifier()), this.properties.getChance());
                        }
                    }
                }
            }
        });
        CommonEvents.ENTITY_SPAWN.register(context.getUUID(), EventPriority.LOWEST, event -> {
            if (event.getEntity().level == world) {
                if (event.getEntity() instanceof LivingEntity entity) {
                    if (!IModifierImmunity.of(entity).test(this)) {
                        Map<MobEffectInstance, Double> effects = ((MobEffectModifierSettable.ILivingEntityAccessor)entity).getEffects();
                        Random random = entity.level.getRandom();
                        effects.forEach((instance, chance) -> {
                            if (random.nextDouble() < chance) {
                                entity.addEffect(instance);
                            }
                        });
                    }
                }
            }
        });
    }

    public static class Properties extends SettableValueVaultModifier.Properties {
        @Expose
        private final EntityPredicate filter;
        @Expose
        private final MobEffect effect;
        @Expose
        private final int amplifier;

        public Properties(EntityPredicate filter, MobEffect effect, int amplifier) {
            this.filter = filter;
            this.effect = effect;
            this.amplifier = amplifier;
        }

        public EntityPredicate getFilter() {
            return this.filter;
        }

        public MobEffect getEffect() {
            return this.effect;
        }

        public int getAmplifier() {
            return this.amplifier;
        }

        public double getChance() {
            return this.getValue();
        }
    }

    public interface ILivingEntityAccessor {
        Map<MobEffectInstance, Double> getEffects();
    }
}
