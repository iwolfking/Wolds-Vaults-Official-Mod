package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.items.ItemScavengerPouch;

@Mixin(value = Inventory.class, priority = 1500)
public class MixinPlayerInventory {
    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void handleInterceptingItemAddition(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if(ItemScavengerPouch.interceptPlayerInventoryItemAddition((Inventory)(Object)this, pStack)) {
            cir.setReturnValue(true);
        }
    }
}
