package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class LevitateIIPotionEffect extends MobEffect {
    public LevitateIIPotionEffect(MobEffectCategory mobEffectCategory, int i, ResourceLocation id) {
        super(mobEffectCategory, i);
        this.setRegistryName(id);
    }
}
