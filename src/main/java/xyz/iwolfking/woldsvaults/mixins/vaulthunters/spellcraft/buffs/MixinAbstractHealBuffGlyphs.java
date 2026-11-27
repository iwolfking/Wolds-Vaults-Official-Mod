package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.AbstractHealAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Heal: the flat life healed takes Intensify (its cost is the spell's heal mana factor, billed with the slot). */
@Mixin(value = AbstractHealAbility.class, remap = false)
public class MixinAbstractHealBuffGlyphs {
    @ModifyReturnValue(method = "getFlatLifeHealed", at = @At("RETURN"))
    private float woldsvaults$scaleHeal(float healed) {
        return healed * BuffGlyphs.castIntensity((AbstractHealAbility) (Object) this);
    }
}
