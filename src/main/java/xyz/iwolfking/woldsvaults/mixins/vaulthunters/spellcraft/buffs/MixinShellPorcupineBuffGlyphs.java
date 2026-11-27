package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.ShellPorcupineAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/**
 * Porcupine and Quill: the extra thorns share takes Intensify (Quill's count does not); the per-hit cost takes drain.
 */
@Mixin(value = ShellPorcupineAbility.class, remap = false)
public class MixinShellPorcupineBuffGlyphs {
    @ModifyReturnValue(method = "getAdditionalThornsDamagePercent", at = @At("RETURN"))
    private float woldsvaults$scaleThorns(float thorns) {
        return thorns * BuffGlyphs.intensity((ShellPorcupineAbility) (Object) this);
    }

    @ModifyReturnValue(method = "getAdditionalManaPerHit", at = @At("RETURN"))
    private float woldsvaults$scaleHitCost(float cost) {
        return cost * BuffGlyphs.drain((ShellPorcupineAbility) (Object) this);
    }
}
