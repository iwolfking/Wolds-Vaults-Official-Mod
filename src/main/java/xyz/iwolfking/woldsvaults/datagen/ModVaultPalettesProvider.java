package xyz.iwolfking.woldsvaults.datagen;

import appeng.core.definitions.AEBlocks;
import com.cursedcauldron.wildbackport.common.registry.WBBlocks;
import iskallia.auxiliaryblocks.AuxiliaryBlocks;
import iskallia.auxiliaryblocks.init.ModBlocks;
import iskallia.vault.VaultMod;
import iskallia.vault.block.PlaceholderBlock;
import net.mcreator.buildingmod.init.DavebuildingmodModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.regions_unexplored.block.RegionsUnexploredBlocks;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractPaletteProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.gen.palette.PaletteBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import java.util.*;

public class ModVaultPalettesProvider extends AbstractPaletteProvider {
    public ModVaultPalettesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    protected void registerPalettes() {
        add(WoldsVaults.id("universal_mystical_forest"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(WoldsVaults.id("generic/ore_placeholder_magic"))
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(WoldsVaults.id("generic/spawners/ars_mobs"))

                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(ModBlocks.DARK_PURPLE_DIORITE.get().getRegistryName(), 6);
                        replacementBlocks.put(ResourceLocation.parse("ars_nouveau:stripped_purple_archwood_wood"), 4);
                        replacementBlocks.put(ResourceLocation.parse("ars_nouveau:stripped_purple_archwood_log"), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, ResourceLocation.parse("ars_nouveau:blue_archwood_log"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, ResourceLocation.parse("chipped:mossy_cobblestone_1"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, ResourceLocation.parse("ars_nouveau:purple_archwood_wood"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:moss_block"), 5);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:dirt_1"), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("ars_nouveau:archwood_slab"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:coarse_dirt"), 6);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_1"), 4);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:podzol"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:moss_carpet"), 8);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.PEONY.getRegistryName(), 4);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("ars_nouveau:magebloom_crop"), 2);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 4);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:flowering_azalea"), 3);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 7);
                    })

                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("ars_nouveau:blue_archwood_leaves"), 6);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("ars_nouveau:purple_archwood_leaves"), 4);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_HANGING_ACCENT, ResourceLocation.parse("ars_nouveau:archwood_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ModBlocks.PURPLE_DIORITE.get().getRegistryName(), 1);
                        resourceLocationIntegerMap.put(ModBlocks.DARK_PURPLE_DIORITE.get().getRegistryName(), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, ResourceLocation.parse("ars_nouveau:purple_archwood_wood"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, ResourceLocation.parse("minecraft:spore_blossom"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, ResourceLocation.parse("minecraft:cave_vines_plant"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, ResourceLocation.parse("minecraft:cave_vines"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, ResourceLocation.parse("ars_nouveau:stripped_blue_archwood_log"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_SECONDARY, ResourceLocation.parse("ars_nouveau:archwood_planks"), 1)
                    // Fixed: arcane_stone_mosaic -> ab_mosaic
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, ResourceLocation.parse("ars_nouveau:ab_mosaic"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, ResourceLocation.parse("ars_nouveau:archwood_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, ResourceLocation.parse("regions_unexplored:mauve_log"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, ResourceLocation.parse("regions_unexplored:mauve_planks"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, ResourceLocation.parse("ars_nouveau:blue_archwood_wood"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, ResourceLocation.parse("ars_nouveau:archwood_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, ResourceLocation.parse("ars_nouveau:blue_archwood_leaves"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, ResourceLocation.parse("regions_unexplored:mauve_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FENCE_WOOD_SECONDARY, ResourceLocation.parse("regions_unexplored:mauve_fence"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN, ResourceLocation.parse("ars_nouveau:archwood_planks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT, ResourceLocation.parse("ars_nouveau:archwood_planks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT_SECONDARY, ResourceLocation.parse("ars_nouveau:ab_mosaic"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_ACCENT, ResourceLocation.parse("ars_nouveau:sas_mosaic"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PILLAR, ResourceLocation.parse("ars_nouveau:stripped_blue_archwood_log"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STONE_STAIRS, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_SECONDARY, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL, ResourceLocation.parse("ars_nouveau:archwood_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_SECONDARY, ResourceLocation.parse("ars_nouveau:archwood_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_TERTIARY, ResourceLocation.parse("chipped:mossy_cobblestone_wall_1"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB, ResourceLocation.parse("ars_nouveau:ab_smooth_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB_TERTIARY, ResourceLocation.parse("ars_nouveau:ab_smooth_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LOG, ResourceLocation.parse("ars_nouveau:blue_archwood_log"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WOOD, ResourceLocation.parse("ars_nouveau:blue_archwood_wood"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PLANKS, ResourceLocation.parse("ars_nouveau:archwood_planks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LEAVES, ResourceLocation.parse("ars_nouveau:blue_archwood_leaves"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE, ResourceLocation.parse("ars_nouveau:archwood_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE_GATE, ResourceLocation.parse("ars_nouveau:archwood_fence_gate"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_TRAPDOOR, ResourceLocation.parse("ars_nouveau:archwood_trapdoor"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_DOOR, ResourceLocation.parse("ars_nouveau:archwood_door"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_WOOD, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LANTERN, ResourceLocation.parse("minecraft:soul_lantern"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CAMPFIRE, ResourceLocation.parse("minecraft:soul_campfire"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_BOOKSHELF, ResourceLocation.parse("everycomp:q/regions_unexplored/mauve_bookshelf"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_MAIN, ResourceLocation.parse("ars_nouveau:arcane_stone"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_ACCENT, ResourceLocation.parse("ars_nouveau:sas_mosaic"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_SLAB, ResourceLocation.parse("ars_nouveau:ab_smooth_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_STAIRS, ResourceLocation.parse("ars_nouveau:archwood_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.DECORATION_BRAZIER, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("ars_nouveau:arcane_pedestal"), 2);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.BRIDGE_SLAB, ResourceLocation.parse("ars_nouveau:archwood_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CHAIN, ResourceLocation.parse("minecraft:chain"), 1)

                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_BOTTOM_LAYER, ModBlocks.PURPLE_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_TOP_LAYER, ModBlocks.PURPLE_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WATER, ModBlocks.PURPLE_WATER.getId(), 1);
        });

        add(WoldsVaults.id("universal_astral"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(WoldsVaults.id("generic/ore_placeholder_astral"))
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(WoldsVaults.id("generic/spawners/astral_mobs"))
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(DavebuildingmodModBlocks.STARS.getId(), 3);
                        replacementBlocks.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 7);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, ResourceLocation.parse("auxiliaryblocks:gloomy_dust"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, DavebuildingmodModBlocks.STARS.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, ResourceLocation.parse("quark:black_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel_slab"), 8);
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.GRAVEL_SLAB.getId(), 2);

                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.STARS.getId(), 7);
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 3);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_SECONDARY, ResourceLocation.parse("architects_palette:moonstone"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, ResourceLocation.parse("auxiliaryblocks:gloomy_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, ResourceLocation.parse("auxiliaryblocks:gloomy_crushed_rocks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, Blocks.DEAD_HORN_CORAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, ResourceLocation.parse("chipped:spruce_leaves_11"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, AEBlocks.SMOOTH_SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_LOWER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_UPPER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, ResourceLocation.parse("quark:black_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_HANGING_ACCENT, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_SOLID_ACCENT, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FENCE_WOOD_SECONDARY, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN, AEBlocks.SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT, AEBlocks.SKY_STONE_BRICK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT_SECONDARY, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_ACCENT, ResourceLocation.parse("chipped:gray_concrete_18"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PILLAR, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS, AEBlocks.SKY_STONE_STAIRS.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE_GATE, ResourceLocation.parse("integrateddynamics:menril_fence_gate"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LEAVES, ResourceLocation.parse("chipped:spruce_leaves_11"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB, AEBlocks.SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB_TERTIARY, AEBlocks.SMOOTH_SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB, ResourceLocation.parse("quark:shale_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB_BRICK, ResourceLocation.parse("quark:shale_bricks_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL, AEBlocks.SKY_STONE_WALL.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_SECONDARY, ResourceLocation.parse("quark:shale_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_TERTIARY, ResourceLocation.parse("quark:shale_bricks_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LOG, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PLANKS, ResourceLocation.parse("quark:shale"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_WOOD, ResourceLocation.parse("quark:shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STONE_STAIRS, ResourceLocation.parse("quark:polished_shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_SECONDARY, ResourceLocation.parse("quark:shale_bricks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_TRAPDOOR, DavebuildingmodModBlocks.STEEL_TRAPDOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WOOD, ResourceLocation.parse("quark:indigo_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SUPPORT, ResourceLocation.parse("everycomp:db/regions_unexplored/dead_support"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_DOOR, DavebuildingmodModBlocks.STEEL_DOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CAMPFIRE, ResourceLocation.parse("minecraft:soul_campfire"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_BOOKSHELF, ResourceLocation.parse("everycomp:q/the_vault/driftwood_bookshelf"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CONCRETE, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CHAIN, ResourceLocation.parse("buildscape:large_ancient_steel_chain"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.BRIDGE_SLAB, ResourceLocation.parse("buildscape:steel_block_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_MAIN, ResourceLocation.parse("quark:shale_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_ACCENT, ResourceLocation.parse("quark:chiseled_shale_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_SLAB, ResourceLocation.parse("quark:polished_shale_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_STAIRS, ResourceLocation.parse("quark:polished_shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.DECORATION_BRAZIER, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.ARCANE_DUCK.getId(), 1);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 9);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_BOTTOM_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_TOP_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WATER, ModBlocks.GRAY_WATER.getId(), 1);
        });

        add(WoldsVaults.id("universal_sculk"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(ThemePaletteBuilder.Placeholder.ORE_PLACEHOLDER_VOID)
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(VaultMod.id("generic/spawners/void_mobs"))
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(RegionsUnexploredBlocks.SCULKWOOD_PLANKS.getId(), 3);
                        replacementBlocks.put(RegionsUnexploredBlocks.SCULKWOOD_LOG_DARK.getId(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, RegionsUnexploredBlocks.SCULKWOOD_LEAVES.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, RegionsUnexploredBlocks.SCULKWOOD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, WBBlocks.SCULK.get().getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_GRASS_BLOCK.getId(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULKWOOD_SLAB.getId(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_GRASS_BLOCK.getId(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_GRASS_BLOCK.getId(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_TENDRIL.getId(), 4);
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_SPROUT.getId(), 4);
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULKWOOD_SAPLING.getId(), 2);
                        resourceLocationIntegerMap.put(Blocks.WITHER_ROSE.getRegistryName(), 1);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 12);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(RegionsUnexploredBlocks.SCULK_TENDRIL.getId(), 3);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 3);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, WBBlocks.SCULK.get().getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, RegionsUnexploredBlocks.SCULKWOOD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_SECONDARY, WBBlocks.SCULK.get().getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, RegionsUnexploredBlocks.SCULKWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, RegionsUnexploredBlocks.SCULKWOOD_STAIRS.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, RegionsUnexploredBlocks.SCULKWOOD_LOG_DARK.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, WBBlocks.SCULK.get().getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, RegionsUnexploredBlocks.SCULKWOOD_LEAVES.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, RegionsUnexploredBlocks.SCULKWOOD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, RegionsUnexploredBlocks.SCULKWOOD_SLAB.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_LOWER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_UPPER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, WBBlocks.SCULK.get().getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, Blocks.POLISHED_BLACKSTONE_BRICKS.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, RegionsUnexploredBlocks.SCULKWOOD_LEAVES.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_HANGING_ACCENT, RegionsUnexploredBlocks.SCULKWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_SOLID_ACCENT, RegionsUnexploredBlocks.SCULKWOOD_LEAVES.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FENCE_WOOD_SECONDARY, RegionsUnexploredBlocks.SCULKWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN, ResourceLocation.parse("chipped:warped_planks_23"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT, ResourceLocation.parse("chipped:warped_planks_14"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT_SECONDARY, ResourceLocation.parse("chipped:warped_planks_37"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_ACCENT, ResourceLocation.parse("chipped:warped_planks_6"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PILLAR, ResourceLocation.parse("rechiseled:warped_planks_bricks_connecting"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS, ResourceLocation.parse("architects_palette:warped_board_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE, ResourceLocation.parse("minecraft:warped_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE_GATE, ResourceLocation.parse("minecraft:warped_fence_gate"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LEAVES, RegionsUnexploredBlocks.SCULKWOOD_LEAVES.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB, ResourceLocation.parse("architects_palette:warped_board_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB_TERTIARY, ResourceLocation.parse("architects_palette:warped_board_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB, ResourceLocation.parse("architects_palette:warped_board_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB_BRICK, ResourceLocation.parse("architects_palette:warped_board_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL, ResourceLocation.parse("architects_palette:warped_board_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_SECONDARY, ResourceLocation.parse("architects_palette:warped_board_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_TERTIARY, ResourceLocation.parse("architects_palette:warped_board_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LOG, ResourceLocation.parse("minecraft:stripped_warped_stem"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PLANKS, ResourceLocation.parse("minecraft:warped_planks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_WOOD, ResourceLocation.parse("minecraft:warped_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STONE_STAIRS, ResourceLocation.parse("minecraft:warped_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_SECONDARY, ResourceLocation.parse("minecraft:warped_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_TRAPDOOR, ResourceLocation.parse("minecraft:warped_trapdoor"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WOOD, ResourceLocation.parse("minecraft:warped_hyphae"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SUPPORT, ResourceLocation.parse("decorative_blocks:warped_support"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_DOOR, ResourceLocation.parse("chipped:warped_door_17"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CAMPFIRE, ResourceLocation.parse("minecraft:soul_campfire"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_BOOKSHELF, ResourceLocation.parse("quark:warped_bookshelf"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CONCRETE, ResourceLocation.parse("minecraft:stripped_warped_stem"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CHAIN, ResourceLocation.parse("buildscape:large_ancient_steel_chain"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.BRIDGE_SLAB, ResourceLocation.parse("minecraft:warped_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_MAIN, ResourceLocation.parse("rechiseled:warped_planks_small_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_ACCENT, ResourceLocation.parse("rechiseled:warped_planks_small_tiles"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_SLAB, ResourceLocation.parse("architects_palette:warped_board_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_STAIRS, ResourceLocation.parse("architects_palette:warped_board_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.DECORATION_BRAZIER, Blocks.SCULK_SENSOR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_BOTTOM_LAYER, ModBlocks.CYAN_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_TOP_LAYER, ModBlocks.CYAN_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WATER, ModBlocks.CYAN_WATER.getId(), 1);
        });

        add(WoldsVaults.id("universal_astral"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(WoldsVaults.id("generic/ore_placeholder_astral"))
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(WoldsVaults.id("generic/spawners/astral_mobs"))
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(DavebuildingmodModBlocks.STARS.getId(), 3);
                        replacementBlocks.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 7);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, ResourceLocation.parse("auxiliaryblocks:gloomy_dust"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, DavebuildingmodModBlocks.STARS.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, ResourceLocation.parse("quark:black_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel_slab"), 8);
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.GRAVEL_SLAB.getId(), 2);

                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:gravel_3"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("auxiliaryblocks:gloomy_gravel"), 8);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.STARS.getId(), 7);
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 3);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_SECONDARY, ResourceLocation.parse("architects_palette:moonstone"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, ResourceLocation.parse("auxiliaryblocks:gloomy_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, ResourceLocation.parse("auxiliaryblocks:gloomy_crushed_rocks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, Blocks.DEAD_HORN_CORAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, ResourceLocation.parse("chipped:spruce_leaves_11"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, AEBlocks.SMOOTH_SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_LOWER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_UPPER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, ResourceLocation.parse("quark:black_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_HANGING_ACCENT, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_SOLID_ACCENT, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FENCE_WOOD_SECONDARY, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN, AEBlocks.SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT, AEBlocks.SKY_STONE_BRICK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT_SECONDARY, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_ACCENT, ResourceLocation.parse("chipped:gray_concrete_18"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PILLAR, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS, AEBlocks.SKY_STONE_STAIRS.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE, ResourceLocation.parse("integrateddynamics:menril_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE_GATE, ResourceLocation.parse("integrateddynamics:menril_fence_gate"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LEAVES, ResourceLocation.parse("chipped:spruce_leaves_11"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB, AEBlocks.SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB_TERTIARY, AEBlocks.SMOOTH_SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB, ResourceLocation.parse("quark:shale_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB_BRICK, ResourceLocation.parse("quark:shale_bricks_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL, AEBlocks.SKY_STONE_WALL.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_SECONDARY, ResourceLocation.parse("quark:shale_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_TERTIARY, ResourceLocation.parse("quark:shale_bricks_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LOG, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PLANKS, ResourceLocation.parse("quark:shale"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_WOOD, ResourceLocation.parse("quark:shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STONE_STAIRS, ResourceLocation.parse("quark:polished_shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_SECONDARY, ResourceLocation.parse("quark:shale_bricks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_TRAPDOOR, DavebuildingmodModBlocks.STEEL_TRAPDOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WOOD, ResourceLocation.parse("quark:indigo_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SUPPORT, ResourceLocation.parse("everycomp:db/regions_unexplored/dead_support"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_DOOR, DavebuildingmodModBlocks.STEEL_DOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CAMPFIRE, ResourceLocation.parse("minecraft:soul_campfire"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_BOOKSHELF, ResourceLocation.parse("everycomp:q/the_vault/driftwood_bookshelf"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CONCRETE, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CHAIN, ResourceLocation.parse("buildscape:large_ancient_steel_chain"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.BRIDGE_SLAB, ResourceLocation.parse("buildscape:steel_block_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_MAIN, ResourceLocation.parse("quark:shale_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_ACCENT, ResourceLocation.parse("quark:chiseled_shale_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_SLAB, ResourceLocation.parse("quark:polished_shale_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_STAIRS, ResourceLocation.parse("quark:polished_shale_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.DECORATION_BRAZIER, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.ARCANE_DUCK.getId(), 1);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 9);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_BOTTOM_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_TOP_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WATER, ModBlocks.GRAY_WATER.getId(), 1);
        });

        add(WoldsVaults.id("universal_astral_red"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(WoldsVaults.id("generic/ore_placeholder_astral"))
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(WoldsVaults.id("generic/spawners/astral_mobs"))
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(DavebuildingmodModBlocks.STARS.getId(), 3);
                        replacementBlocks.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 7);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, ResourceLocation.parse("auxiliaryblocks:gloomy_dust"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, DavebuildingmodModBlocks.STARS.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, ResourceLocation.parse("quark:red_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:red_sand"), 8);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_57"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_61"), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:red_sandstone_slab"), 8);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:cut_red_sandstone_slab"), 2);

                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:red_sand"), 8);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_57"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_61"), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(ResourceLocation.parse("minecraft:red_sand"), 8);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_57"), 2);
                        resourceLocationIntegerMap.put(ResourceLocation.parse("chipped:red_sandstone_61"), 2);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.STARS.getId(), 7);
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.VANTA_BLACK.getId(), 3);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, AEBlocks.SMOOTH_SKY_STONE_BLOCK.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_SECONDARY, ResourceLocation.parse("architects_palette:moonstone"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, ResourceLocation.parse("auxiliaryblocks:gloomy_fence"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, ResourceLocation.parse("auxiliaryblocks:gloomy_crushed_rocks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, Blocks.DEAD_HORN_CORAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, ResourceLocation.parse("chipped:dark_oak_leaves_9"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, AEBlocks.SMOOTH_SKY_STONE_SLAB.id(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_LOWER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_UPPER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, ResourceLocation.parse("quark:red_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_HANGING_ACCENT, RegionsUnexploredBlocks.REDWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_SOLID_ACCENT, ResourceLocation.parse("quark:white_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FENCE_WOOD_SECONDARY, RegionsUnexploredBlocks.REDWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN, ResourceLocation.parse("quark:jasper"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT, ResourceLocation.parse("quark:jasper_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_MAIN_ALT_SECONDARY, ResourceLocation.parse("quark:polished_jasper"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_ACCENT, ResourceLocation.parse("quark:chiseled_jasper_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PILLAR, ResourceLocation.parse("quark:jasper_pillar"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS, ResourceLocation.parse("quark:jasper_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE, RegionsUnexploredBlocks.REDWOOD_FENCE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_FENCE_GATE, RegionsUnexploredBlocks.REDWOOD_FENCE_GATE.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LEAVES, ResourceLocation.parse("chipped:dark_oak_leaves_9"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB, ResourceLocation.parse("quark:jasper_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SLAB_TERTIARY, ResourceLocation.parse("quark:polished_jasper_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB, ResourceLocation.parse("quark:jasper_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_VERTICAL_SLAB_BRICK, ResourceLocation.parse("quark:jasper_bricks_vertical_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL, ResourceLocation.parse("quark:jasper_bricks_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_SECONDARY, ResourceLocation.parse("quark:jasper_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WALL_TERTIARY, ResourceLocation.parse("quark:jasper_bricks_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_LOG, RegionsUnexploredBlocks.DEAD_LOG.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_PLANKS, ResourceLocation.parse("quark:jasper"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_WOOD, ResourceLocation.parse("quark:jasper_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STONE_STAIRS, ResourceLocation.parse("quark:polished_jasper_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_STAIRS_SECONDARY, ResourceLocation.parse("quark:jasper_bricks_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_TRAPDOOR, DavebuildingmodModBlocks.RED_STEEL_TRAPDOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_WOOD, ResourceLocation.parse("quark:violet_corundum"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_SUPPORT, ResourceLocation.parse("everycomp:db/regions_unexplored/dead_support"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_DOOR, DavebuildingmodModBlocks.STEEL_DOOR.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CAMPFIRE, ResourceLocation.parse("minecraft:campfire"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_BOOKSHELF, ResourceLocation.parse("everycomp:q/the_vault/driftwood_bookshelf"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POI_CONCRETE, iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CHAIN, ResourceLocation.parse("buildscape:large_ancient_steel_chain"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.BRIDGE_SLAB, DavebuildingmodModBlocks.RED_STEEL_SLAB.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_MAIN, ResourceLocation.parse("chipped:granite_8"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_ACCENT, ResourceLocation.parse("chipped:granite_9"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_SLAB, ResourceLocation.parse("minecraft:polished_granite_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.GOD_ALTAR_STAIRS, ResourceLocation.parse("minecraft:polished_granite_stairs"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.DECORATION_BRAZIER, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(DavebuildingmodModBlocks.RUBBER_DUCK.getId(), 1);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 9);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_BOTTOM_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.STARTING_ROOM_POOL_TOP_LAYER, ModBlocks.GRAY_WATER.getId(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WATER, ModBlocks.GRAY_WATER.getId(), 1);
        });

        add(WoldsVaults.id("generic/ore_placeholder_astral"), new PaletteBuilder(), p -> {
            p.placeholder(PlaceholderBlock.Type.ORE, placeholderBuilder -> {
               placeholderBuilder.probability(0, 0.08, successes -> {
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 140);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 100);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 6);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 10);
                   successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 10);
               }, failures -> {
                failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
               });
                placeholderBuilder.probability(11, 0.1, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 160);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 100);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 10);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
                placeholderBuilder.probability(23, 0.1, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 180);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 100);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 10);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
                placeholderBuilder.probability(40, 0.12, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 100);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 40);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 120);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 10);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
            });
        });

        add(WoldsVaults.id("generic/ore_placeholder_magic"), new PaletteBuilder(), p -> {
            p.placeholder(PlaceholderBlock.Type.ORE, placeholderBuilder -> {
                placeholderBuilder.probability(0, 0.08, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 100);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 6);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 2);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 5);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
                placeholderBuilder.probability(11, 0.1, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 60);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 2);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 5);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
                placeholderBuilder.probability(23, 0.1, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 10);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 60);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 5);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
                placeholderBuilder.probability(40, 0.12, successes -> {
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PAINITE_ORE, "vault_stone"), 100);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ALEXANDRITE_ORE, "vault_stone"), 80);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.WUTODIE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BENITOITE_ORE, "vault_stone"), 200);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.LARIMAR_ORE, "vault_stone"), 300);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BLACK_OPAL_ORE, "vault_stone"), 25);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ECHO_ORE, "vault_stone"), 1);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ISKALLIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.GORGINITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.ASHIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.SPARKLETINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.BOMIGNITE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.TUBIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.UPALINE_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.XENIUM_ORE, "vault_stone"), 5);
                    successes.put(vaultOre(iskallia.vault.init.ModBlocks.PETZANITE_ORE, "vault_stone"), 5);
                }, failures -> {
                    failures.put(iskallia.vault.init.ModBlocks.VAULT_STONE.getRegistryName().toString(), 1);
                });
            });
        });

        add(WoldsVaults.id("generic/spawners/ars_mobs"), new PaletteBuilder(), p -> {

            p.leveled(leveledBuilder -> {
                leveledBuilder.list(0, "weighted_target", "ispawner:spawner", entries -> {
                    entries.put("ispawner:spawner{group: horde}", 50);
                    entries.put("ispawner:spawner{group: assassin}", 40);
                    entries.put("ispawner:spawner{group: tank}", 10);
                    entries.put("ispawner:spawner{group: dwellers}", 5);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:tank}", 1, entries -> {
                    entries.put("ars_nouveau:vexing_weald_walker", 2);
                    entries.put("ars_nouveau:cascading_weald_walker", 2);
                    entries.put("ars_nouveau:flourishing_weald_walker", 2);
                    entries.put("ars_nouveau:blazing_weald_walker", 2);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:horde}", 1, entries -> {
                    entries.put("ars_nouveau:wilden_stalker", 30);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:assassin}", 1, entries -> {
                    entries.put("ars_nouveau:wilden_hunter", 30);
                });
            });



            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter", 15);
                });
                leveledBuilder.weighted(20, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter", 15);
                    entries.put("the_vault:vault_fighter_2", 15);
                });
                leveledBuilder.weighted(40, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_1", 15);
                    entries.put("the_vault:vault_fighter_2", 15);
                });
                leveledBuilder.weighted(60, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_2", 15);
                    entries.put("the_vault:vault_fighter_3", 15);
                });
                leveledBuilder.weighted(80, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_3", 15);
                    entries.put("the_vault:vault_fighter_4", 15);
                });
                leveledBuilder.weighted(100, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_4", 15);
                });
            });

            p.reference("the_vault:generic/spawners/group_settings");
        });


        add(WoldsVaults.id("generic/spawners/astral_mobs"), new PaletteBuilder(), p -> {

            p.leveled(leveledBuilder -> {
                leveledBuilder.list(0, "weighted_target", "ispawner:spawner", entries -> {
                    entries.put("ispawner:spawner{group: horde}", 50);
                    entries.put("ispawner:spawner{group: assassin}", 30);
                    entries.put("ispawner:spawner{group: tank}", 15);
                    entries.put("ispawner:spawner{group: dwellers}", 5);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:tank}", 1, entries -> {
                   entries.put("woldsvaults:star_devourer", 0);
                   entries.put("woldsvaults:nebula_sentinel", 2);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:horde}", 1, entries -> {
                    entries.put("woldsvaults:loginar", 30);
                });
            });

            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:assassin}", 1, entries -> {
                    entries.put("woldsvaults:singularity_creeper", 10);
                    entries.put("woldsvaults:astral_stalker", 15);
                    entries.put("alexsmobs:cosmaw", 5);
                });
            });



            p.leveled(leveledBuilder -> {
                leveledBuilder.weighted(0, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter", 15);
                });
                leveledBuilder.weighted(20, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter", 15);
                    entries.put("the_vault:vault_fighter_2", 15);
                });
                leveledBuilder.weighted(40, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_1", 15);
                    entries.put("the_vault:vault_fighter_2", 15);
                });
                leveledBuilder.weighted(60, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_2", 15);
                    entries.put("the_vault:vault_fighter_3", 15);
                });
                leveledBuilder.weighted(80, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_3", 15);
                    entries.put("the_vault:vault_fighter_4", 15);
                });
                leveledBuilder.weighted(100, "spawner", "ispawner:spawner{group:dwellers}", 1, entries -> {
                    entries.put("the_vault:vault_fighter_4", 15);
                });
            });

            p.reference("the_vault:generic/spawners/group_settings");
        });

    }

    public static String vaultOre(ItemLike oreBlock, String type) {
        return oreBlock.asItem().getRegistryName().toString() + "[type=" + type + ",generated=true]";
    }
}
