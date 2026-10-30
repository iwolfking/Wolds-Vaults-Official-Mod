package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.EnderAnchorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

@Mixin(value = EnderAnchorTileEntity.class, remap = false)
public class MixinEnderAnchorTileEntity {
    @Inject(method = "hasEnderAnchorAround(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private static void checkUniversalSuppressor(BlockPos pos, Level level, CallbackInfoReturnable<Boolean> cir) {
       if(SuppressorTileEntity.isSuppressionActiveAround(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), SuppressorTileEntity.SuppressorType.ENDER)) {
            cir.setReturnValue(true);
        }
    }
}
