package xyz.iwolfking.woldsvaults.mixins;

import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(value = WirelessTerminalItem.class, remap = false)
public abstract class MixinWirelessTerminalItem {
    @Shadow protected abstract boolean checkPreconditions(ItemStack item, Player player);

    @Shadow public abstract MenuType<?> getMenuType();

    /**
     * @author iwolfking
     * @reason Add curio support to wireless terminal
     */
    @Overwrite
    protected boolean openFromInventory(Player player, int inventorySlot, boolean returningFromSubmenu) {
        ItemStack is = player.getInventory().getItem(inventorySlot);

        IItemHandlerModifiable handler = CuriosApi.getCuriosHelper().getEquippedCurios(player).resolve().get();

        for(int i =0; i < handler.getSlots(); i++) {
            ItemStack curioStack = handler.getStackInSlot(i);
            if(curioStack.getItem() instanceof WirelessTerminalItem || curioStack.getItem() instanceof WirelessCraftingTerminalItem) {
                is = curioStack;
            }
        }

        return this.checkPreconditions(is, player) && MenuOpener.open(this.getMenuType(), player, MenuLocators.forInventorySlot(inventorySlot), returningFromSubmenu);
    }
}
