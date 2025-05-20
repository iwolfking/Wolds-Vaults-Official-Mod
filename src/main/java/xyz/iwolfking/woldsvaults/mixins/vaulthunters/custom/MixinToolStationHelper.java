package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.ToolStationTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.gear.crafting.ToolStationHelper;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static iskallia.vault.util.InventoryUtil.findAllItems;

@Mixin(value = ToolStationHelper.class, remap = false)
public abstract class MixinToolStationHelper {
    @Shadow
    private static boolean isEqualCrafting(ItemStack thisStack, ItemStack thatStack) {
        return false;
    }

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

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static boolean consumeInputs(List<ItemStack> recipeInputs, Inventory playerInventory, ToolStationTileEntity tile, boolean simulate, List<OverSizedItemStack> consumed) {
        boolean missingInput = true;

        for (ItemStack input : recipeInputs) {
            int neededCount = input.getCount();
            NonNullList<OverSizedItemStack> overSizedContents = tile.getInventory().getOverSizedContents();

            for (int slot = 0; slot < overSizedContents.size(); ++slot) {
                OverSizedItemStack overSized = (OverSizedItemStack) overSizedContents.get(slot);
                if (neededCount <= 0) {
                    break;
                }

                if (isEqualCrafting(input, overSized.stack())) {
                    int deductedAmount = Math.min(neededCount, overSized.amount());
                    if (!simulate) {
                        tile.getInventory().setOverSizedStack(slot, overSized.addCopy(-deductedAmount));
                        consumed.add(overSized.copyAmount(deductedAmount));
                    }

                    neededCount -= overSized.amount();
                }
            }

            for (ItemStack plStack : playerInventory.items) {
                if (neededCount <= 0) {
                    break;
                }

                if (isEqualCrafting(input, plStack)) {
                    int deductedAmount = Math.min(neededCount, plStack.getCount());
                    if (!simulate) {
                        ItemStack deducted = plStack.copy();
                        deducted.setCount(deductedAmount);
                        consumed.add(OverSizedItemStack.of(deducted));
                        plStack.shrink(deductedAmount);
                    }

                    neededCount -= deductedAmount;
                }
            }

            if(neededCount > 0 && CoinDefinition.getCoinDefinition(input.getItem()).isPresent()) {
                ItemStack inputNeeded = input.copy();
                inputNeeded.setCount(neededCount);
                List<InventoryUtil.ItemAccess> itemAccesses = findAllItems(playerInventory.player);
                if(CoinDefinition.hasEnoughCurrency(itemAccesses, inputNeeded)) {
                    if(!simulate) {
                        CoinDefinition.extractCurrency(playerInventory.player, itemAccesses, inputNeeded);
                    }
                    neededCount -= neededCount;
                }
            }

            if (neededCount > 0) {
                missingInput = false;
            }
        }

        return missingInput;
    }
}
