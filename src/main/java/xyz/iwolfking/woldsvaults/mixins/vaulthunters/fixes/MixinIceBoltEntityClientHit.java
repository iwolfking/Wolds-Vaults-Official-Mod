package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.entity.entity.IceBoltEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IceBoltEntity.class, remap = false)
public abstract class MixinIceBoltEntityClientHit {

    /**
     * A client removes its ice bolt on an entity hit it predicted, so a hit looks instant, but never on a predicted block
     * hit: a steered bolt can clip a corner on the client that the server's bolt flies past, and the server's removal
     * handles real block hits.
     */
    @WrapOperation(method = "lambda$onHit$1", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/IceBoltEntity;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", remap = true))
    private void woldsvaults$serverRemovesBolt(IceBoltEntity instance, Entity.RemovalReason reason, Operation<Void> original,
                                               @Local(argsOnly = true) HitResult result) {
        if (!instance.level.isClientSide() || result.getType() == HitResult.Type.ENTITY) {
            original.call(instance, reason);
        }
    }
}
