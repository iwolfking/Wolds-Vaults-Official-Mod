package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.VaultDecoyProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.augments.effect.ProjectileSteering;

@Mixin(value = VaultDecoyProjectile.class, remap = false)
public abstract class MixinVaultDecoyProjectileOrbit {

    /** An orbiting Taunt decoy is not removed by its flight limit; it spawns its decoy when the orbit ends. */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultDecoyProjectile;discard()V", remap = true), remap = true)
    private void woldsvaults$holdWhileOrbiting(VaultDecoyProjectile instance, Operation<Void> original) {
        if (!ProjectileSteering.isOrbiting(instance)) {
            original.call(instance);
        }
    }
}
