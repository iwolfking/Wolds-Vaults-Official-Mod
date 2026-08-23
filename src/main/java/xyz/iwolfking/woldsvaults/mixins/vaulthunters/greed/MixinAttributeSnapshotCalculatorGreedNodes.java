package xyz.iwolfking.woldsvaults.mixins.vaulthunters.greed;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancels the greed snapshot pass, so {@code greed_gear_attribute} nodes grant no attributes. */
@Mixin(value = AttributeSnapshotCalculator.class, remap = false)
public abstract class MixinAttributeSnapshotCalculatorGreedNodes {
    @Inject(method = "addGreedInformationToSnapshot(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/snapshot/AttributeSnapshot;)V",
            at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$skipGreedNodeAttributes(ServerPlayer player, AttributeSnapshot snapshot, CallbackInfo ci) {
        ci.cancel();
    }
}
