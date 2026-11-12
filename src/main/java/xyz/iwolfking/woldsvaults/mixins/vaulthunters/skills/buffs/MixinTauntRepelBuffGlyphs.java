package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.TauntRepelAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Taunt Repel: the repel force takes Intensify. */
@Mixin(value = TauntRepelAbility.class, remap = false)
public class MixinTauntRepelBuffGlyphs {
    @WrapOperation(method = "lambda$doAction$1", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/TauntRepelAbility;repelForce:F"))
    private float woldsvaults$scaleForce(TauntRepelAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.castIntensity(ability);
    }
}
