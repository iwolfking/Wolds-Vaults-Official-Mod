package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.TauntCharmAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Taunt Charm: the charmed mobs' share of the player's attack damage, stored on them at the cast, takes Intensify. */
@Mixin(value = TauntCharmAbility.class, remap = false)
public class MixinTauntCharmBuffGlyphs {
    @ModifyReturnValue(method = "getPercentPlayerDamage", at = @At("RETURN"))
    private float woldsvaults$scaleShare(float share) {
        return share * BuffGlyphs.castIntensity((TauntCharmAbility) (Object) this);
    }
}
