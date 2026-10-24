package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.integration.bettercombat.DualWieldSnapshotManager;

@Mixin(value = AttributeSnapshotHelper.class, remap = false)
public abstract class MixinAttributeSnapshotHelper {

    @Inject(
            method = "getSnapshot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void woldsVaults$selectDualWieldSnapshot(
            LivingEntity entity,
            CallbackInfoReturnable<AttributeSnapshot> cir
    ) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        AttributeSnapshot selected =
                DualWieldSnapshotManager.getSelected(player);

        if (selected != null) {
            cir.setReturnValue(selected);
        }
    }

    @Inject(method = "refreshSnapshot", at = @At("HEAD"))
    private void woldsVaults$invalidateDualWieldSnapshots(
            ServerPlayer player,
            CallbackInfo ci
    ) {
        DualWieldSnapshotManager.invalidate(player);
    }
}