package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.processor.Palette;
import iskallia.vault.core.world.processor.TileMapper;
import iskallia.vault.core.world.processor.tile.CachedPaletteContainer;
import iskallia.vault.core.world.processor.tile.ReferenceTileProcessor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.stages.StageTileMapper;
import xyz.iwolfking.woldsvaults.medallions.stages.VaultStages;

import javax.annotation.Nullable;

@Mixin(value = TileMapper.class, remap = false)
public class MixinTileMapperStages implements StageTileMapper {
    @Unique
    @Nullable
    private Vault woldsvaults$stageVault;

    @Override
    @Nullable
    public Vault woldsvaults$getStageVault() {
        return this.woldsvaults$stageVault;
    }

    @Override
    public void woldsvaults$setStageVault(@Nullable Vault vault) {
        this.woldsvaults$stageVault = vault;
    }

    /**
     * Inlines the stage palette instead of the referenced one when the reference resolves to a stage id for this
     * mapper's vault. The stage palette is read from the registry and the reference's own cache is left untouched.
     */
    @WrapOperation(
            method = "flattenReference",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/processor/tile/CachedPaletteContainer;getCachedPaletteForVersion(Liskallia/vault/core/Version;)Liskallia/vault/core/world/processor/Palette;")
    )
    private Palette resolveStagePalette(CachedPaletteContainer container, Version version, Operation<Palette> original,
                                        @Local(argsOnly = true) ReferenceTileProcessor reference) {
        ResourceLocation id = reference.getPool().keySet().iterator().next();
        ResourceLocation resolved = VaultStages.resolvePalette(this.woldsvaults$stageVault, id);
        if (resolved.equals(id)) {
            return original.call(container, version);
        }
        PaletteKey key = VaultRegistry.PALETTE.getKey(resolved);
        if (key == null) {
            WoldLogger.errorOnce("greed-stage-palette-" + resolved,
                    "Greed stage palette {} is not registered; vaults read {} in its place.", resolved, id);
            return original.call(container, version);
        }
        return key.get(version);
    }
}
