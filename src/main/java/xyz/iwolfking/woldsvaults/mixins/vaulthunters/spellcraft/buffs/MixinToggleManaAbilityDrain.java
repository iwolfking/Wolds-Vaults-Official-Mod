package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.core.ToggleManaAbility;
import iskallia.vault.skill.base.Skill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** A toggle's drain per second (and so the mana needed to switch it on) takes its activation's drain factor. */
@Mixin(value = ToggleManaAbility.class, remap = false)
public class MixinToggleManaAbilityDrain {

    @ModifyReturnValue(method = "getManaCostPerSecond", at = @At("RETURN"))
    private float woldsvaults$scaleDrain(float drain) {
        return drain * BuffGlyphs.drain((Skill) (Object) this);
    }
}
