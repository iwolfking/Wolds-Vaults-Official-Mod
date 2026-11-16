package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions.stages;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.TreasureDoorTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.world.template.JigsawTemplate;
import iskallia.vault.core.world.template.Template;
import iskallia.vault.core.world.template.context.TemplatePoolContext;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.stages.VaultStages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(value = TreasureDoorTileEntity.class, remap = false)
public class MixinTreasureDoorTileEntityStages {

    /** Builds the door's room from the stage pool when the door's pool resolves to a stage id for the door's vault. */
    @WrapOperation(
            method = "lambda$generateRoom$6",
            at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/TreasureDoorTileEntity;getPool()Lnet/minecraft/resources/ResourceLocation;")
    )
    private static ResourceLocation resolveDoorPool(TreasureDoorTileEntity door, Operation<ResourceLocation> original,
                                                    @Local(argsOnly = true) ServerLevel world) {
        return woldsvaults$resolvePool(world, original.call(door));
    }

    /** Writes the resolved pool into the jigsaw the door's room grows from, matching {@link #resolveDoorPool}. */
    @WrapOperation(
            method = "lambda$generateRoom$6",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Liskallia/vault/block/entity/TreasureDoorTileEntity;pool:Lnet/minecraft/resources/ResourceLocation;")
    )
    private static ResourceLocation resolveJigsawPool(TreasureDoorTileEntity door, Operation<ResourceLocation> original,
                                                      @Local(argsOnly = true) ServerLevel world) {
        return woldsvaults$resolvePool(world, original.call(door));
    }

    /** Places the door's room with each palette resolved for the door's vault. */
    @WrapOperation(
            method = "lambda$generateRoom$6",
            at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/template/JigsawTemplate;of(Liskallia/vault/core/Version;Liskallia/vault/core/world/template/Template;Ljava/util/Collection;ILiskallia/vault/core/random/RandomSource;Liskallia/vault/core/world/template/context/TemplatePoolContext;)Liskallia/vault/core/world/template/JigsawTemplate;")
    )
    private static JigsawTemplate resolveDoorPalettes(Version version, Template root, Collection<PaletteKey> palettes, int depth,
                                                      RandomSource random, TemplatePoolContext context,
                                                      Operation<JigsawTemplate> original) {
        Vault vault = context == null ? null : context.getVault();
        List<PaletteKey> resolved = new ArrayList<>(palettes.size());
        for (PaletteKey palette : palettes) {
            ResourceLocation id = palette.getId();
            ResourceLocation stageId = VaultStages.resolvePalette(vault, id);
            PaletteKey stageKey = stageId.equals(id) ? palette : VaultRegistry.PALETTE.getKey(stageId);
            if (stageKey == null) {
                WoldLogger.errorOnce("greed-stage-palette-" + stageId,
                        "Greed stage palette {} is not registered; vaults read {} in its place.", stageId, id);
                stageKey = palette;
            }
            resolved.add(stageKey);
        }
        return original.call(version, root, resolved, depth, random, context);
    }

    @Unique
    private static ResourceLocation woldsvaults$resolvePool(ServerLevel world, ResourceLocation pool) {
        if (pool == null) {
            return null;
        }
        Vault vault = ServerVaults.get(world).orElse(null);
        ResourceLocation resolved = VaultStages.resolvePool(vault, pool);
        if (resolved.equals(pool)) {
            return pool;
        }
        if (VaultRegistry.TEMPLATE_POOL.getKey(resolved) == null) {
            WoldLogger.errorOnce("greed-stage-pool-" + resolved,
                    "Greed stage pool {} is not registered; vaults read {} in its place.", resolved, pool);
            return pool;
        }
        return resolved;
    }
}
