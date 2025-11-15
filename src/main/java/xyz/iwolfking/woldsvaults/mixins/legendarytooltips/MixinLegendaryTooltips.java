package xyz.iwolfking.woldsvaults.mixins.legendarytooltips;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltips;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LegendaryTooltips.class, remap = false)
public class MixinLegendaryTooltips {
    @WrapOperation(method = "onRenderTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z", remap = true))
    private static boolean renderTickHead(Slot instance, Operation<Boolean> original){
        try {
            return original.call(instance);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /* Thank you forge for this genius behavior
    java.lang.RuntimeException: Slot 0 not in valid range - [0,0)
        at net.minecraftforge.items.ItemStackHandler.validateSlotIndex(ItemStackHandler.java:207)
        at net.minecraftforge.items.ItemStackHandler.getStackInSlot(ItemStackHandler.java:59)
        at net.minecraftforge.items.SlotItemHandler.getItem(SlotItemHandler.java:40)
        at net.minecraft.world.inventory.Slot.hasItem(Slot.java:55)
        at com.anthonyhilyard.legendarytooltips.LegendaryTooltips.onRenderTick(LegendaryTooltips.java:114)
    */
}
