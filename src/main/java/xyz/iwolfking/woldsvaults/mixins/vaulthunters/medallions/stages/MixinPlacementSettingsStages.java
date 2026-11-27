package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.processor.TileMapper;
import iskallia.vault.core.world.template.PlacementSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.medallions.stages.StageTileMapper;

@Mixin(value = PlacementSettings.class, remap = false)
public class MixinPlacementSettingsStages {

    /**
     * Gives a new tile mapper the vault of these settings' processor context before the tile processors are added,
     * so the palette references it inlines while they are added resolve for that vault.
     */
    @Inject(method = "getTileMapper", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void bindStageVault(CallbackInfoReturnable<TileMapper> cir, @Local TileMapper mapper) {
        ProcessorContext context = ((PlacementSettings) (Object) this).getProcessorContext();
        ((StageTileMapper) (Object) mapper).woldsvaults$setStageVault(context == null ? null : context.getVault());
    }
}
