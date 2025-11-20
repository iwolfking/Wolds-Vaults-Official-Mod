package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.vhapi.api.lib.core.datagen.gen.AbstractPaletteProvider;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gen.palette.PaletteBuilder;
import xyz.iwolfking.vhapi.api.lib.core.datagen.lib.gen.palette.PaletteDefinition;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.*;
import java.util.function.Consumer;

public class ModVaultPalettesProvider extends AbstractPaletteProvider {
    public ModVaultPalettesProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    public static class ThemePaletteBuilder extends PaletteBuilder {

        Map<ThemeBlockType, Map<ResourceLocation, Integer>> themeBlocks = new HashMap<>();

        public ThemePaletteBuilder placeholder(ResourceLocation placeholderId) {
            this.reference(placeholderId);
            return this;
        }

        public ThemePaletteBuilder placeholder(Placeholder placeholder) {
            return placeholder(placeholder.getId());
        }

        public ThemePaletteBuilder replace(ThemeBlockType type, ResourceLocation blockId, int weight) {
            if(this.themeBlocks.containsKey(type)) {
                themeBlocks.get(type).put(blockId, weight);
            }
            else {
                Map<ResourceLocation, Integer> blockMap = new HashMap<>();
                blockMap.put(blockId, weight);
                this.themeBlocks.put(type, blockMap);
            }
            return this;
        }

        public ThemePaletteBuilder replace(ThemeBlockType type, Consumer<Map<ResourceLocation, Integer>> replacements) {
            Map<ResourceLocation, Integer> replacementBlocks = new HashMap<>();
            replacements.accept(replacementBlocks);
            if(this.themeBlocks.containsKey(type)) {
                replacementBlocks.forEach((res, weight) -> themeBlocks.get(type).put(res, weight));
            }
            else {
                this.themeBlocks.put(type, replacementBlocks);
            }

            return this;
        }

        public ThemePaletteBuilder themeCategory(String themeName, String themeCategory) {
            this.templateStackTile("@the_vault:vault_lootables", themeName, themeCategory);
            this.templateStackSpawner("ispawner:spawner", themeName, themeCategory);
            return this;
        }

        @Override
        public PaletteDefinition build() {
            for(ThemeBlockType themeBlockType : ThemeBlockType.values()) {
                if(themeBlocks.containsKey(themeBlockType)) {
                        this.weightedTarget(themeBlockType.toString(), weightedBuilder -> {
                            themeBlocks.get(themeBlockType).forEach(weightedBuilder::add);
                    });
                }
                else {
                    if(themeBlockType.altId == null) {
                        continue;
                    }

                    this.weightedTarget(themeBlockType.toString(), weightedBuilder -> {
                        weightedBuilder.add(themeBlockType.altId, 1);
                    });
                }
            }
            return super.build();
        }

        public record ReplacementBlock(ResourceLocation block, int weight) { }

        public enum ThemeBlockType {
            WALL_MAIN("minecraft:stone"),
            WALL_SECONDARY("minecraft:cobblestone"),
            WALL_TERTIARY("minecraft:gray_concrete"),
            WALL_FLOURISH("minecraft:purple_wool"),

            POI_BARS("minecraft:iron_bars"),
            POI_MAIN("minecraft:stone_bricks"),
            POI_MAIN_ALT("minecraft:mossy_stone_bricks"),
            POI_MAIN_ALT_SECONDARY("minecraft:mossy_cobblestone"),
            POI_ACCENT("minecraft:chiseled_stone_bricks"),
            POI_PILLAR("create:andesite_pillar"),
            POI_STAIRS("minecraft:stone_bricks_stairs"),
            POI_STAIRS_SECONDARY("minecraft:cobblestone_stairs"),
            POI_WALL("minecraft:stone_brick_wall"),
            POI_WALL_SECONDARY("minecraft:cobblestone_wall"),
            POI_SLAB("minecraft:stone_brick_slab"),
            POI_SLAB_SECONDARY("minecraft:cobblestone_slab"),
            POI_SLAB_TERTIARY("minecraft:stone_slab"),
            POI_LOG("minecraft:oak_log"),
            POI_WOOD("minecraft:oak_wood"),
            POI_LEAVES("minecraft:oak_leaves"),
            POI_PLANKS("minecraft:oak_planks"),
            POI_FENCE("minecraft:oak_fence"),
            POI_TRAPDOOR("minecraft:oak_trapdoor"),
            POI_STAIRS_WOOD("minecraft:oak_stairs"),
            POI_POT("minecraft:potted_oak_sapling"),
            POI_LANTERN("minecraft:lantern"),
            POI_CAMPFIRE("minecraft:campfire"),
            POI_BOOKSHELF("minecraft:bookshelf"),
            POI_FENCE_GATE("minecraft:oak_fence_gate"),
            POI_WALL_LANTERN("supplementaries:wall_lantern{Lantern:{Name:\\\"minecraft:lantern\\\"}}\""),
            POI_DOOR("minecraft:oak_door"),
            POI_VERTICAL_SLAB_BRICK("quark:stone_brick_vertical_slab"),
            POI_VERTICAL_SLAB("quark:stone_vertical_slab"),
            POI_SUPPORT("decorative_blocks:oak_support"),

            FLOOR("minecraft:dirt"),
            FLOOR_SLAB("minecraft:spruce_slab"),
            FLOOR_SECONDARY("minecraft:coarse_dirt"),
            FLOOR_TERTIRARY("minecraft:grass_block"),
            FLOOR_CARPET("minecraft:moss_carpet"),
            FLOOR_DECORATION("minecraft:grass"),
            FLOOR_DECORATION_SECONDARY("minecraft:fern"),
            FLOOR_TALL_DECORATION_LOWER("minecraft:tall_grass[half=lower]"),
            FLOOR_TALL_DECORATION_UPPER("minecraft:tall_grass[half=upper]"),
            FLOOR_VINES("minecraft:twisting_vines"),
            FLOOR_PLANT("minecraft:twisting_vines_plant"),

            CEILING_PLANT("minecraft:cave_vines_plant"),
            CEILING_VINES("minecraft:cave_vines"),
            CEILING_HANGING_ACCENT("minecraft:birch_fence"),
            CEILING_DECORATION("minecraft:spore_blossom"),
            CEILING_ACCENT("minecraft:pink_wool"),
            CEILING_ACCENT_SECONDARY("minecraft:lime_wool"),
            CEILING_ACCENT_TERTIARY("minecraft:brown_wool"),

            TUNNEL_LANTERN("minecraft:soul_lantern"),
            TUNNEL_PILLAR("quark:azalea_log"),
            TUNNEL_PILLAR_SECONDARY("quark:azalea_planks"),
            TUNNEL_PILLAR_ACCENT("create:cut_veridium_wall"),
            TUNNEL_PILLAR_STAIRS("quark:azalea_planks_stairs"),

            TUNNEL_VARIANT_PILLAR("ecologics:azalea_log"),
            TUNNEL_VARIANT_PILLAR_ACCENT("create:cut_crimsite_wall"),



            GOD_ALTAR_ACCENT("minecraft:end_stone"),
            GOD_ALTAR_MAIN("minecraft:end_stone_bricks"),
            GOD_ALTAR_STAIRS("minecraft:end_stone_bricks_stairs"),
            GOD_ALTAR_SLAB("minecraft:end_stone_brick_slab"),

            POST_FENCE("minecraft:warped_fence"),
            POST_BLOCK("minecraft:warped_fence"),
            POST_LIGHT("minecraft:shroomlight"),

            BRIDGE_SLAB("minecraft:oak_slab"),

            DECORATION_BRAZIER("decorative_blocks:brazier"),


            LAVA("minecraft:lava"),
            WATER("minecraft:water"),

            STARTING_ROOM_POOL_TOP_LAYER("minecraft:cyan_stained_glass", "minecraft:water"),
            STARTING_ROOM_POOL_BOTTOM_LAYER("minecraft:light_blue_stained_glass", "minecraft:water"),

            MAGMA("minecraft:magma_block"),

            LIGHT("minecraft:glass", "minecraft:light[level=15]"),


            //Misc
            CANDLE("minecraft:candle"),
            CHAIN("minecraft:chain"),
            COBWEB("minecraft:cobweb"),
            RAIL("minecraft:rail"),
            GRINDSTONE("minecraft:grindstone"),
            SAND("minecraft:sand"),

            //Unknown purpose
            NATURE("minecraft:green_concrete"),
            TWISTED_FENCE("architects_palette:twisted_fence"),
            FENCE_WOOD_SECONDARY("ecologics:azalea_fence"),
            HEDGE("quark:oak_hedge");

            private final String id;
            private String altId;

            ThemeBlockType(String s) {
                this.id = s;
                this.altId = null;
            }

            ThemeBlockType(String s, String alt) {
                this.id = s;
                this.altId = alt;
            }

            @Override
            public String toString() {
                return this.id;
            }
        }

        public enum Placeholder {
            ORE_PLACEHOLDER_VOID(VaultMod.id("generic/ore_placeholder_void")),
            TREASURE_DOOR(VaultMod.id("generic/treasure_door_placeholder")),
            COMMON_ELITE_SPAWNERS(VaultMod.id("generic/common_elite_spawners")),
            ROOM_BASE(VaultMod.id("generic/room_base"));

            private final ResourceLocation id;

            Placeholder(ResourceLocation placeholderId) {
                this.id = placeholderId;
            }

            public ResourceLocation getId() {
                return id;
            }

        }
    }

    @Override
    protected void registerPalettes() {
        add(WoldsVaults.id("universal_eclipse"), new ThemePaletteBuilder(), tb -> {
            tb.placeholder(ThemePaletteBuilder.Placeholder.ORE_PLACEHOLDER_VOID)
                    .placeholder(ThemePaletteBuilder.Placeholder.TREASURE_DOOR)
                    .placeholder(ThemePaletteBuilder.Placeholder.ROOM_BASE)
                    .placeholder(ThemePaletteBuilder.Placeholder.COMMON_ELITE_SPAWNERS)
                    .placeholder(VaultMod.id("generic/spawners/beach_mobs"))
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_MAIN, replacementBlocks -> {
                        replacementBlocks.put(Blocks.BLACKSTONE.getRegistryName(), 3);
                        replacementBlocks.put(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_SECONDARY, new ResourceLocation("blackstone_bricks"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_TERTIARY, new ResourceLocation("black_stained_glass"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.WALL_FLOURISH, new ResourceLocation("black_wool"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, Blocks.POLISHED_BLACKSTONE.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, Blocks.POLISHED_BLACKSTONE_BRICKS.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, Blocks.GILDED_BLACKSTONE.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1);
        });

        add(WoldsVaults.id("spawners_group_standard"), new PaletteBuilder(), p -> {

            p.leveled(leveledBuilder -> {
                leveledBuilder.list(0, "weighted_target", "ispawner:spawner", entries -> {
                    entries.put("ispawner:spawner{group: horde}", 50);
                    entries.put("ispawner:spawner{group: assassin}", 33);
                    entries.put("ispawner:spawner{group: tank}", 12);
                    entries.put("ispawner:spawner{group: dwellers}", 5);
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
}
