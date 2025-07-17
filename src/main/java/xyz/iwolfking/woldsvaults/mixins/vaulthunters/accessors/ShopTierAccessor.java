package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "iskallia.vault.config.ShopPedestalConfig$ShopTier", remap = false)
public interface ShopTierAccessor {
    @Accessor("MIN_LEVEL")
    int getMinLevel();

    @Accessor("TRADE_POOL")
    WeightedList<ShopEntryAccessor> getTradePool();

}
