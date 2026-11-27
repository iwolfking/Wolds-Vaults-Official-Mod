package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.ShellAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/** Shell: the stun chance and stun duration on a hit taken take Intensify; the per-hit cost takes drain. */
@Mixin(value = ShellAbility.class, remap = false)
public class MixinShellBuffGlyphs {
    @WrapOperation(method = "lambda$onDamage$5", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/ShellAbility;stunChance:F"))
    private static float woldsvaults$scaleStunChance(ShellAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.intensity(ability);
    }

    @WrapOperation(method = "lambda$onDamage$5", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/ShellAbility;stunDurationTicks:I"))
    private static int woldsvaults$scaleStunDuration(ShellAbility ability, Operation<Integer> original) {
        return BuffGlyphs.scale(original.call(ability), BuffGlyphs.intensity(ability));
    }

    @WrapOperation(method = "lambda$onDamage$5", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/ShellAbility;additionalManaPerHit:F"))
    private static float woldsvaults$scaleHitCost(ShellAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.drain(ability);
    }
}
