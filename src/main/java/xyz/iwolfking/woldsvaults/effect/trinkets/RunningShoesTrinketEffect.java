package xyz.iwolfking.woldsvaults.effect.trinkets;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.custom.effect.EffectGearAttribute;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.effects.PotionEffectTrinket;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class RunningShoesTrinketEffect extends TrinketEffect<PotionEffectTrinket.Config> implements GearAttributeTrinket {
    private final MobEffect effect;
    private final int addedAmplifier;

    public RunningShoesTrinketEffect(ResourceLocation name, MobEffect effect, int addedAmplifier) {
        super(name);
        this.effect = effect;
        this.addedAmplifier = addedAmplifier;
    }

    public Class<PotionEffectTrinket.Config> getConfigClass() {
        return PotionEffectTrinket.Config.class;
    }

    public PotionEffectTrinket.Config getDefaultConfig() {
        return new PotionEffectTrinket.Config(this.effect.getRegistryName(), this.addedAmplifier);
    }

    public List<VaultGearAttributeInstance<?>> getAttributes() {
        iskallia.vault.gear.trinket.effects.PotionEffectTrinket.Config cfg = this.getConfig();
        return Lists.newArrayList(new VaultGearAttributeInstance<>(ModGearAttributes.EFFECT, new EffectGearAttribute(cfg.getEffect(), cfg.getAddedAmplifier())), new VaultGearAttributeInstance<>(ModGearAttributes.MOVEMENT_SPEED, 2.0F));
    }

    public static class Config extends TrinketEffect.Config {
        @Expose
        private ResourceLocation effect;
        @Expose
        private int addedAmplifier;

        public Config(ResourceLocation effect, int addedAmplifier) {
            this.effect = effect;
            this.addedAmplifier = addedAmplifier;
        }

        public MobEffect getEffect() {
            return ForgeRegistries.MOB_EFFECTS.getValue(this.effect);
        }

        public int getAddedAmplifier() {
            return this.addedAmplifier;
        }
    }
}
