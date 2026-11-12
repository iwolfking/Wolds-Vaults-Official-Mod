package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.RampageBerserkerAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Rampage: Berserker's uncapped drain ramp takes its activation's drain factor, like its base drain. */
@Mixin(value = RampageBerserkerAbility.class, remap = false)
public class MixinRampageBerserkerBuffGlyphs {
    @WrapOperation(method = "getCurrentManaCostPerSecond", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/RampageBerserkerAbility;manaRampPerSecond:F"))
    private float woldsvaults$scaleRamp(RampageBerserkerAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.drain(ability);
    }
}
