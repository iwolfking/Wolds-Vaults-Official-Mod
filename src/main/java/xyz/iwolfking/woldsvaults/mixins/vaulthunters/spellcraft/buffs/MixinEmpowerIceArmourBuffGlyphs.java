package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.EmpowerIceArmourAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Ice Armor: the Chilled amplifier and duration it applies take Intensify; the per-hit cost takes drain. */
@Mixin(value = EmpowerIceArmourAbility.class, remap = false)
public class MixinEmpowerIceArmourBuffGlyphs {
    @ModifyReturnValue(method = "getChilledAmplifier", at = @At("RETURN"))
    private int woldsvaults$scaleAmplifier(int amplifier) {
        return BuffGlyphs.scale(amplifier, BuffGlyphs.intensity((EmpowerIceArmourAbility) (Object) this));
    }

    @ModifyReturnValue(method = "getChilledDuration", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.intensity((EmpowerIceArmourAbility) (Object) this));
    }

    @ModifyReturnValue(method = "getAdditionalManaPerHit", at = @At("RETURN"))
    private float woldsvaults$scaleHitCost(float cost) {
        return cost * BuffGlyphs.drain((EmpowerIceArmourAbility) (Object) this);
    }
}
