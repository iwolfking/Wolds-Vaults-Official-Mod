package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundGreedQuestRerollMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerboundGreedQuestRerollMessage.class, remap = false)
public class MixinServerboundGreedQuestRerollMessage {
    /** Refuses paid greed quest rerolls before any greed coins are consumed. */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseQuestReroll(ServerboundGreedQuestRerollMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
