package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.ManaShieldAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Mana Shield: the shield's duration takes Extend and its health (also its charge count) Intensify. */
@Mixin(value = ManaShieldAbility.class, remap = false)
public class MixinManaShieldBuffGlyphs {
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((ManaShieldAbility) (Object) this));
    }

    @ModifyReturnValue(method = "getHealthPoints", at = @At("RETURN"))
    private int woldsvaults$scaleHealth(int health) {
        return BuffGlyphs.scale(health, BuffGlyphs.castIntensity((ManaShieldAbility) (Object) this));
    }
}
