package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.InventoryUtilItemAccessAccessor;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = InventoryUtil.class, remap = false)
public abstract class MixinInventoryUtil {
    @Inject(method = "getMissingInputs(Ljava/util/List;Lnet/minecraft/world/entity/player/Inventory;Liskallia/vault/container/oversized/OverSizedInventory;)Ljava/util/List;", at = @At(value = "TAIL"), cancellable = true)
    private static void checkCurrencyProperly(List<ItemStack> recipeInputs, Inventory playerInventory, OverSizedInventory containerInventory, CallbackInfoReturnable<List<ItemStack>> cir, @Local(ordinal = 1) List<ItemStack> missing) {
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

            if(itemAccesses == null) {
                return;
            }

            if(CoinDefinition.hasEnoughCurrency(itemAccesses, stack)) {
                cir.setReturnValue(trueMissing);
                return;
            }
        }
    }

    @Shadow
    public static List<InventoryUtil.ItemAccess> findAllItems(Player player) {
        return null;
    }

    @Shadow
    public static boolean isEqualCrafting(ItemStack thisStack, ItemStack thatStack) {
        return false;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static boolean consumeInputs(List<ItemStack> recipeInputs, Inventory playerInventory, OverSizedInventory tileInv, boolean simulate, List<OverSizedItemStack> consumed) {
        if (playerInventory.player.isCreative()) {
            return true;
        } else {
            boolean success = true;

            for (ItemStack input : recipeInputs) {
                int neededCount = input.getCount();
                NonNullList<OverSizedItemStack> overSizedContents = tileInv.getOverSizedContents();

                for (int slot = 0; slot < overSizedContents.size(); ++slot) {
                    OverSizedItemStack overSized = (OverSizedItemStack) overSizedContents.get(slot);
                    if (neededCount <= 0) {
                        break;
                    }

                    if (isEqualCrafting(input, overSized.stack())) {
                        int deductedAmount = Math.min(neededCount, overSized.amount());
                        if (!simulate) {
                            tileInv.setOverSizedStack(slot, overSized.addCopy(-deductedAmount));
                            consumed.add(overSized.copyAmount(deductedAmount));
                        }

                        neededCount -= overSized.amount();
                    }
                }

                for (InventoryUtil.ItemAccess plStack : findAllItems(playerInventory.player)) {
                    if (neededCount <= 0) {
                        break;
                    }

                    if (isEqualCrafting(input, ((InventoryUtilItemAccessAccessor)plStack).getActualStack())) {
                        int deductedAmount = Math.min(neededCount, ((InventoryUtilItemAccessAccessor)plStack).getActualStack().getCount());
                        if (!simulate) {
                            ((InventoryUtilItemAccessAccessor)plStack).getActualStack().shrink(deductedAmount);
                            ((InventoryUtilItemAccessAccessor)plStack).getSetter().accept(((InventoryUtilItemAccessAccessor)plStack).getActualStack());
                            ItemStack deducted = ((InventoryUtilItemAccessAccessor)plStack).getActualStack().copy();
                            deducted.setCount(deductedAmount);
                            consumed.add(OverSizedItemStack.of(deducted));
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
                    success = false;
                }
            }

            return success;
        }
    }
}
