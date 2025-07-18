package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.entry.vending.ProductEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ProductEntry.class, remap = false)
public interface ProductEntryAccessor {
    @Accessor
    int getAmountMin();

    @Accessor
    int getAmountMax();
}
