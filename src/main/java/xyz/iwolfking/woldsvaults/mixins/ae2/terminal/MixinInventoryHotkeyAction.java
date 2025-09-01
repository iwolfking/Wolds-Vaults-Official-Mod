package xyz.iwolfking.woldsvaults.mixins.ae2.terminal;

import appeng.hotkeys.InventoryHotkeyAction;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

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

        IItemHandlerModifiable handler = CuriosApi.getCuriosHelper().getEquippedCurios(player).resolve().orElse(null);
        if (handler == null) {
            return;
        }

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack curioStack = handler.getStackInSlot(i);
            if (this.locatable.test(curioStack) && opener.open(player, -1)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
