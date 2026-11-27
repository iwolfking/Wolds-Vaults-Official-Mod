package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.AbstractTotemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Totems: the totem's life, stored in the tile at the cast (and the cooldown delay), takes Extend. */
@Mixin(value = AbstractTotemAbility.class, remap = false)
public class MixinAbstractTotemBuffGlyphs {
    @ModifyReturnValue(method = "getTotemDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((AbstractTotemAbility<?>) (Object) this));
    }
}
