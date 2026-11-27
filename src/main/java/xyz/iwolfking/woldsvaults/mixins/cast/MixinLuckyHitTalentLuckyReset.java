package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.Cooldown;
import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.api.cast.LuckyReset;

/**
 * Lucky Reset removes {@link LuckyReset#REMAINING_SHARE} of the remaining cooldown (at least one tick) instead of 25%
 * of the full one, and each ability cast can trigger it at most once.
 */
@Mixin(value = LuckyHitTalent.class, remap = false)
public class MixinLuckyHitTalentLuckyReset {
    @WrapOperation(method = "lambda$doLuckyHit$7", at = @At(value = "INVOKE",
            target = "Liskallia/vault/skill/ability/effect/spi/core/Ability;reduceCooldownBy(I)V"))
    private static void woldsvaults$luckyResetPerCast(Ability ability, int reduceBy, Operation<Void> original,
                                                       @Local(argsOnly = true) Cooldown cooldown) {
        CastTag tag = CastContext.current();
        if (tag != null && !tag.claimLuckyReset()) {
            return;
        }
        int remaining = cooldown.getRemainingTicks() + cooldown.getRemainingDelayTicks();
        if (remaining <= 0) {
            return;
        }
        original.call(ability, Math.max(1, Math.round(remaining * LuckyReset.REMAINING_SHARE)));
    }
}
