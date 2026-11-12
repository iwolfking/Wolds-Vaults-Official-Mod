package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.data.key.TemplateKey;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.generator.layout.VaultGridLayout;
import iskallia.vault.core.world.processor.Palette;
import iskallia.vault.core.world.processor.Processor;
import iskallia.vault.core.world.processor.tile.ReferenceTileProcessor;
import iskallia.vault.core.world.processor.tile.TileProcessor;
import iskallia.vault.core.world.processor.tile.WeightedTileProcessor;
import iskallia.vault.core.world.template.PlacementSettings;
import iskallia.vault.core.world.template.data.TemplateEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.VaultGenUtils;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.config.ThemePaletteRegistryConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.vault.PaletteInjectionModifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = VaultGridLayout.class, remap = false)
public abstract class MixinVaultGridLayout {

    @Unique
    private static final Map<Vault, CachedPalettes> WOLDS_VAULTS$PALETTE_CACHE = new ConcurrentHashMap<>();

    @WrapOperation(
            method = "getRoom",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/core/world/template/PlacementSettings;addProcessors([Liskallia/vault/core/world/processor/Processor;)Liskallia/vault/core/world/template/PlacementSettings;"))
    private <T> PlacementSettings injectThemePalettesIntoRoom(
            PlacementSettings instance,
            Processor<T>[] processors,
            Operation<PlacementSettings> original, @Local(name = "entry") TemplateEntry entry, @Local(argsOnly = true) Vault vault, @Local(argsOnly = true) RegionPos regionPos
    ) {
        TemplateKey templateKey = entry.getTemplate();
        ResourceLocation key = templateKey != null ? templateKey.getId() : null;
        if (key == null) {
            return original.call(instance, processors);
        }

        // Special Room exceptions
        if (key.getPath().contains("labyrinth")) {
            return original.call(instance, processors);
        }

        CachedPalettes cachedPalettes = WOLDS_VAULTS$PALETTE_CACHE.computeIfAbsent(vault, v -> {
            Set<ResourceLocation> pregen = new HashSet<>();
            Set<ResourceLocation> postgen = new HashSet<>();

            VaultModifierUtils.getModifiersOfType(v, PaletteInjectionModifier.class).forEach(modifier -> {
                pregen.addAll(modifier.properties().getPregenPalettes());
                postgen.addAll(modifier.properties().getPostgenPalettes());
            });

            WoldLogger.debug("Cached {} pregen and {} postgen palettes for vault run.", pregen.size(), postgen.size());
            return new CachedPalettes(pregen, postgen);
        });
        Set<ResourceLocation> modifierPregen = cachedPalettes.pregen();
        Set<ResourceLocation> modifierPostgen = cachedPalettes.postgen();

        ResourceLocation themeId = vault.get(Vault.WORLD).get(WorldManager.THEME);
        ThemePaletteRegistryConfig.ThemePaletteMapEntry themePaletteMapEntry = themeId != null ?
                ModConfigs.THEME_PALETTE_REGISTRY.THEME_TO_PALETTE_MAP.getOrDefault(themeId, null) : null;

        boolean canApplyThemeFull = themePaletteMapEntry != null &&
                !themePaletteMapEntry.FULL_PALETTE_ENTRIES.isEmpty() &&
                ModConfigs.THEME_PALETTE_REGISTRY.EXCLUDED_ROOM_POOLS.stream().noneMatch(s -> key.getPath().contains(s)) &&
                !(VaultGenUtils.isInscriptionRoom(vault, regionPos) && !themePaletteMapEntry.affectsInscriptionRooms);

        List<ResourceLocation> combinedPregen = new ArrayList<>();
        if (canApplyThemeFull) {
            combinedPregen.addAll(themePaletteMapEntry.FULL_PALETTE_ENTRIES);
        }

        boolean canApplyModifierPregen = !(VaultGenUtils.isInscriptionRoom(vault, regionPos) && (themePaletteMapEntry != null && !themePaletteMapEntry.affectsInscriptionRooms));
        if (canApplyModifierPregen) {
            combinedPregen.addAll(modifierPregen);
        }

        if (!combinedPregen.isEmpty()) {
            for (ResourceLocation paletteId : combinedPregen) {
                PaletteKey paletteKey = VaultRegistry.PALETTE.getKey(paletteId);
                if (paletteKey == null) {
                    continue;
                }

                Palette palette = paletteKey.get(Version.latest());

                List<TileProcessor> filteredThemeProcessors = palette.getTileProcessors().stream()
                        .filter(tileProcessor -> {
                            if (tileProcessor instanceof ReferenceTileProcessor referenceTileProcessor) {
                                Optional<ResourceLocation> paletteRefId = referenceTileProcessor.getPool().keySet().stream().findFirst();
                                if (paletteRefId.isPresent()) {
                                    ResourceLocation id = paletteRefId.get();
                                    return !id.toString().contains("room_base")
                                            && !id.toString().contains("generic/spawners")
                                            && !id.toString().contains("coin_stack")
                                            && !id.toString().contains("gate_placeholder")
                                            && !id.toString().contains("pedestal_placeholder")
                                            && !id.toString().contains("chest_placeholder")
                                            && !id.toString().contains("treasure_door_placeholder");
                                }
                            }
                            else if (key.toString().contains("aquarium") && tileProcessor instanceof WeightedTileProcessor weightedTileProcessor && weightedTileProcessor.getPredicate().test(PartialTile.of(Blocks.WATER.defaultBlockState()))) {
                                return false;
                            }
                            else if(key.toString().contains("dragon") && tileProcessor instanceof WeightedTileProcessor weightedTileProcessor && weightedTileProcessor.getPredicate().test(PartialTile.of(Blocks.PETRIFIED_OAK_SLAB.defaultBlockState()))) {
                                return false;
                            }
                            else if ((key.toString().contains("cube") || key.toString().contains("puzzle") || key.toString().contains("arena")) && tileProcessor instanceof WeightedTileProcessor weightedTileProcessor && weightedTileProcessor.getPredicate().test(PartialTile.of(Blocks.LIME_WOOL.defaultBlockState()))) {
                                return false;
                            }

                            return true;
                        })
                        .map(tileProcessor -> {
                            if (key.getPath().contains("mine") && tileProcessor instanceof ReferenceTileProcessor referenceTileProcessor) {
                                Optional<ResourceLocation> paletteRefId = referenceTileProcessor.getPool().keySet().stream().findFirst();
                                if (paletteRefId.isPresent()) {
                                    ResourceLocation originalRefId = paletteRefId.get();
                                    if (originalRefId.getPath().contains("ore_placeholder")) {
                                        ResourceLocation mineRefId;
                                        if(originalRefId.getPath().equals("generic/ore_placeholder")) {
                                            mineRefId = ResourceLocUtils.replace(originalRefId, "generic/ore_placeholder", "mine/ore_placeholder_alternate");
                                        }
                                        else {
                                            mineRefId = ResourceLocUtils.replace(originalRefId, "generic/", "mine/");
                                        }

                                        if (VaultRegistry.PALETTE.getKey(mineRefId) != null) {
                                            ReferenceTileProcessor mineProcessor = new ReferenceTileProcessor(new WeightedList<>());
                                            mineProcessor.getPool().add(mineRefId, 1);
                                            return mineProcessor;
                                        }
                                    }
                                }
                            }
                            return tileProcessor;
                        })
                        .toList();
                filteredThemeProcessors.forEach(instance::addProcessorAtBeginning);
            }
        }

        boolean canApplyThemePost = themePaletteMapEntry != null &&
                !themePaletteMapEntry.POST_PROCESSING_PALETTES.isEmpty() &&
                (themePaletteMapEntry.applyPostProcesorsToNormalRooms || ModConfigs.THEME_PALETTE_REGISTRY.EXCLUDED_ROOM_POOLS.stream().anyMatch(s -> key.getPath().contains(s))) &&
                !(VaultGenUtils.isInscriptionRoom(vault, regionPos) && !themePaletteMapEntry.affectsInscriptionRooms);

        List<ResourceLocation> combinedPostgen = new ArrayList<>();
        if (canApplyThemePost) {
            combinedPostgen.addAll(themePaletteMapEntry.POST_PROCESSING_PALETTES);
        }

        boolean canApplyModifierPostgen = !(VaultGenUtils.isInscriptionRoom(vault, regionPos) && (themePaletteMapEntry != null && !themePaletteMapEntry.affectsInscriptionRooms));
        if (canApplyModifierPostgen) {
            combinedPostgen.addAll(modifierPostgen);
        }

        if (!combinedPostgen.isEmpty()) {
            for (ResourceLocation paletteId : combinedPostgen) {
                PaletteKey paletteKey = VaultRegistry.PALETTE.getKey(paletteId);
                if (paletteKey == null) {
                    continue;
                }

                Palette palette = paletteKey.get(Version.latest());
                palette.getTileProcessors().forEach(instance::addProcessor);
            }
        }

        return original.call(instance, processors);
    }

    private record CachedPalettes(Set<ResourceLocation> pregen, Set<ResourceLocation> postgen) {}
}