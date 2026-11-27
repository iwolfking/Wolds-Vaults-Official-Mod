package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.StonefallAbility;
import iskallia.vault.skill.ability.effect.StonefallSnowAbility;
import iskallia.vault.skill.ability.effect.spi.AbstractStonefallAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/**
 * Stonefall: the landing window takes Extend; Intensify scales Snow's damage reduction and the base spec's knockback
 * (VH clamps the reduction at 1).
 */
@Mixin(value = AbstractStonefallAbility.class, remap = false)
public class MixinAbstractStonefallBuffGlyphs {
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleWindow(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((AbstractStonefallAbility) (Object) this));
    }

    @ModifyReturnValue(method = "getDamageReduction", at = @At("RETURN"))
    private float woldsvaults$scaleReduction(float reduction) {
        Object self = this;
        return self instanceof StonefallSnowAbility snow ? reduction * BuffGlyphs.intensity(snow) : reduction;
    }

    @ModifyReturnValue(method = "getKnockbackMultiplier", at = @At("RETURN"))
    private float woldsvaults$scaleKnockback(float knockback) {
        Object self = this;
        return self instanceof StonefallAbility base ? knockback * BuffGlyphs.intensity(base) : knockback;
    }
}
