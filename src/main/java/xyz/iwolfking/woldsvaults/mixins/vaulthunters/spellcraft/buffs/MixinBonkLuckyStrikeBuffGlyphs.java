package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.BonkLuckyStrikeAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Battle Cry Lucky Strike: the lucky hit chance each stack adds takes Intensify while the stacks last. */
@Mixin(value = BonkLuckyStrikeAbility.class, remap = false)
public class MixinBonkLuckyStrikeBuffGlyphs {
    @ModifyReturnValue(method = "getLuckyHitChancePerStack", at = @At("RETURN"))
    private float woldsvaults$scalePerStack(float value) {
        return value * BuffGlyphs.intensity((BonkLuckyStrikeAbility) (Object) this);
    }
}
