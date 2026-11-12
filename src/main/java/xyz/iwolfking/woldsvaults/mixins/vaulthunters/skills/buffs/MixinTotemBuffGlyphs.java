package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.TotemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.BuffGlyphs;

/** Totem: the healing per second stored in the tile at the cast takes Intensify. */
@Mixin(value = TotemAbility.class, remap = false)
public class MixinTotemBuffGlyphs {
    @WrapOperation(method = "initializeTotem", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/TotemAbility;totemHealthPerSecond:F"))
    private float woldsvaults$scaleHealing(TotemAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.castIntensity(ability);
    }
}
