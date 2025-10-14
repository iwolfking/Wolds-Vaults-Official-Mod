package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;

import appeng.core.definitions.AEItems;
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ae2")
        }
)
@Mixin(value = WirelessTerminalItem.class, remap = false)
public abstract class MixinWirelessTerminalItem {
    @Shadow protected abstract boolean checkPreconditions(ItemStack item, Player player);

    @Shadow public abstract MenuType<?> getMenuType();


    @Inject(method = "openFromInventory(Lnet/minecraft/world/entity/player/Player;IZ)Z", at = @At("HEAD"), cancellable = true)
    private void openWirelessTerminalFromCurio(Player player, int inventorySlot, boolean returningFromSubmenu, CallbackInfoReturnable<Boolean> cir) {
        if (inventorySlot == -1) {
            SlotResult terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()).orElse(null);
            if (terminalSlot == null) {
                terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_TERMINAL.asItem()).orElse(null);
            }

            if (terminalSlot != null) {
                ItemStack curioStack = terminalSlot.stack();
                if (curioStack.getItem() instanceof WirelessTerminalItem) {
                    cir.setReturnValue(this.checkPreconditions(curioStack, player) && MenuOpener.open(this.getMenuType(), player, MenuLocators.forInventorySlot(-1), returningFromSubmenu));
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }
}
