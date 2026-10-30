package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.DebagnetizerBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

@Mixin(value = DebagnetizerBlock.class, remap = false)
public class MixinDebagnetizerBlock {
    @Inject(method = "isInRange", at = @At("HEAD"), cancellable = true)
    private void checkUniversalSuppressor(Level world, Vec3 origin, CallbackInfoReturnable<Boolean> cir) {
        if(SuppressorTileEntity.isSuppressionActiveAround(world, origin, SuppressorTileEntity.SuppressorType.BACKPACKS)) {
            cir.setReturnValue(true);
        }
    }
}
