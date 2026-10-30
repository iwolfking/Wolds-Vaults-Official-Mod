package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.ability.effect.ImplodeAbility;
import iskallia.vault.skill.ability.effect.LifeTapAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;

@Mixin(value = LifeTapAbility.class, remap = false)
public abstract class MixinLifeTapAbility {

    @Shadow
    protected abstract float getDamageModifier(float radius, float dist);

    @Redirect(method = "lambda$doAction$3", at = @At(value = "INVOKE", target = "Liskallia/vault/skill/ability/effect/LifeTapAbility;getDamageModifier(FF)F"))
    private float callAdjustedDamageModifier(LifeTapAbility instance, float radius, float dist, @Local(argsOnly = true) ServerPlayer player) {
        return AbilityHelper.getScaledByLevelDamageFalloff(this.getDamageModifier(radius, dist), player, "Implode_Life_Tap");
    }

}
