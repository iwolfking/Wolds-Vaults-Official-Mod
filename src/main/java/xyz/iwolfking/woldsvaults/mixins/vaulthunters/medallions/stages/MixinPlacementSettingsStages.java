package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.template.PlacementSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.iwolfking.woldsvaults.medallions.stages.StageTileMapper;

@Mixin(value = PlacementSettings.class, remap = false)
public class MixinPlacementSettingsStages {

    /**
     * Gives the tile mapper these settings build the vault of their processor context, so the palette references it
     * inlines resolve for that vault.
     */
    @ModifyArg(method = "getTileMapper", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z"))
    private Object bindStageVault(Object mapper) {
        ProcessorContext context = ((PlacementSettings) (Object) this).getProcessorContext();
        ((StageTileMapper) mapper).woldsvaults$setStageVault(context == null ? null : context.getVault());
        return mapper;
    }
}
