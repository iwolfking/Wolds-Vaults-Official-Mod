package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class GroundedPotionEffect extends MobEffect {
    public GroundedPotionEffect(MobEffectCategory mobEffectCategory, int i, ResourceLocation id) {
        super(mobEffectCategory, i);
        this.setRegistryName(id);
    }
}
