package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundRebirthMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Retires the legacy greed trial. The old flow let any post-Herald player open an infinite rebirth
 * vault from the retired greed tree screen, and its ending called {@code incrementGreedTier}, which
 * zeroes reputation - so a legacy win would both hand out a rank the rework never authorised and
 * wipe the reputation the ladder is measured in.
 *
 * <p>The packet is cancelled rather than the ending, because the ending is the only seam the
 * rework's own vessel trial shares with it: {@code MixinRebirthObjective} already intercepts
 * {@code endGreedTrial} for rank-up trials, and cancelling base's ending outright for everything
 * else would strand a player in an arena that never closes. With no way left to open a legacy
 * trial vault, base's rank-up branch is unreachable and the objective itself stays intact - the
 * rework still builds vessel trials on it.</p>
 */
@Mixin(value = ServerboundRebirthMessage.class, remap = false)
public class MixinServerboundRebirthMessage {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void refuseLegacyGreedTrial(ServerboundRebirthMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        contextSupplier.get().setPacketHandled(true);
        ci.cancel();
    }
}
