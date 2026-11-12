package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.skill.ability.effect.SummonEternalAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Summon Eternal: the eternal's despawn time takes Extend. */
@Mixin(value = SummonEternalAbility.class, remap = false)
public class MixinSummonEternalBuffGlyphs {
    @ModifyReturnValue(method = "getDespawnTime", at = @At("RETURN"))
    private int woldsvaults$scaleDespawn(int ticks) {
        return BuffGlyphs.scale(ticks, BuffGlyphs.castDuration((SummonEternalAbility) (Object) this));
    }
}
