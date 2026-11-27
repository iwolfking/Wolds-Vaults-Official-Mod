package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.projectile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.entity.entity.VaultBlizzardShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.ProjectileSteering;

@Mixin(value = VaultBlizzardShard.class, remap = false)
public abstract class MixinVaultBlizzardShardOrbit {

    /** An orbiting Blizzard shard with no locked target is not destroyed by its 20-tick limit while the orbit lasts. */
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultBlizzardShard;destroy()V", ordinal = 2, remap = false), remap = true)
    private void woldsvaults$holdWhileOrbiting(VaultBlizzardShard instance, Operation<Void> original) {
        if (!ProjectileSteering.isOrbiting(instance)) {
            original.call(instance);
        }
    }
}
