package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import iskallia.vault.block.base.InventoryRetainerBlock;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "effortlessbuilding")
        }
)
@Mixin(value = EventHandler.class, remap = false)
public abstract class MixinEventHandler {

    @Inject(method = "onBlockPlaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void preventPlacingRetainerBlocks(BlockEvent.EntityPlaceEvent event, CallbackInfo ci) {
        if(event.getPlacedBlock().getBlock() instanceof InventoryRetainerBlock<?>) {
            ci.cancel();
            return;
        }
    }
}
