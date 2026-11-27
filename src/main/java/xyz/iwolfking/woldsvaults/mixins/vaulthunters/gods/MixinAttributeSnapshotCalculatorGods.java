package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;

@Mixin(value = AttributeSnapshotCalculator.class, remap = false)
public abstract class MixinAttributeSnapshotCalculatorGods {
    /**
     * @author PoorMansPhysicist
     * @reason fold god tree gear attributes into the snapshot alongside the greed tree
     */
    @Inject(method = "computeSnapshot", at = @At(value = "INVOKE",
            target = "Liskallia/vault/snapshot/AttributeSnapshotCalculator;addGreedInformationToSnapshot(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/snapshot/AttributeSnapshot;)V",
            shift = At.Shift.AFTER))
    private static void addGodInformationToSnapshot(ServerPlayer player, AttributeSnapshot snapshot, CallbackInfo ci) {
        GodCarryover.addGodInformationToSnapshot(player, snapshot);
    }
}
