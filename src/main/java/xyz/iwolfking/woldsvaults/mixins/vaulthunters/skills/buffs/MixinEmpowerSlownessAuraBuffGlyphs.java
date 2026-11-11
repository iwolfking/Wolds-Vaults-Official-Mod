package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.EmpowerSlownessAuraAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Slowness Aura: the Slowness amplifier it applies takes Intensify. */
@Mixin(value = EmpowerSlownessAuraAbility.class, remap = false)
public class MixinEmpowerSlownessAuraBuffGlyphs {
    @ModifyReturnValue(method = "getSlownessAmplifier", at = @At("RETURN"))
    private int woldsvaults$scaleAmplifier(int amplifier) {
        return BuffGlyphs.scale(amplifier, BuffGlyphs.intensity((EmpowerSlownessAuraAbility) (Object) this));
    }
}
