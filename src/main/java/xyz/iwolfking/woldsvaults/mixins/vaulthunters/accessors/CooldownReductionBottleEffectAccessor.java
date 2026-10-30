package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.bottle.AbsorptionBottleEffect;
import iskallia.vault.item.bottle.CooldownReductionBottleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CooldownReductionBottleEffect.class, remap = false)
public interface CooldownReductionBottleEffectAccessor {
    @Accessor("amount")
    float getAmount();
}
