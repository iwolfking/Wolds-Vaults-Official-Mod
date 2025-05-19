package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.ToolStationTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.gear.crafting.ToolStationHelper;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static iskallia.vault.util.InventoryUtil.findAllItems;

@Mixin(value = ToolStationHelper.class, remap = false)
public class MixinToolStationHelper {
    @Inject(method = "getMissingInputs", at = @At(value = "TAIL"), cancellable = true)
    private static void checkCurrencyProperly(List<ItemStack> recipeInputs, Inventory playerInventory, ToolStationTileEntity tile, CallbackInfoReturnable<List<ItemStack>> cir, @Local(ordinal = 1) List<ItemStack> missing) {
        if(missing.isEmpty()) {
            return;
        }

        List<ItemStack> trueMissing = new ArrayList<>();

        for(ItemStack stack : missing) {
            if(CoinDefinition.getCoinDefinition(stack.getItem()).isEmpty()) {
                trueMissing.add(stack);
                continue;
            }

            List<InventoryUtil.ItemAccess> itemAccesses = findAllItems(playerInventory.player);

            if(CoinDefinition.hasEnoughCurrency(itemAccesses, stack)) {
                cir.setReturnValue(trueMissing);
                return;
            }
        }
    }
}
