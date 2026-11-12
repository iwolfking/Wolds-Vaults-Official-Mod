package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundOpenGreedMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerboundOpenGreedMessage.class, remap = false)
public class MixinServerboundOpenGreedMessage {
    /** Refuses to open the greed tree screen. Unlocked greed nodes keep their effects. */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseGreedTreeScreen(ServerboundOpenGreedMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
