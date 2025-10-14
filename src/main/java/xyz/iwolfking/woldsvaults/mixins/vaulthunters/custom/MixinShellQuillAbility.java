package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.ability.effect.ShellQuillAbility;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ShellQuillAbility.class, remap = false)
public class MixinShellQuillAbility {

    @Redirect(method = "doQuill", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), remap = true)
    private static boolean adjustQuillDamage(Mob instance, DamageSource damageSource, float v, @Local(argsOnly = true) float reflectedDamage) {
        return instance.hurt(damageSource, reflectedDamage * 0.5F);
    }
}
