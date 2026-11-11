package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.TauntAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Taunt: the Vulnerable amplifier it applies takes Intensify. */
@Mixin(value = TauntAbility.class, remap = false)
public class MixinTauntBuffGlyphs {
    @ModifyReturnValue(method = "getAmplifier", at = @At("RETURN"))
    private int woldsvaults$scaleAmplifier(int amplifier) {
        return BuffGlyphs.scale(amplifier, BuffGlyphs.castIntensity((TauntAbility) (Object) this));
    }
}
