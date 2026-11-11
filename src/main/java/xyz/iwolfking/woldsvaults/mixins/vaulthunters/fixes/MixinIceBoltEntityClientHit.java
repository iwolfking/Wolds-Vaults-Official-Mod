package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.IceBoltEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IceBoltEntity.class, remap = false)
public abstract class MixinIceBoltEntityClientHit {

    /**
     * A client never removes its ice bolt on a hit it predicted; the server's removal does. A steered bolt drifts on the
     * client, and a predicted hit there used to delete the bolt locally while the server's bolt flew on.
     */
    @WrapOperation(method = "lambda$onHit$1", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/IceBoltEntity;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", remap = true))
    private void woldsvaults$serverRemovesBolt(IceBoltEntity instance, Entity.RemovalReason reason, Operation<Void> original) {
        if (!instance.level.isClientSide()) {
            original.call(instance, reason);
        }
    }
}
