package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.bottle.AbsorptionBottleEffect;
import iskallia.vault.item.bottle.CastAbilityBottleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CastAbilityBottleEffect.class, remap = false)
public interface CastAbilityBottleEffectAccessor {
    @Accessor("abilityId")
    String getAbilityId();
}
