package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.item.VaultModifierItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import xyz.iwolfking.vhapi.api.lib.core.datagen.gen.AbstractPaletteProvider;
import xyz.iwolfking.vhapi.api.registry.gen.palette.PaletteProcessors;
import xyz.iwolfking.vhapi.api.registry.gen.template_pools.TemplatePoolProcessors;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModVaultPalettesProvider extends AbstractPaletteProvider {
    public ModVaultPalettesProvider(DataGenerator generator, String modid) {
        super(generator, modid);
    }

    @Override
    protected void registerPalettes() {
        add(new ResourceLocation("woldsvaults", "universal_eclipse"), b -> {
            b.reference(VaultMod.id("generic/ore_placeholder_void"))
                    .reference(VaultMod.id("generic/treasure_door_placeholder"));

            b.weightedTarget("minecraft:stone", w -> {
                w.add("minecraft:obsidian", 1)
                        .add("minecraft:black_concrete", 1)
                        .add("minecraft:blackstone", 1);
            });

            b.weightedTarget("minecraft:cobblestone", w -> {
                w.add("minecraft:cobbled_deepslate", 1);
            });

            b.weightedTarget("minecraft:gray_concrete", w -> {
                w.add("minecraft:deepslate", 1);
            });

            b.weightedTarget("minecraft:coarse_dirt", w -> {
                w.add("minecraft:coarse_dirt", 1);
            });

            b.weightedTarget("minecraft:dirt", w -> {
                w.add("minecraft:dirt", 1);
            });

            b.weightedTarget("minecraft:grass_block", w -> {
                w.add("minecraft:grass_block", 1);
            });

            b.weightedTarget("minecraft:stone_slab", w -> {
                w.add("minecraft:sandstone_slab", 1);
            });

            b.weightedTarget("minecraft:cobblestone_slab", w -> {
                w.add("minecraft:sandstone_slab", 1);
            });

            b.weightedTarget("minecraft:spruce_slab", w -> {
                w.add("minecraft:spruce_slab", 1);
            });

            b.weightedTarget("minecraft:purple_wool", w -> {
                w.add("minecraft:stripped_spruce_wood", 1);
            });

            b.weightedTarget("minecraft:water", w -> {
                w.add("minecraft:water", 1);
            });

            b.weightedTarget("minecraft:birch_fence", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:warped_fence", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:warped_wart_block", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:shroomlight", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("architects_palette:twisted_fence", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:cave_vines_plant", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:cave_vines", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:spore_blossom", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:brown_wool", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:pink_wool", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:grass", w -> {
                w.add("minecraft:grass", 1);
            });

            b.weightedTarget("minecraft:tall_grass[half=lower]", w -> {
                w.add("minecraft:grass", 1);
            });

            b.weightedTarget("minecraft:tall_grass[half=upper]", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:twisting_vines_plant", w -> {
                w.add("minecraft:cactus", 1);
            });

            b.weightedTarget("minecraft:twisting_vines", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:fern", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:lime_wool", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_log", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_wood", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_leaves", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_planks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_fence", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_slab", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_trapdoor", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_stairs", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_fence_gate", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:oak_door", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:stone_bricks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:stone_brick_stairs", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("create:andesite_pillar", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:chiseled_stone_bricks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:stone_brick_slab", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:stone_brick_vertical_slab", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:stone_vertical_slab", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:stone_brick_wall", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:cobblestone_wall", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:cobblestone_stairs", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:mossy_stone_bricks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:mossy_cobblestone", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:magma_block", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:lava", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:green_concrete", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:moss_carpet", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("decorative_blocks:brazier", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:campfire", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:lantern", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("supplementaries:wall_lantern{Lantern:{Name:\"minecraft:lantern\"}}", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:soul_lantern", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:glass", w -> {
                w.add("minecraft:light[level=15]", 1);
            });

            b.weightedTarget("minecraft:cyan_stained_glass", w -> {
                w.add("minecraft:water", 1);
            });

            b.weightedTarget("minecraft:light_blue_stained_glass", w -> {
                w.add("minecraft:water", 1);
            });

            b.weightedTarget("minecraft:bookshelf", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("ecologics:azalea_log", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("ecologics:azalea_fence", w -> {
                w.add("minecraft:air", 1);
            });


            b.weightedTarget("create:cut_crimsite_wall", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:azalea_log", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:azalea_planks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:azalea_planks_stairs", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("create:cut_veridium_wall", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:sand", w -> {
                w.add("minecraft:sand", 1);
            });

            b.weightedTarget("minecraft:potted_oak_sapling", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("quark:oak_hedge", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("decorative_blocks:oak_support", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:end_stone_bricks", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:end_stone_bricks_stairs", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:end_stone_brick_slab", w -> {
                w.add("minecraft:air", 1);
            });

            b.weightedTarget("minecraft:end_stone", w -> {
                w.add("minecraft:air", 1);
            });

            b.reference(VaultMod.id("generic/room_base"));
            b.reference(VaultMod.id("generic/spawners/beach_mobs"));
            b.reference(VaultMod.id("generic/common_elite_spawners"));

            b.templateStackTile("@the_vault:vault_lootables", "THEME_AURORA_CAVE", "THEME_CATEGORY_ICE");
            b.templateStackSpawner("ispawner:spawner", "THEME_AURORA_CAVE", "THEME_CATEGORY_ICE");
        });

        add(WoldsVaults.id("spawners_group_standard"), p -> {

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
