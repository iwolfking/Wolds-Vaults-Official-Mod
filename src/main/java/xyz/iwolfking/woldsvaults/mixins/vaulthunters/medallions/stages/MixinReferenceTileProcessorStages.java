package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.data.key.VersionedKey;
import iskallia.vault.core.data.key.registry.KeyRegistry;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.processor.Palette;
import iskallia.vault.core.world.processor.ProcessorContext;
import iskallia.vault.core.world.processor.tile.ReferenceTileProcessor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.stages.VaultStages;

@Mixin(value = ReferenceTileProcessor.class, remap = false)
public class MixinReferenceTileProcessorStages {

    /**
     * Processes a single-entry reference with the stage palette when its id resolves to a stage id for the context's
     * vault. The stage palette is read from the registry for the vault's version; the processor's cache is only
     * consulted, and only written, for the normal id.
     */
    @WrapOperation(
            method = "process(Liskallia/vault/core/world/data/tile/PartialTile;Liskallia/vault/core/world/processor/ProcessorContext;)Liskallia/vault/core/world/data/tile/PartialTile;",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/processor/tile/ReferenceTileProcessor;getCachedPalette(Liskallia/vault/core/world/processor/ProcessorContext;)Liskallia/vault/core/world/processor/Palette;")
    )
    private Palette resolveSingleReference(ReferenceTileProcessor processor, ProcessorContext context, Operation<Palette> original) {
        Vault vault = context.getVault();
        ResourceLocation id = processor.getPool().keySet().iterator().next();
        ResourceLocation resolved = VaultStages.resolvePalette(vault, id);
        if (resolved.equals(id)) {
            return original.call(processor, context);
        }
        PaletteKey key = VaultRegistry.PALETTE.getKey(resolved);
        if (key == null) {
            WoldLogger.errorOnce("greed-stage-palette-" + resolved,
                    "Greed stage palette {} is not registered; vaults read {} in its place.", resolved, id);
            return original.call(processor, context);
        }
        return key.get(vault.get(Vault.VERSION));
    }

    /** Looks up the stage palette when the id a multi-entry reference picked resolves to a stage id for the context's vault. */
    @WrapOperation(
            method = "process(Liskallia/vault/core/world/data/tile/PartialTile;Liskallia/vault/core/world/processor/ProcessorContext;)Liskallia/vault/core/world/data/tile/PartialTile;",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/data/key/registry/KeyRegistry;getKey(Lnet/minecraft/resources/ResourceLocation;)Liskallia/vault/core/data/key/VersionedKey;")
    )
    private VersionedKey<?, ?, ?> resolvePickedReference(KeyRegistry<?, ?, ?> registry, ResourceLocation id,
                                                         Operation<VersionedKey<?, ?, ?>> original,
                                                         @Local(argsOnly = true) ProcessorContext context) {
        ResourceLocation resolved = VaultStages.resolvePalette(context.getVault(), id);
        if (resolved.equals(id)) {
            return original.call(registry, id);
        }
        VersionedKey<?, ?, ?> key = original.call(registry, resolved);
        if (key == null) {
            WoldLogger.errorOnce("greed-stage-palette-" + resolved,
                    "Greed stage palette {} is not registered; vaults read {} in its place.", resolved, id);
            return original.call(registry, id);
        }
        return key;
    }
}
