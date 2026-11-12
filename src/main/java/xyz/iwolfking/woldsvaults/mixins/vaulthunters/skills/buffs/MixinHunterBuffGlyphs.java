package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.spi.HunterAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Hunter: the scan duration (and so the scan count) takes Extend. */
@Mixin(value = HunterAbility.class, remap = false)
public class MixinHunterBuffGlyphs {
    @ModifyReturnValue(method = "getDurationTicks", at = @At("RETURN"))
    private int woldsvaults$scaleDuration(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((HunterAbility) (Object) this));
    }
}
