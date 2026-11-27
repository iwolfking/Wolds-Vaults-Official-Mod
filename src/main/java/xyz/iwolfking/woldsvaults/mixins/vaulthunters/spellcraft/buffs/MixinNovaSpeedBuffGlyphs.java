package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.NovaSpeedAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Frost Nova: the Chilled amplifier takes Intensify and its duration Extend. */
@Mixin(value = NovaSpeedAbility.class, remap = false)
public class MixinNovaSpeedBuffGlyphs {
    @ModifyReturnValue(method = "getAmplifier", at = @At("RETURN"))
    private int woldsvaults$scaleAmplifier(int amplifier) {
        return BuffGlyphs.scale(amplifier, BuffGlyphs.castIntensity((NovaSpeedAbility) (Object) this));
    }

    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((NovaSpeedAbility) (Object) this));
    }
}
