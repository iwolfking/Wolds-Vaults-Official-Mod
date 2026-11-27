package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.TotemManaRegenAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Mana totem: the mana regeneration stored in the tile at the cast takes Intensify. */
@Mixin(value = TotemManaRegenAbility.class, remap = false)
public class MixinTotemManaRegenBuffGlyphs {
    @ModifyReturnValue(method = "getTotemManaRegenPercent", at = @At("RETURN"))
    private float woldsvaults$scaleRegen(float regen) {
        return regen * BuffGlyphs.castIntensity((TotemManaRegenAbility) (Object) this);
    }
}
