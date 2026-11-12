package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.TreasurePedestalBlock;
import iskallia.vault.block.entity.TreasurePedestalTileEntity;
import iskallia.vault.world.data.ServerVaults;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TreasurePedestalBlock.class, remap = false)
public class MixinTreasurePedestalBlock {
    @WrapOperation(method = "use", remap = true, at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/TreasurePedestalTileEntity;isVaultPedestal()Z", remap = false))
    private boolean alwaysReturnTrueIfInVault(TreasurePedestalTileEntity instance, Operation<Boolean> original) {
        if(instance.getLevel() != null && ServerVaults.isInVault(instance.getLevel())) {
            return true;
        }

        return original.call(instance);
    }
}
