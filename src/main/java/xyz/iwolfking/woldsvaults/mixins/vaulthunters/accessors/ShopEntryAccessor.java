package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "iskallia.vault.config.ShopPedestalConfig$ShopEntry", remap = false)
public interface ShopEntryAccessor {
    @Accessor("OFFER")
    ItemStack getOffer();

    @Accessor("CURRENCY")
    Item getCurrency();

    @Accessor("MIN_COST")
    int getMinCost();

    @Accessor("MAX_COST")
    int getMaxCost();

}
