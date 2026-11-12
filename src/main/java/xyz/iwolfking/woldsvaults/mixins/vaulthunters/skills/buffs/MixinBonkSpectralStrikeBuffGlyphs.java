package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.BonkSpectralStrikeAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Battle Cry Spectral Strike: the ability power each stack adds takes Intensify while the stacks last. */
@Mixin(value = BonkSpectralStrikeAbility.class, remap = false)
public class MixinBonkSpectralStrikeBuffGlyphs {
    @ModifyReturnValue(method = "getAbilityPowerPerStack", at = @At("RETURN"))
    private float woldsvaults$scalePerStack(float value) {
        return value * BuffGlyphs.intensity((BonkSpectralStrikeAbility) (Object) this);
    }
}
