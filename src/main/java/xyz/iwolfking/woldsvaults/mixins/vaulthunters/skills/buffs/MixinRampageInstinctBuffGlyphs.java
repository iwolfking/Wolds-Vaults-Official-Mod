package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.RampageInstinctAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Rampage: Instinct's lucky hit chance, stored as the effect amplifier when it switches on, takes Intensify. */
@Mixin(value = RampageInstinctAbility.class, remap = false)
public class MixinRampageInstinctBuffGlyphs {
    @WrapOperation(method = "lambda$doToggle$2", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/RampageInstinctAbility;luckyHitChance:F"))
    private float woldsvaults$scaleLuckyHitChance(RampageInstinctAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.intensity(ability);
    }
}
