package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.block.*;
import iskallia.vault.block.render.ScavengerAltarRenderer;
import iskallia.vault.core.vault.stat.VaultChestType;
import iskallia.vault.init.ModDecorativeBlocks;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CoinBlockItem;
import iskallia.vault.item.VaultChestBlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.*;
import xyz.iwolfking.woldsvaults.blocks.tiles.*;
import xyz.iwolfking.woldsvaults.client.renderers.*;

import java.util.function.Consumer;

public class ModBlocks {

    public static final VaultSalvagerBlock VAULT_SALVAGER_BLOCK;
    public static final IskallianLeavesBlock ISKALLIAN_LEAVES_BLOCK;
    public static final HellishSandBlock HELLISH_SAND_BLOCK;
    public static final DungeonPedestalBlock DUNGEON_PEDESTAL_BLOCK;
    public static final DecoScavengerAltarBlock DECO_SCAVENGER_ALTAR_BLOCK;
    public static final DecoObeliskBlock DECO_OBELISK_BLOCK;
    public static final DecoLodestoneBlock DECO_LODESTONE_BLOCK;
    public static final DecoMonolithBlock DECO_MONOLITH_BLOCK;
    public static final SurvivalMobBarrier SURVIVAL_MOB_BARRIER;
    public static final VaultInfuserBlock VAULT_INFUSER_BLOCK;
    public static final VaultInfuserBlock CHROMATIC_STEEL_INFUSER_BLOCK;
    public static final GatewayChannelingBlock GATEWAY_CHANNELING_BLOCK;
    public static final FracturedObelisk FRACTURED_OBELISK;
    public static final MonolithControllerBlock MONOLITH_CONTROLLER;
    public static final VaultCrateBlock VAULT_CRATE_CORRUPTED;
    public static final VaultCrateBlock VAULT_CRATE_ALCHEMY;
    public static final BrewingAltar BREWING_ALTAR;
    public static final Block DOLL_DISMANTLING_BLOCK;

    public static final Block PRISMATIC_FIBER_BLOCK;
    public static final CoinPileDecorBlock VAULT_PALLADIUM_PILE;
    public static final CoinPileDecorBlock VAULT_IRIDIUM_PILE;
    public static BlockItem VAULT_PALLADIUM;
    public static BlockItem VAULT_IRIDIUM;

    public static final BlockEntityType<VaultSalvagerTileEntity> VAULT_SALVAGER_ENTITY;
    public static final BlockEntityType<IskallianLeavesTileEntity> ISKALLIAN_LEAVES_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<HellishSandTileEntity> HELLISH_SAND_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DungeonPedestalTileEntity> DUNGEON_PEDESTAL_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DecoScavengerAltarEntity> DECO_SCAVENGER_ALTAR_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DecoObeliskTileEntity> DECO_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DecoLodestoneTileEntity> DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DecoMonolithTileEntity> DECO_MONOLITH_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<SurvivalMobBarrierTileEntity> SURVIVAL_MOB_BARRIER_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<BackpackBlockEntity> SOPHISTICATED_BACKPACK;
    public static final BlockEntityType<VaultInfuserTileEntity> VAULT_INFUSER_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<DollDismantlingTileEntity> DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE;

    //Workstations
    public static final AugmentCraftingTableBlock AUGMENT_CRAFTING_TABLE;
    public static final ModBoxWorkstationBlock MOD_BOX_WORKSTATION;
    public static final WeavingStationBlock WEAVING_STATION;
    public static final BlockEntityType<AugmentCraftingTableTileEntity> AUGMENT_CRAFTING_TABLE_ENTITY;
    public static final BlockEntityType<ModBoxWorkstationTileEntity> MOD_BOX_WORKSTATION_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<FracturedObeliskTileEntity> FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<MonolithControllerTileEntity> MONOLITH_CONTROLLER_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<WeavingStationTileEntity> WEAVING_STATION_TILE_ENTITY_BLOCK_ENTITY_TYPE;
    public static final BlockEntityType<BrewingAltarTileEntity> BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE;

    //Shop Pedestals
    public static final ShopPedestalBlock ETCHING_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock GOD_VENDOR_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock BLACKSMITH_VENDOR_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock RARE_VENDOR_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock OMEGA_VENDOR_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock SPOOKY_VENDOR_PEDESTAL = new ShopPedestalBlock();
    public static final ShopPedestalBlock CARD_VENDOR_PEDESTAL = new ShopPedestalBlock();

    public static final BackpackBlock XL_BACKPACK;

    public static final Block INFUSED_DRIFTWOOD_PLANKS;
    public static final Block NULLITE_ORE;

    //Decorative Blocks
    public static final VaultGemStairsBlock WUTODIE_STAIRS;
    public static final VaultGemSlabBlock WUTODIE_SLAB;
    public static final VaultGemWallBlock WUTODIE_WALL;

    static {
        INFUSED_DRIFTWOOD_PLANKS = new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));
        NULLITE_ORE = new Block(BlockBehaviour.Properties.of(Material.STONE).strength(250F, 1500F));
        VAULT_PALLADIUM_PILE = new CoinPileDecorBlock();
        VAULT_IRIDIUM_PILE = new CoinPileDecorBlock();
        VAULT_PALLADIUM  = new CoinBlockItem(VAULT_PALLADIUM_PILE, new Item.Properties().tab(ModItems.VAULT_MOD_GROUP));
        VAULT_IRIDIUM  = new CoinBlockItem(VAULT_IRIDIUM_PILE, new Item.Properties().tab(ModItems.VAULT_MOD_GROUP));
        VAULT_SALVAGER_BLOCK = new VaultSalvagerBlock();
        ISKALLIAN_LEAVES_BLOCK = new IskallianLeavesBlock();
        HELLISH_SAND_BLOCK = new HellishSandBlock();
        DUNGEON_PEDESTAL_BLOCK = new DungeonPedestalBlock();
        DECO_SCAVENGER_ALTAR_BLOCK = new DecoScavengerAltarBlock();
        DECO_OBELISK_BLOCK = new DecoObeliskBlock();
        DECO_LODESTONE_BLOCK = new DecoLodestoneBlock();
        DECO_MONOLITH_BLOCK = new DecoMonolithBlock();
        SURVIVAL_MOB_BARRIER = new SurvivalMobBarrier();
        XL_BACKPACK = new BackpackBlock(12000);
        VAULT_INFUSER_BLOCK = new VaultInfuserBlock(1);
        CHROMATIC_STEEL_INFUSER_BLOCK = new VaultInfuserBlock(4);
        AUGMENT_CRAFTING_TABLE = new AugmentCraftingTableBlock();
        MOD_BOX_WORKSTATION = new ModBoxWorkstationBlock();
        WEAVING_STATION = new WeavingStationBlock();
        PRISMATIC_FIBER_BLOCK = new Block(BlockBehaviour.Properties.copy(Blocks.GREEN_WOOL).lightLevel((state) -> 8));
        GATEWAY_CHANNELING_BLOCK = new GatewayChannelingBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK));
        FRACTURED_OBELISK = new FracturedObelisk();
        VAULT_CRATE_CORRUPTED = new VaultCrateBlock();
        VAULT_CRATE_ALCHEMY = new VaultCrateBlock();
        MONOLITH_CONTROLLER = new MonolithControllerBlock();
        BREWING_ALTAR = new BrewingAltar();
        WUTODIE_SLAB = new VaultGemSlabBlock(ModItems.WUTODIE_GEM);
        WUTODIE_WALL = new VaultGemWallBlock(ModItems.WUTODIE_GEM);
        WUTODIE_STAIRS = new VaultGemStairsBlock(ModItems.WUTODIE_GEM);
        DOLL_DISMANTLING_BLOCK = new DollDismantlingBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(1.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops(), DollDismantlingBlock.DOLL_DISMANTLING_SHAPE);
        VAULT_SALVAGER_ENTITY = BlockEntityType.Builder.of(VaultSalvagerTileEntity::new, VAULT_SALVAGER_BLOCK).build(null);
        ISKALLIAN_LEAVES_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(IskallianLeavesTileEntity::new, ISKALLIAN_LEAVES_BLOCK).build(null);
        HELLISH_SAND_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(HellishSandTileEntity::new, HELLISH_SAND_BLOCK).build(null);
        DUNGEON_PEDESTAL_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DungeonPedestalTileEntity::new, DUNGEON_PEDESTAL_BLOCK).build(null);
        DECO_SCAVENGER_ALTAR_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DecoScavengerAltarEntity::new, DECO_SCAVENGER_ALTAR_BLOCK).build(null);
        DECO_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DecoObeliskTileEntity::new, DECO_OBELISK_BLOCK).build(null);
        DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DecoLodestoneTileEntity::new, DECO_LODESTONE_BLOCK).build(null);
        DECO_MONOLITH_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DecoMonolithTileEntity::new, DECO_MONOLITH_BLOCK).build(null);
        SURVIVAL_MOB_BARRIER_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(SurvivalMobBarrierTileEntity::new, SURVIVAL_MOB_BARRIER).build(null);
        VAULT_INFUSER_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(VaultInfuserTileEntity::new, new Block[]{VAULT_INFUSER_BLOCK, CHROMATIC_STEEL_INFUSER_BLOCK}).build(null);
        SOPHISTICATED_BACKPACK = BlockEntityType.Builder.of(BackpackBlockEntity::new, new Block[]{XL_BACKPACK}).build(null);
        AUGMENT_CRAFTING_TABLE_ENTITY = BlockEntityType.Builder.of(AugmentCraftingTableTileEntity::new, new Block[]{AUGMENT_CRAFTING_TABLE}).build(null);
        MOD_BOX_WORKSTATION_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ModBoxWorkstationTileEntity::new, new Block[]{MOD_BOX_WORKSTATION}).build(null);
        FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(FracturedObeliskTileEntity::new, FRACTURED_OBELISK).build(null);
        MONOLITH_CONTROLLER_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(MonolithControllerTileEntity::new, MONOLITH_CONTROLLER).build(null);
        WEAVING_STATION_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(WeavingStationTileEntity::new, new Block[]{WEAVING_STATION}).build(null);
        BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(BrewingAltarTileEntity::new, new Block[]{BREWING_ALTAR}).build(null);
        DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(DollDismantlingTileEntity::new, new Block[]{DOLL_DISMANTLING_BLOCK}).build(null);
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        registerBlock(event, VAULT_SALVAGER_BLOCK, VaultMod.id("vault_salvager"));
        registerBlock(event, ISKALLIAN_LEAVES_BLOCK, WoldsVaults.id("iskallian_leaves"));
        registerBlock(event, HELLISH_SAND_BLOCK, WoldsVaults.id("hellish_sand"));
        registerBlock(event, DUNGEON_PEDESTAL_BLOCK, WoldsVaults.id("dungeon_pedestal"));
        registerBlock(event, DECO_SCAVENGER_ALTAR_BLOCK, WoldsVaults.id("scavenger_altar"));
        registerBlock(event, DECO_OBELISK_BLOCK, WoldsVaults.id("obelisk"));
        registerBlock(event, DECO_LODESTONE_BLOCK, WoldsVaults.id("lodestone"));
        registerBlock(event, DECO_MONOLITH_BLOCK, WoldsVaults.id("monolith"));
        registerBlock(event, SURVIVAL_MOB_BARRIER, WoldsVaults.id("mob_barrier_red"));
        registerBlock(event, VAULT_PALLADIUM_PILE, VaultMod.id("vault_palladium"));
        registerBlock(event, VAULT_IRIDIUM_PILE, VaultMod.id("vault_iridium"));
        registerBlock(event, XL_BACKPACK, WoldsVaults.id("xl_backpack"));
        registerBlock(event, PRISMATIC_FIBER_BLOCK, WoldsVaults.id("prismatic_fiber_block"));
        registerBlock(event, AUGMENT_CRAFTING_TABLE, WoldsVaults.id("augment_crafting_table"));
        registerBlock(event, MOD_BOX_WORKSTATION, WoldsVaults.id("mod_box_workstation"));
        registerBlock(event, WEAVING_STATION, WoldsVaults.id("weaving_station"));
        registerBlock(event, INFUSED_DRIFTWOOD_PLANKS, WoldsVaults.id("infused_driftwood_planks"));
        registerBlock(event, NULLITE_ORE, WoldsVaults.id("nullite_ore"));
        registerBlock(event, VAULT_INFUSER_BLOCK, WoldsVaults.id("chromatic_iron_vault_infuser"));
        registerBlock(event, CHROMATIC_STEEL_INFUSER_BLOCK, WoldsVaults.id("chromatic_steel_vault_infuser"));
        registerBlock(event, GATEWAY_CHANNELING_BLOCK, WoldsVaults.id("gateway_channeling_block"));
        registerBlock(event, ETCHING_PEDESTAL, WoldsVaults.id("etching_shop_pedestal"));
        registerBlock(event, FRACTURED_OBELISK, WoldsVaults.id("fractured_obelisk"));
        registerBlock(event, VAULT_CRATE_CORRUPTED, WoldsVaults.id("vault_crate_corrupt"));
        registerBlock(event, VAULT_CRATE_ALCHEMY, WoldsVaults.id("vault_crate_alchemy"));
        registerBlock(event, MONOLITH_CONTROLLER, WoldsVaults.id("monolith_controller"));
        registerBlock(event, BLACKSMITH_VENDOR_PEDESTAL, WoldsVaults.id("blacksmith_shop_pedestal"));
        registerBlock(event, RARE_VENDOR_PEDESTAL, WoldsVaults.id("rare_shop_pedestal"));
        registerBlock(event, OMEGA_VENDOR_PEDESTAL, WoldsVaults.id("omega_shop_pedestal"));
        registerBlock(event, GOD_VENDOR_PEDESTAL, WoldsVaults.id("god_shop_pedestal"));
        registerBlock(event, SPOOKY_VENDOR_PEDESTAL, WoldsVaults.id("spooky_shop_pedestal"));
        registerBlock(event, CARD_VENDOR_PEDESTAL, WoldsVaults.id("card_shop_pedestal"));
        registerBlock(event, WUTODIE_SLAB, VaultMod.id("block_gem_wutodie_slab"));
        registerBlock(event, WUTODIE_STAIRS, VaultMod.id("block_gem_wutodie_stairs"));
        registerBlock(event, WUTODIE_WALL, VaultMod.id("block_gem_wutodie_wall"));
        registerBlock(event, BREWING_ALTAR, WoldsVaults.id("brewing_altar"));
        registerBlock(event, DOLL_DISMANTLING_BLOCK, WoldsVaults.id("doll_dismantler"));

    }
    public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        registerTileEntity(event, VAULT_SALVAGER_ENTITY, VaultMod.id("vault_salvager_tile_entity"));
        registerTileEntity(event, ISKALLIAN_LEAVES_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("iskallian_leaves_tile_entity"));
        registerTileEntity(event, HELLISH_SAND_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("hellish_sand_tile_entity"));
        registerTileEntity(event, DUNGEON_PEDESTAL_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("dungeon_pedestal_tile_entity"));
        registerTileEntity(event, DECO_SCAVENGER_ALTAR_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("scavenger_altar_deco_tile_entity"));
        registerTileEntity(event, DECO_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("obelisk_deco_tile_entity"));
        registerTileEntity(event, DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("lodestone_deco_tile_entity"));
        registerTileEntity(event, DECO_MONOLITH_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("monolith_deco_tile_entity"));
        registerTileEntity(event, SURVIVAL_MOB_BARRIER_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("mob_barrier_entity"));
        registerTileEntity(event, AUGMENT_CRAFTING_TABLE_ENTITY, WoldsVaults.id("augment_table_entity"));
        registerTileEntity(event, MOD_BOX_WORKSTATION_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("mod_box_workstation_entity"));
        registerTileEntity(event, WEAVING_STATION_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("weaving_station_entity"));
        registerTileEntity(event, VAULT_INFUSER_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("vault_infuser_entity"));
        registerTileEntity(event, FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("fractured_obelisk_tile_entity"));
        registerTileEntity(event, MONOLITH_CONTROLLER_BLOCK_ENTITY_TYPE, WoldsVaults.id("monolith_controller_tile_entity"));
        registerTileEntity(event, BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("brewing_altar_tile_entity"));
        registerTileEntity(event, DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE, WoldsVaults.id("doll_dismantler_tile_entity"));
    }

    public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        registerBlockItem(event, VAULT_SALVAGER_BLOCK);
        registerBlockItem(event, ISKALLIAN_LEAVES_BLOCK);
        registerBlockItem(event, HELLISH_SAND_BLOCK);
        registerBlockItem(event, DUNGEON_PEDESTAL_BLOCK);
        registerBlockItem(event, DECO_SCAVENGER_ALTAR_BLOCK);
        registerBlockItem(event, DECO_OBELISK_BLOCK);
        registerBlockItem(event, DECO_LODESTONE_BLOCK);
        registerBlockItem(event, DECO_MONOLITH_BLOCK);
        registerBlockItem(event, SURVIVAL_MOB_BARRIER);
        registerBlockItem(event, AUGMENT_CRAFTING_TABLE);
        registerBlockItem(event, MOD_BOX_WORKSTATION);
        registerBlockItem(event, WEAVING_STATION);
        registerBlockItem(event, INFUSED_DRIFTWOOD_PLANKS);
        registerBlockItem(event, NULLITE_ORE);
        registerBlockItem(event, VAULT_INFUSER_BLOCK);
        registerBlockItem(event, CHROMATIC_STEEL_INFUSER_BLOCK);
        registerBlockItem(event, GATEWAY_CHANNELING_BLOCK);
        registerBlockItem(event, ETCHING_PEDESTAL);
        registerBlockItem(event, GOD_VENDOR_PEDESTAL);
        registerBlockItem(event, BLACKSMITH_VENDOR_PEDESTAL);
        registerBlockItem(event, RARE_VENDOR_PEDESTAL);
        registerBlockItem(event, OMEGA_VENDOR_PEDESTAL);
        registerBlockItem(event, SPOOKY_VENDOR_PEDESTAL);
        registerBlockItem(event, CARD_VENDOR_PEDESTAL);
        registerBlockItem(event, PRISMATIC_FIBER_BLOCK);
        registerBlockItem(event, VAULT_PALLADIUM_PILE, VAULT_PALLADIUM);
        registerBlockItem(event, VAULT_IRIDIUM_PILE, VAULT_IRIDIUM);
        registerBlockItem(event, FRACTURED_OBELISK);
        registerBlockItem(event, VAULT_CRATE_CORRUPTED, 1, Item.Properties::fireResistant);
        registerBlockItem(event, VAULT_CRATE_ALCHEMY, 1, Item.Properties::fireResistant);
        registerBlockItem(event, MONOLITH_CONTROLLER);
        registerBlockItem(event, WUTODIE_STAIRS);
        registerBlockItem(event, WUTODIE_SLAB);
        registerBlockItem(event, WUTODIE_WALL);
        registerBlockItem(event, BREWING_ALTAR);
        registerBlockItem(event, DOLL_DISMANTLING_BLOCK);
    }

    public static void registerTileEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DECO_SCAVENGER_ALTAR_ENTITY_BLOCK_ENTITY_TYPE, ScavengerAltarRenderer::new);
        event.registerBlockEntityRenderer(SURVIVAL_MOB_BARRIER_TILE_ENTITY_BLOCK_ENTITY_TYPE, SurvivalMobBarrierRenderer::new);
        event.registerBlockEntityRenderer(DUNGEON_PEDESTAL_TILE_ENTITY_BLOCK_ENTITY_TYPE, DungeonPedestalRenderer::new);
        event.registerBlockEntityRenderer(FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, FracturedObeliskRenderer::new);
        event.registerBlockEntityRenderer(MONOLITH_CONTROLLER_BLOCK_ENTITY_TYPE, MonolithControllerRenderer::new);
        event.registerBlockEntityRenderer(BREWING_ALTAR_TILE_ENTITY_BLOCK_ENTITY_TYPE, BrewingAltarRenderer::new);
        event.registerBlockEntityRenderer(DOLL_DISMANTLING_TILE_ENTITY_BLOCK_ENTITY_TYPE, DollDismantlingRenderer::new);
        //event.registerBlockEntityRenderer(DECO_LODESTONE_TILE_ENTITY_BLOCK_ENTITY_TYPE, DecoLodestoneRenderer::new);
    }



    private static void registerBlock(RegistryEvent.Register<Block> event, Block block, ResourceLocation id) {
        block.setRegistryName(id);
        event.getRegistry().register(block);
    }



    private static <T extends BlockEntity> void registerTileEntity(RegistryEvent.Register<BlockEntityType<?>> event, BlockEntityType<?> type, ResourceLocation id) {
        type.setRegistryName(id);
        event.getRegistry().register(type);
    }

    private static void registerBlockItemWithEffect(RegistryEvent.Register<Item> event, Block block, int maxStackSize, Consumer<Item.Properties> adjustProperties) {
        Item.Properties properties = (new Item.Properties()).tab(ModItems.VAULT_MOD_GROUP).stacksTo(maxStackSize);
        adjustProperties.accept(properties);
        BlockItem blockItem = new BlockItem(block, properties) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        };
        registerBlockItem(event, block, blockItem);
    }

    private static void registerBlockItem(RegistryEvent.Register<Item> event, Block block) {
        registerBlockItem(event, block, 64);
    }

    private static void registerBlockItem(RegistryEvent.Register<Item> event, Block block, int maxStackSize) {
        registerBlockItem(event, block, maxStackSize, properties -> {});
    }

    private static void registerBlockItem(RegistryEvent.Register<Item> event, Block block, int maxStackSize, Consumer<Item.Properties> adjustProperties) {
        Item.Properties properties = (new Item.Properties()).tab(ModItems.VAULT_MOD_GROUP).stacksTo(maxStackSize);
        adjustProperties.accept(properties);
        registerBlockItem(event, block, new BlockItem(block, properties));
    }


    private static void registerBlockItem(RegistryEvent.Register<Item> event, Block block, BlockItem blockItem) {
        blockItem.setRegistryName(block.getRegistryName());
        event.getRegistry().register(blockItem);
    }

    private static void registerTallBlockItem(RegistryEvent.Register<Item> event, Block block) {
        DoubleHighBlockItem tallBlockItem = new DoubleHighBlockItem(block, (new Item.Properties()).tab(ModItems.VAULT_MOD_GROUP).stacksTo(64));
        tallBlockItem.setRegistryName(block.getRegistryName());
        event.getRegistry().register(tallBlockItem);
    }
}
