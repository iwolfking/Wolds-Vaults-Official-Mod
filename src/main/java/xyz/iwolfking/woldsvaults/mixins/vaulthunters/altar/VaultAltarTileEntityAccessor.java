package xyz.iwolfking.woldsvaults.mixins.vaulthunters.altar;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.block.entity.VaultAltarTileEntity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.api.lib.VaultAltarTileEntityInterface;

@Mixin(value = VaultAltarTileEntity.class, remap = false)
public class VaultAltarTileEntityAccessor implements VaultAltarTileEntityInterface {

    @Shadow
    private void resetAltar(ServerLevel world) {
    }

    @Shadow
    private void updateDisplayedIndex(AltarInfusionRecipe recipe) {

    }


    @Override
    public void invokeResetAltar(ServerLevel world) {
        this.resetAltar(world);
    }

    @Override
    public void invokeUpdateDisplayedIndex(AltarInfusionRecipe recipe) {
        this.updateDisplayedIndex(recipe);

    }
}
