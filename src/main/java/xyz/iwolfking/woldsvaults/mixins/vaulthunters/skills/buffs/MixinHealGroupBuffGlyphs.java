package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.HealGroupAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Group Heal: the flat life healed (the caster, nearby members, half for distant members) takes Intensify. */
@Mixin(value = HealGroupAbility.class, remap = false)
public class MixinHealGroupBuffGlyphs {
    @WrapOperation(method = "lambda$doAction$1", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/HealGroupAbility;flatLifeHealed:F"))
    private float woldsvaults$scaleHeal(HealGroupAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.castIntensity(ability);
    }
}
