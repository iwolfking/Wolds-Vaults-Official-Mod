package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.AbilitySuppressorTileEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.blocks.tiles.SuppressorTileEntity;

@Mixin(value = AbilitySuppressorTileEntity.class, remap = false)
public class MixinAbilitySuppressorTileEntity {
    @Inject(method = "hasAbilitySuppressorAround", at = @At("HEAD"), cancellable = true)
    private static void checkUniversalSuppressor(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(SuppressorTileEntity.isSuppressionActiveAround(entity, SuppressorTileEntity.SuppressorType.ABILITY)) {
            cir.setReturnValue(true);
        }
    }
}
