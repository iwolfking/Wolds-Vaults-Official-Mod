package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import nl.requios.effortlessbuilding.gui.DiamondRandomizerBagContainer;
import nl.requios.effortlessbuilding.gui.RandomizerBagContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "effortlessbuilding")
        }
)
@Mixin(DiamondRandomizerBagContainer.class)
public abstract class MixinDiamondRandomizerBagContainer {

    @Shadow
    public abstract Slot getSlot(int index);

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void preventNonBlockPlacement(int slotId, int dragType, ClickType clickTypeIn, Player player, CallbackInfo ci) {
        if (slotId >= 0 && slotId < 5) {
            DiamondRandomizerBagContainer menu = (DiamondRandomizerBagContainer) (Object) this;
            ItemStack carried = menu.getCarried();

            if (!carried.isEmpty() && !(carried.getItem() instanceof BlockItem)) {
                ci.cancel();
            }

            if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
                ItemStack hotbarItem = player.getInventory().getItem(dragType);
                if (!hotbarItem.isEmpty() && !(hotbarItem.getItem() instanceof BlockItem)) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void preventNonBlockQuickMove(Player playerIn, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (slotIndex >= 5) {
            Slot slot = this.getSlot(slotIndex);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (!(stack.getItem() instanceof BlockItem)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                }
            }
        }
    }
}