package xyz.iwolfking.woldsvaults.datagen;

import me.dinnerbeef.compressium.Compressium;
import net.mcreator.buildingmod.init.DavebuildingmodModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, WoldsVaults.MOD_ID, existingFileHelper);
    }

    public static final TagKey<Block> WUTODIE_STORAGE_BLOCK = BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", "storage_blocks/wutodie"));

    @Override
    protected void addTags() {

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.GRAVEYARD_LOOT_BLOCK)
                .add(ModBlocks.AUGMENT_CRAFTING_TABLE)
                .add(ModBlocks.VAULT_INFUSER_BLOCK)
                .add(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK)
                .add(ModBlocks.VAULT_SALVAGER_BLOCK)
                .add(ModBlocks.NULLITE_ORE)
                .add(ModBlocks.WUTODIE_SLAB)
                .add(ModBlocks.WUTODIE_STAIRS)
                .add(ModBlocks.WUTODIE_WALL)
                .add(ModBlocks.WUTODIE)
                .add(ModBlocks.DOLL_DISMANTLING_BLOCK)
                .add(ModBlocks.CRATE_CRACKER_BLOCK)
                .add(ModBlocks.CHROMATIC_GOLD_BLOCK)
                .add(ModBlocks.SILVER_SCRAP_BLOCK)
                .add(ModBlocks.SILVER_SCRAP_BLOCK)
                .add(ModBlocks.VAULT_INGOT_BLOCK)
                .add(ModBlocks.VAULT_PLATING_BLOCK)
                .add(ModBlocks.CARBON_BLOCK)
                .add(ModBlocks.OMEGA_POG_BLOCK)
                .add(ModBlocks.ECHO_POG_BLOCK)
                .add(ModBlocks.POG_BLOCK)
                .add(ModBlocks.VAULT_PLATING_BLOCK)
                .add(ModBlocks.COLORED_UNOBTANIUMS.values().toArray(new Block[]{}))
                .add(ModBlocks.RAINBOW_UNOBTANIUM)
                .add(ModBlocks.PUZZLE_CUBE_BLOCK)
                .add(ModBlocks.TIME_TRIAL_TROPHY_BLOCK)
                .add(ModBlocks.TRINKET_FUSION_BLOCK)
                .add(iskallia.vault.init.ModBlocks.CRYSTAL_BLOCK)
                .add(DavebuildingmodModBlocks.VANTA_BLACK.get())
                .add(DavebuildingmodModBlocks.STARS.get())

                .add(ModBlocks.TENOS_BRICKS_SLAB)
                .add(ModBlocks.TENOS_BRICK_CHISELED_SLAB)
                .add(ModBlocks.TENOS_DARK_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.TENOS_LIGHT_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.TENOS_GEM_BLOCK_SLAB)
                .add(ModBlocks.TENOS_BRICKS_STAIRS)
                .add(ModBlocks.TENOS_BRICK_CHISELED_STAIRS)
                .add(ModBlocks.TENOS_DARK_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.TENOS_LIGHT_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.TENOS_GEM_BLOCK_STAIRS)

                .add(ModBlocks.VELARA_BRICK_CHISELED_SLAB)
                .add(ModBlocks.VELARA_DARK_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.VELARA_LIGHT_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.VELARA_BRICKS_STAIRS)
                .add(ModBlocks.VELARA_BRICK_CHISELED_STAIRS)
                .add(ModBlocks.VELARA_DARK_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.VELARA_LIGHT_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.VELARA_GEM_BLOCK_SLAB)
                .add(ModBlocks.VELARA_GEM_BLOCK_STAIRS)

                .add(ModBlocks.WENDARR_BRICKS_SLAB)
                .add(ModBlocks.WENDARR_BRICK_CHISELED_SLAB)
                .add(ModBlocks.WENDARR_DARK_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.WENDARR_LIGHT_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.WENDARR_BRICKS_STAIRS)
                .add(ModBlocks.WENDARR_BRICK_CHISELED_STAIRS)
                .add(ModBlocks.WENDARR_DARK_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.WENDARR_LIGHT_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.WENDARR_GEM_BLOCK_SLAB)
                .add(ModBlocks.WENDARR_GEM_BLOCK_STAIRS)
                .add(ModBlocks.WENDARR_JEWEL_BLOCK_SLAB)
                .add(ModBlocks.WENDARR_JEWEL_BLOCK_STAIRS)

                .add(ModBlocks.IDONA_BRICKS_SLAB)
                .add(ModBlocks.IDONA_BRICK_CHISELED_SLAB)
                .add(ModBlocks.IDONA_DARK_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.IDONA_LIGHT_SMOOTH_BRICKS_SLAB)
                .add(ModBlocks.IDONA_BRICKS_STAIRS)
                .add(ModBlocks.IDONA_BRICK_CHISELED_STAIRS)
                .add(ModBlocks.IDONA_DARK_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.IDONA_LIGHT_SMOOTH_BRICKS_STAIRS)
                .add(ModBlocks.IDONA_GEM_BLOCK_STAIRS)
                .add(ModBlocks.IDONA_GEM_BLOCK_SLAB)
                .add(ModBlocks.DECO_IDONA_ALTAR_BLOCK)
                .add(ModBlocks.DECO_TENOS_ALTAR_BLOCK)
                .add(ModBlocks.DECO_WENDARR_ALTAR_BLOCK)
                .add(ModBlocks.DECO_VELARA_ALTAR_BLOCK);
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.ISKALLIAN_LEAVES_BLOCK)
                .add(ModBlocks.PRISMATIC_FIBER_BLOCK);
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.HELLISH_SAND_BLOCK)
                .add(ModBlocks.VAULT_ESSENCE_BLOCK);
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.TENOS_PLANKS_SLAB)
                .add(ModBlocks.TENOS_PLANKS_STAIRS)
                .add(ModBlocks.OWNED_CRAFTING_TABLE_BLOCK)
                .add(iskallia.vault.init.ModBlocks.CARDBOARD_BOX)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_dark_oak_log"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_spruce_log"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_oak_log"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_birch_log"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_acacia_log"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_crimson_stem"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_warped_stem"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("chipped", "stripped_jungle_log"));
        tag(BlockTags.LEAVES)
                .add(ModBlocks.ISKALLIAN_LEAVES_BLOCK);
        tag(WUTODIE_STORAGE_BLOCK)
                .add(ModBlocks.WUTODIE);
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .add(ModBlocks.WUTODIE);
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.COLORED_UNOBTANIUMS.values().toArray(new Block[]{}))
                .add(ModBlocks.RAINBOW_UNOBTANIUM);
       addCompressibleTags();
    }

    private void addCompressibleTags() {
        enum RequiresWood {
            VAULT_STONE, VAULT_COBBLESTONE, ORNATE_BLOCK, GILDED_BLOCK, VAULT_DIAMOND_BLOCK, ANCIENT_COPPER_BLOCK
        }
        enum RequiresPick {
            VAULT_STONE, VAULT_COBBLESTONE, ORNATE_BLOCK, GILDED_BLOCK, VAULT_DIAMOND_BLOCK, ANCIENT_COPPER_BLOCK, VAULT_PLATING_BLOCK, CARBON_BLOCK, CHROMATIC_IRON_BLOCK, CHROMATIC_STEEL_BLOCK, CHROMATIC_GOLD_BLOCK, SILVER_SCRAP_BLOCK, LIVING_ROCK_BLOCK_COBBLE, ORNATE_CHAIN, TOPAZ_BLOCK
        }
        enum RequiresShovel {
            VAULT_ESSENCE_BLOCK, SANDY_BLOCK, ROTTEN_MEAT_BLOCK, COMPRESSED_SOOT_BLOCK
        }
        enum RequiresHoe {
            MAGIC_SILK_BLOCK, VELVET_BLOCK
        }

        addTagFromList(RequiresPick.values(), BlockTags.MINEABLE_WITH_PICKAXE);
        addTagFromList(RequiresWood.values(), Tags.Blocks.NEEDS_WOOD_TOOL);
        addTagFromList(RequiresHoe.values(), BlockTags.MINEABLE_WITH_HOE);
        addTagFromList(RequiresHoe.values(), BlockTags.MINEABLE_WITH_SHOVEL);
        addTagFromList(RequiresShovel.values(), BlockTags.MINEABLE_WITH_SHOVEL);
    }

    private <T extends Enum<?>> void addTagFromList(T[] values, TagKey<Block> tag) {
        for (T value : values) {
            List<Supplier<Block>> blockList = ModCompressibleBlocks.getRegisteredBlocks().entrySet().stream().filter(e -> e.getKey().name().equalsIgnoreCase(value.name())).findFirst().map(Map.Entry::getValue).orElse(List.of());
            blockList.forEach(e -> tag(tag).add(e.get()));
        }
    }
}
