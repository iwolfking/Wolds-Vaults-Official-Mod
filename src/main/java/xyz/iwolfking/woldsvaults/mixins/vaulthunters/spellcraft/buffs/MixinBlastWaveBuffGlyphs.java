package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.BlastWaveAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Smite Blast Wave: the wave's ability power range takes Intensify. */
@Mixin(value = BlastWaveAbility.class, remap = false)
public class MixinBlastWaveBuffGlyphs {
    @ModifyReturnValue(method = "getAbilityPowerPercentMin", at = @At("RETURN"))
    private float woldsvaults$scaleMin(float value) {
        return value * BuffGlyphs.intensity((BlastWaveAbility) (Object) this);
    }

    @ModifyReturnValue(method = "getAbilityPowerPercentMax", at = @At("RETURN"))
    private float woldsvaults$scaleMax(float value) {
        return value * BuffGlyphs.intensity((BlastWaveAbility) (Object) this);
    }
}
