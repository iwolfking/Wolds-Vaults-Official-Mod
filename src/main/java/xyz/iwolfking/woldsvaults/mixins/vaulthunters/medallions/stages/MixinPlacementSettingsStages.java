package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.processor.TileMapper;
import iskallia.vault.core.world.template.PlacementSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.medallions.stages.StageTileMapper;

@Mixin(value = PlacementSettings.class, remap = false)
public class MixinPlacementSettingsStages {

    /**
     * Gives the tile mapper these settings build the vault of their processor context, so the palette references it
     * inlines resolve for that vault.
     */
    @WrapOperation(method = "getTileMapper", at = @At(value = "NEW", target = "Liskallia/vault/core/world/processor/TileMapper;<init>()V"))
    private TileMapper bindStageVault(Operation<TileMapper> original) {
        TileMapper mapper = original.call();
        ProcessorContext context = ((PlacementSettings) (Object) this).getProcessorContext();
        ((StageTileMapper) (Object) mapper).woldsvaults$setStageVault(context == null ? null : context.getVault());
        return mapper;
    }
}
