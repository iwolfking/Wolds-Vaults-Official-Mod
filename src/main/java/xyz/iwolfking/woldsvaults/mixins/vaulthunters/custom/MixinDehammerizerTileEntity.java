package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.DehammerizerTileEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

@Mixin(value = DehammerizerTileEntity.class, remap = false)
public class MixinDehammerizerTileEntity {
    @Inject(method = "hasDehammerizerAround", at = @At("HEAD"), cancellable = true)
    private static void checkUniversalSuppressor(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(SuppressorTileEntity.isSuppressionActiveAround(entity, SuppressorTileEntity.SuppressorType.HAMMER)) {
            cir.setReturnValue(true);
        }
    }
}
