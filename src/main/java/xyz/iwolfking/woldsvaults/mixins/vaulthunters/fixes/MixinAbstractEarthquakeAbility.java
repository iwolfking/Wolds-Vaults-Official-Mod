package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.VaultChestBlock;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.skill.ability.effect.spi.AbstractEarthquakeAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(value = AbstractEarthquakeAbility.class, remap = false)
public class MixinAbstractEarthquakeAbility {
    @WrapOperation(method = "breakChests", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultChestTileEntity;isVaultChest()Z"))
    private boolean checkIfChestIsLocked(VaultChestTileEntity instance, Operation<Boolean> original) {
        if(instance.getBlockState().getBlock() instanceof VaultChestBlock vaultChestBlock) {
            if(vaultChestBlock.isLocked()) {
                return false;
            }
        }

        return original.call(instance);
    }

    /** Earthquake falloff is measured from the shock's position (a Bundled Spell landing, a mine), not from the player. */
    @WrapOperation(method = "triggerExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;distanceTo(Lnet/minecraft/world/entity/Entity;)F", remap = true))
    private float woldsvaults$falloffFromShock(ServerPlayer player, Entity entity, Operation<Float> original,
                                               @Local(argsOnly = true) Vec3 pos) {
        return (float) entity.position().distanceTo(pos);
    }
}
