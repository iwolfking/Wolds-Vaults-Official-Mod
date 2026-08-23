package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundGreedQuestActionMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ServerboundGreedQuestActionMessage.class, remap = false)
public class MixinServerboundGreedQuestActionMessage {
    /** Refuses accept, abandon and complete requests for greed quests. */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseQuestAction(ServerboundGreedQuestActionMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
