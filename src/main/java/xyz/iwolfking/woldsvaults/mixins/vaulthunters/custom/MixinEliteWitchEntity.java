package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.elite.EliteWitchEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EliteWitchEntity.class, remap = false)
public class MixinEliteWitchEntity {
    @Shadow
    private void throwPotionAtTarget(LivingEntity target, Item potionItem, Potion basePotion, List<MobEffectInstance> customEffects, float velocity, float inaccuracy, float pitchAdjust) {

    }

    @Inject(method = "performCruelBrew", at = @At("HEAD"), cancellable = true)
    private void fixCruelBrewScaling(LivingEntity target, CallbackInfo ci) {
        float maxHealth = target.getMaxHealth();

        int amplifier = Math.max(0, (int)(Math.log((maxHealth / 20.0F) + 1.0D) * 1.5D));

        List<MobEffectInstance> effects = List.of(new MobEffectInstance(MobEffects.HARM, 1, amplifier));

        this.throwPotionAtTarget(target, net.minecraft.world.item.Items.SPLASH_POTION, null, effects, 0.8F, 7.5F, -15.0F);
        ci.cancel();
    }
}
