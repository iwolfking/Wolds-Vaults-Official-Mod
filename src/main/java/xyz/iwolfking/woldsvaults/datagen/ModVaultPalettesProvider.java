package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import xyz.iwolfking.vhapi.api.datagen.gen.AbstractPaletteProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.gen.palette.PaletteBuilder;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.*;
import java.util.function.Consumer;

public class ModVaultPalettesProvider extends AbstractPaletteProvider {
    public ModVaultPalettesProvider(DataGenerator generator, String modid) {
        super(generator, modid);
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
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(new ResourceLocation("rechiseled", "obsidian_dark"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_CARPET, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SLAB, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(new ResourceLocation("regions_unexplored", "black_painted_slab"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_SECONDARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(new ResourceLocation("davebuildingmod", "vanta_black"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TERTIRARY, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(new ResourceLocation("regions_unexplored", "sculk_grass_block"), 1);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_DECORATION, resourceLocationIntegerMap -> {
                        resourceLocationIntegerMap.put(new ResourceLocation("minecraft", "wither_rose"), 4);
                        resourceLocationIntegerMap.put(new ResourceLocation("regions_unexplored", "dorcel"), 2);
                        resourceLocationIntegerMap.put(Blocks.AIR.getRegistryName(), 10);
                    })
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VARIANT, Blocks.OBSIDIAN.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR, new ResourceLocation("architects_palette", "onyx_pillar"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_ACCENT, new ResourceLocation("create", "cut_scorchia_wall"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_PILLAR_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR, Blocks.OBSIDIAN.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_VARIANT_PILLAR_ACCENT, Blocks.CRYING_OBSIDIAN.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_BLOCK, new ResourceLocation("regions_unexplored", "sculkwood_leaves"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_FENCE, new ResourceLocation("regions_unexplored", "sculkwood_log_dark"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_LIGHT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.POST_VARIANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.TUNNEL_SLAB, new ResourceLocation("minecraft", "blackstone_slab"), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_LOWER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_TALL_DECORATION_UPPER, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_SECONDARY, Blocks.POLISHED_BLACKSTONE.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_ACCENT_TERTIARY, Blocks.POLISHED_BLACKSTONE_BRICKS.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_DECORATION, Blocks.GILDED_BLACKSTONE.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_PLANT, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.CEILING_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_VINES, Blocks.AIR.getRegistryName(), 1)
                    .replace(ThemePaletteBuilder.ThemeBlockType.FLOOR_PLANT, Blocks.AIR.getRegistryName(), 1);
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
