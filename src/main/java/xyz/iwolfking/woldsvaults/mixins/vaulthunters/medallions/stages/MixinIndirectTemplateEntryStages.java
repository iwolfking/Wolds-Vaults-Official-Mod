package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.data.key.TemplatePoolKey;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.template.context.TemplatePoolContext;
import iskallia.vault.core.world.template.data.IndirectTemplateEntry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.stages.VaultStages;

@Mixin(value = IndirectTemplateEntry.class, remap = false)
public class MixinIndirectTemplateEntryStages {

    /**
     * Flattens the stage pool instead of the referenced one when the reference resolves to a stage id for the
     * context's vault. The entry keeps its normal reference id.
     */
    @WrapOperation(
            method = "flatten",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/template/data/IndirectTemplateEntry;getReference()Liskallia/vault/core/data/key/TemplatePoolKey;")
    )
    private TemplatePoolKey resolveStagePool(IndirectTemplateEntry entry, Operation<TemplatePoolKey> original,
                                             @Local(argsOnly = true) TemplatePoolContext context) {
        if (context == null) {
            return original.call(entry);
        }
        ResourceLocation id = entry.getReferenceId();
        ResourceLocation resolved = VaultStages.resolvePool(context.getVault(), id);
        if (resolved == null || resolved.equals(id)) {
            return original.call(entry);
        }
        TemplatePoolKey key = VaultRegistry.TEMPLATE_POOL.getKey(resolved);
        if (key == null) {
            WoldLogger.errorOnce("greed-stage-pool-" + resolved,
                    "Greed stage pool {} is not registered; vaults read {} in its place.", resolved, id);
            return original.call(entry);
        }
        return key;
    }
}
