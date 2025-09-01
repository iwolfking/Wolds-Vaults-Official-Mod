package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;

import appeng.core.definitions.AEItems;
import appeng.hotkeys.InventoryHotkeyAction;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Predicate;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "ae2")
        }
)
@Mixin(value = InventoryHotkeyAction.class, remap = false)
public abstract class MixinInventoryHotkeyAction {
    @Shadow @Final private Predicate<ItemStack> locatable;
    @Shadow @Final private InventoryHotkeyAction.Opener opener;


    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void runWirelessTerminalCurio(Player player, CallbackInfoReturnable<Boolean> cir) {
        SlotResult terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_CRAFTING_TERMINAL.asItem()).orElse(null);
        if (terminalSlot == null) {
            terminalSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, AEItems.WIRELESS_TERMINAL.asItem()).orElse(null);
        }

        if (terminalSlot != null && this.locatable.test(terminalSlot.stack()) && opener.open(player, -1)) {
            cir.setReturnValue(true);
        }
    }
}
