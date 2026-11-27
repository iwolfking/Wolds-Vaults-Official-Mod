package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;

@Mixin(ItemCooldowns.class)
public abstract class MixinItemCooldownsRiptide {
    /** Hides a vault trident's riptide cooldown from the equipment scan, so the scan keeps the trident's modifiers. */
    @Inject(method = "isOnCooldown", at = @At("HEAD"), cancellable = true)
    private void ignoreRiptideCooldown(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (RiptideDash.ignoresRiptideCooldown((ItemCooldowns) (Object) this, item)) {
            cir.setReturnValue(false);
        }
    }
}
