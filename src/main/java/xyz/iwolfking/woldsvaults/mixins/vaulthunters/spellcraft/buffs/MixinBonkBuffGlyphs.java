package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.BonkAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Battle Cry: the attack damage each stack adds takes Intensify while the stacks last. */
@Mixin(value = BonkAbility.class, remap = false)
public class MixinBonkBuffGlyphs {
    @ModifyReturnValue(method = "getAttackDamagePerStack", at = @At("RETURN"))
    private float woldsvaults$scalePerStack(float value) {
        return value * BuffGlyphs.intensity((BonkAbility) (Object) this);
    }
}
