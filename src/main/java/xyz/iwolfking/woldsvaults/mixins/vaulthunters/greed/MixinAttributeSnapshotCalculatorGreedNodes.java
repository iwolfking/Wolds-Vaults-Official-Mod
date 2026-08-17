package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Design decision D32: the retired greed tree contributes no gear attributes. All 100
 * {@code greed_gear_attribute} nodes in the pack config - including the assassin damage and
 * assassin resistance rows - reach the player only through this one snapshot pass, so cancelling it
 * removes every one of them for every player without touching their unlock records.
 *
 * <p>{@code MixinAttributeSnapshotCalculatorGods} injects after the CALL to this method inside
 * {@code computeSnapshot}, which is unaffected by the callee returning early.
 */
@Mixin(value = AttributeSnapshotCalculator.class, remap = false)
public abstract class MixinAttributeSnapshotCalculatorGreedNodes {
    @Inject(method = "addGreedInformationToSnapshot(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/snapshot/AttributeSnapshot;)V",
            at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$skipGreedNodeAttributes(ServerPlayer player, AttributeSnapshot snapshot, CallbackInfo ci) {
        ci.cancel();
    }
}
