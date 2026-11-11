package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.RampageChainAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Rampage: Chain's extra chain count takes Intensify. */
@Mixin(value = RampageChainAbility.class, remap = false)
public class MixinRampageChainBuffGlyphs {
    @ModifyReturnValue(method = "getAdditionalChainCount", at = @At("RETURN"))
    private int woldsvaults$scaleChains(int count) {
        return BuffGlyphs.scale(count, BuffGlyphs.intensity((RampageChainAbility) (Object) this));
    }
}
