package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.RampageBloodlustAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Rampage: Bloodlust's mana refund takes Intensify; its stack duration takes Extend's duration upside. */
@Mixin(value = RampageBloodlustAbility.class, remap = false)
public class MixinRampageBloodlustBuffGlyphs {
    @ModifyReturnValue(method = "getManaRefund", at = @At("RETURN"))
    private float woldsvaults$scaleRefund(float refund) {
        return refund * BuffGlyphs.intensity((RampageBloodlustAbility) (Object) this);
    }

    @ModifyReturnValue(method = "getStackDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleStackDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.live((RampageBloodlustAbility) (Object) this).durationUpside());
    }
}
