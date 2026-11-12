package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.AbstractBonkAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Battle Cry: the stack duration set at the cast takes Extend (and Intensify's duration cost). */
@Mixin(value = AbstractBonkAbility.class, remap = false)
public class MixinAbstractBonkBuffGlyphs {
    @ModifyReturnValue(method = "getStackDuration", at = @At("RETURN"))
    private int woldsvaults$scaleStackDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((AbstractBonkAbility) (Object) this));
    }
}
