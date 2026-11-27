package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.RampageLeechAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Rampage: Leech's leech share takes Intensify; VH's amplifier clamp at 100 stays. */
@Mixin(value = RampageLeechAbility.class, remap = false)
public class MixinRampageLeechBuffGlyphs {
    @ModifyReturnValue(method = "getLeechPercent", at = @At("RETURN"))
    private float woldsvaults$scaleLeech(float leech) {
        return leech * BuffGlyphs.intensity((RampageLeechAbility) (Object) this);
    }
}
