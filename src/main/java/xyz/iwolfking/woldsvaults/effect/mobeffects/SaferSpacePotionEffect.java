package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SaferSpacePotionEffect extends MobEffect {
    public SaferSpacePotionEffect(MobEffectCategory mobEffectCategory, int color, ResourceLocation id) {
        super(mobEffectCategory, color);
        this.setRegistryName(id);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {}
    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) { return false; }
}
