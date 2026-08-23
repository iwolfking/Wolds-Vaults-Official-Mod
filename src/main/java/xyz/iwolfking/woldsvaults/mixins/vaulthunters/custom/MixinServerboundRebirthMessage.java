package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundRebirthMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/** Cancels the rebirth packet, so the base greed trial vault can never be opened. */
@Mixin(value = ServerboundRebirthMessage.class, remap = false)
public class MixinServerboundRebirthMessage {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseLegacyGreedTrial(ServerboundRebirthMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
