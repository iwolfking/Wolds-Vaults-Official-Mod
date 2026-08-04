package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.bottle.AbsorptionBottleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbsorptionBottleEffect.class, remap = false)
public interface AbsorptionBottleEffectAccessor {
    @Accessor("amount")
    float getAmount();
}
