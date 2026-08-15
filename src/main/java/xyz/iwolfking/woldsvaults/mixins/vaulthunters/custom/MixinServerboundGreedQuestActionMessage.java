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
    /**
     * Refuses accept/abandon/complete requests for greed quests. The quest system is retired; this
     * stops the still-present trader quest tab from writing quest state or paying reputation.
     */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseQuestAction(ServerboundGreedQuestActionMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
