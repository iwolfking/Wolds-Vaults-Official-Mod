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
    /**
     * Refuses to open the old greed tree screen. The player menu no longer offers the tab
     * (see {@code MixinSkillTabContainerElement}), and this closes the network path a stale or
     * modified client could still use to reach the retired tree. Unlocked greed nodes keep their
     * effects; only the screen is gone.
     */
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseGreedTreeScreen(ServerboundOpenGreedMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
