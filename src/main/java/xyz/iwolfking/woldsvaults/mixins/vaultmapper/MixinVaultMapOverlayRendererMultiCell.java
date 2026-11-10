package xyz.iwolfking.woldsvaults.mixins.vaultmapper;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.vaultmapper.VaultMapperMultiCell;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vaultmapper"),
    }
)
@Mixin(value = VaultMapOverlayRenderer.class, remap = false)
public class MixinVaultMapOverlayRendererMultiCell {

    /** A multi-cell room shows its icon once, on its centre cell. */
    @Inject(method = "renderTextureCell", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$oneIconPerMultiCellRoom(BufferBuilder bufferBuilder, VaultCell cell, CallbackInfo ci) {
        if (VaultMapperMultiCell.hidesIcon(cell)) {
            ci.cancel();
        }
    }
}
