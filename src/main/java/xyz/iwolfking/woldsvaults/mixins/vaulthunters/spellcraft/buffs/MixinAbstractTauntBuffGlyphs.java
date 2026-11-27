package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.AbstractTauntAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Taunts and Decoy: the duration set at the cast takes Extend (and Intensify's duration cost). */
@Mixin(value = AbstractTauntAbility.class, remap = false)
public class MixinAbstractTauntBuffGlyphs {
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((AbstractTauntAbility) (Object) this));
    }
}
