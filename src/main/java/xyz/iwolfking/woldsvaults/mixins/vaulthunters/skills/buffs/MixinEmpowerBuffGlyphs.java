package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.EmpowerAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Empower: the speed it grants (to the caster when it switches on, to allies every tick) takes Intensify. */
@Mixin(value = EmpowerAbility.class, remap = false)
public class MixinEmpowerBuffGlyphs {
    @ModifyReturnValue(method = "getSpeedPercentAdded", at = @At("RETURN"))
    private float woldsvaults$scaleSpeed(float speed) {
        return speed * BuffGlyphs.intensity((EmpowerAbility) (Object) this);
    }
}
