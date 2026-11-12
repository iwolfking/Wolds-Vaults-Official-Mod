package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.StonefallColdAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Stonefall Cold: the Chilled duration its landing applies takes Intensify (the amplifier does not). */
@Mixin(value = StonefallColdAbility.class, remap = false)
public class MixinStonefallColdBuffGlyphs {
    @ModifyReturnValue(method = "getFreezeTicks", at = @At("RETURN"))
    private int woldsvaults$scaleFreeze(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.intensity((StonefallColdAbility) (Object) this));
    }
}
