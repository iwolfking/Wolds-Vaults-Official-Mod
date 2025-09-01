package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;


import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.helpers.WirelessCraftingTerminalMenuHost;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessCraftingTerminalItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
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
@Mixin(targets = "appeng.menu.locator.MenuItemLocator", remap = false)
public class MixinMenuItemLocator {
    @Shadow @Final private int itemIndex;

    @Inject(method = "locate", at = @At("HEAD"), cancellable = true)
    private <T> void locateWirelessTerminalInCurio(Player player, Class<T> hostInterface, CallbackInfoReturnable<T> cir) {
        if (itemIndex == -1) {
            SlotResult terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()).orElse(null);
            if (terminalSlot == null) {
                terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_TERMINAL.asItem()).orElse(null);
            }

            if (terminalSlot != null) {
                var curioStack = terminalSlot.stack();
                Item var5 = curioStack.getItem();
                if (var5 instanceof IMenuItem guiItem) {
                    var menuHost = guiItem.getMenuHost(player, this.itemIndex, curioStack, null);
                    if (hostInterface.isInstance(menuHost)) {
                        cir.setReturnValue(hostInterface.cast(menuHost));
                        return;
                    }
                    if (menuHost != null) {
                        AELog.warn("[Wolds] Item in curio slot %d of %s did not create a compatible menu of type %s: %s", this.itemIndex, player,
                            hostInterface, menuHost);
                    }
                }
            }
            cir.setReturnValue(null);
        }
    }
}
