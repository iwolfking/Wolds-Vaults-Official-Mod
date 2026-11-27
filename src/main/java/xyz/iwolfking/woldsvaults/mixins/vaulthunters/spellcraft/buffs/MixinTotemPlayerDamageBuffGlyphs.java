package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.TotemPlayerDamageAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Totem of Wrath: the damage bonus stored in the tile at the cast takes Intensify. */
@Mixin(value = TotemPlayerDamageAbility.class, remap = false)
public class MixinTotemPlayerDamageBuffGlyphs {
    @ModifyReturnValue(method = "getTotemPlayerDamagePercent", at = @At("RETURN"))
    private float woldsvaults$scaleBonus(float bonus) {
        return bonus * BuffGlyphs.castIntensity((TotemPlayerDamageAbility) (Object) this);
    }
}
