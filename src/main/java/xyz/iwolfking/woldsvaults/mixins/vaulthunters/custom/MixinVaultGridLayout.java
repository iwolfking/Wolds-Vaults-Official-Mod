package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.PaletteKey;
import iskallia.vault.core.data.key.TemplateKey;
import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.data.tile.PartialBlockState;
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
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.util.VaultGenUtils;
import xyz.iwolfking.woldsvaults.config.ThemePaletteRegistryConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = VaultGridLayout.class, remap = false)
public abstract class MixinVaultGridLayout {

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

        //Special Room exceptions
        if(key.getPath().contains("labyrinth")) {
            return original.call(instance, processors);
        }

        ResourceLocation themeId = vault.get(Vault.WORLD).get(WorldManager.THEME);
        if (themeId == null) {
            return original.call(instance, processors);
        }

        ThemePaletteRegistryConfig.ThemePaletteMapEntry themePaletteMapEntry = ModConfigs.THEME_PALETTE_REGISTRY.THEME_TO_PALETTE_MAP.getOrDefault(themeId, null);
        if (themePaletteMapEntry == null) {
            return original.call(instance, processors);
        }

        //Main Theme Palette Application
        if(!themePaletteMapEntry.FULL_PALETTE_ENTRIES.isEmpty() && ModConfigs.THEME_PALETTE_REGISTRY.EXCLUDED_ROOM_POOLS.stream().noneMatch(s -> key.getPath().contains(s))) {
            if(VaultGenUtils.isInscriptionRoom(vault, regionPos) && !themePaletteMapEntry.affectsInscriptionRooms) {
                return original.call(instance, processors);
            }

            for(ResourceLocation paletteId : themePaletteMapEntry.FULL_PALETTE_ENTRIES) {
                PaletteKey paletteKey = VaultRegistry.PALETTE.getKey(paletteId);
                if(paletteKey == null) {
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
                            else if(key.toString().contains("aquarium") && tileProcessor instanceof WeightedTileProcessor weightedTileProcessor && weightedTileProcessor.getPredicate().test(PartialTile.of(Blocks.WATER.defaultBlockState()))) {
                                return false;
                            }
                            else if((key.toString().contains("cube") || key.toString().contains("puzzle")) && tileProcessor instanceof WeightedTileProcessor weightedTileProcessor && weightedTileProcessor.getPredicate().test(PartialTile.of(Blocks.LIME_WOOL.defaultBlockState()))) {
                                return false;
                            }

                            return true;
                        })
                        .toList();
                filteredThemeProcessors.forEach(instance::addProcessorAtBeginning);
            }
        }

        //Palettes that should apply at the end of the processor list
        if(!themePaletteMapEntry.POST_PROCESSING_PALETTES.isEmpty()) {
            if(!themePaletteMapEntry.applyPostProcesorsToNormalRooms && ModConfigs.THEME_PALETTE_REGISTRY.EXCLUDED_ROOM_POOLS.stream().noneMatch(s -> key.getPath().contains(s))) {
                return original.call(instance, processors);
            }

            if(VaultGenUtils.isInscriptionRoom(vault, regionPos) && !themePaletteMapEntry.affectsInscriptionRooms) {
                return original.call(instance, processors);
            }

            for(ResourceLocation paletteId :  themePaletteMapEntry.POST_PROCESSING_PALETTES) {
                PaletteKey paletteKey = VaultRegistry.PALETTE.getKey(paletteId);
                if(paletteKey == null) {
                    continue;
                }

                Palette palette = paletteKey.get(Version.latest());

                palette.getTileProcessors().forEach(instance::addProcessor);
            }

        }

        return original.call(instance, processors);
    }
}