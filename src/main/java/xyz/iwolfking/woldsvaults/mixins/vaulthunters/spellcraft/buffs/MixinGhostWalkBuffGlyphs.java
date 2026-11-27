package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.GhostWalkAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Ghost Walk: the effect duration takes Extend. */
@Mixin(value = GhostWalkAbility.class, remap = false)
public class MixinGhostWalkBuffGlyphs {
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((GhostWalkAbility) (Object) this));
    }
}
