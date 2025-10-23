package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpiritExtractorTileEntity.RecoveryCost.class, remap = false)
public class MixinRecoveryCost {
    @Redirect(method = "lambda$calculate$2", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
    private int capLevelCostScaling(int a, int b) {
        if(b > 100) {
            return 100;
        }

        return Math.max(a, b);
    }
}
