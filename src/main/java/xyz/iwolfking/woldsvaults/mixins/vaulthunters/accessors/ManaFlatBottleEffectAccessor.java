package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.bottle.CooldownReductionBottleEffect;
import iskallia.vault.item.bottle.ManaFlatBottleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ManaFlatBottleEffect.class, remap = false)
public interface ManaFlatBottleEffectAccessor {
    @Accessor("amount")
    float getAmount();
}
