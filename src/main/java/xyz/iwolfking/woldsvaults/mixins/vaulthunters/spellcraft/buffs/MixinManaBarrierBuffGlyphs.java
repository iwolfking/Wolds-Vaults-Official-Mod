package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.ManaBarrierAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Mana Barrier: how long the barrier stands (and the cooldown delay) takes Extend. */
@Mixin(value = ManaBarrierAbility.class, remap = false)
public class MixinManaBarrierBuffGlyphs {
    @WrapOperation(method = "lambda$doAction$6", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/ManaBarrierAbility;durationTicks:I"))
    private int woldsvaults$scaleDuration(ManaBarrierAbility ability, Operation<Integer> original) {
        return BuffGlyphs.scale(original.call(ability), BuffGlyphs.castDuration(ability));
    }
}
