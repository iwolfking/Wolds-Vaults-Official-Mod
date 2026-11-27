package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.skill.ability.effect.spi.AbstractSmiteAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;

/**
 * Smite and Archon: the bolt's ability power range takes Intensify (Lightning Damage talents add unscaled); the
 * per-bolt cost takes drain.
 */
@Mixin(value = AbstractSmiteAbility.class, remap = false)
public class MixinAbstractSmiteBuffGlyphs {
    @WrapOperation(method = "getRandomAbilityPowerDamage", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/spi/AbstractSmiteAbility;percentAbilityPowerDealtMin:F"))
    private float woldsvaults$scaleMin(AbstractSmiteAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.intensity(ability);
    }

    @WrapOperation(method = "getRandomAbilityPowerDamage", at = @At(value = "FIELD",
            target = "Liskallia/vault/skill/ability/effect/spi/AbstractSmiteAbility;percentAbilityPowerDealtMax:F"))
    private float woldsvaults$scaleMax(AbstractSmiteAbility ability, Operation<Float> original) {
        return original.call(ability) * BuffGlyphs.intensity(ability);
    }

    @ModifyReturnValue(method = "getAdditionalManaPerBolt", at = @At("RETURN"))
    private float woldsvaults$scaleBoltCost(float cost) {
        return cost * BuffGlyphs.drain((AbstractSmiteAbility) (Object) this);
    }
}
