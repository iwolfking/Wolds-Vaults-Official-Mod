package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlock;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.*;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    private final ModelFile EMPTY = models().getExistingFile(modLoc("block/base/nothing"));

    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, WoldsVaults.MOD_ID, exFileHelper);
    }



    @Override
    protected void registerStatesAndModels() {
        generateChromaticSteelVaultInfuser();
        generateChromaticIronVaultInfuser();
        generateAugmentTableBlock();
        generateGatewayChannelingBlock();
        generateMonolithController();
        generateWeavingStation();
        generateDecoMonolith();
        generateDecoObelisk();
        generateXLBackpack();
        generateGenericItemModelBlockState(ModBlocks.VAULT_CRATE_CORRUPTED);
        generateGenericItemModelBlockState(ModBlocks.VAULT_CRATE_ALCHEMY);
        generateGenericItemModelBlockState(ModBlocks.DECO_SCAVENGER_ALTAR_BLOCK);
        generateGenericItemModelBlockState(ModBlocks.DECO_LODESTONE_BLOCK);
        generateGenericItemModelBlockState(ModBlocks.MOD_BOX_WORKSTATION);
        generateGenericItemModelBlockState(ModBlocks.OMEGA_VENDOR_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.RARE_VENDOR_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.DUNGEON_PEDESTAL_BLOCK);
        generateGenericItemModelBlockState(ModBlocks.ETCHING_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.GOD_VENDOR_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.BREWING_ALTAR);
        generateGenericItemModelBlockState(ModBlocks.CARD_VENDOR_PEDESTAL, "god_shop_pedestal");
        generateGenericItemModelBlockState(ModBlocks.SPOOKY_VENDOR_PEDESTAL, "etching_shop_pedestal");

        generateTwoTallItemModelBlockState(ModBlocks.FRACTURED_OBELISK);

        simpleBlockWithItem(ModBlocks.HELLISH_SAND_BLOCK);
        simpleBlockWithItem(ModBlocks.INFUSED_DRIFTWOOD_PLANKS);
        simpleBlockWithItem(ModBlocks.ISKALLIAN_LEAVES_BLOCK);
        simpleBlockWithItem(ModBlocks.NULLITE_ORE);
        simpleBlockWithItem(ModBlocks.PRISMATIC_FIBER_BLOCK);
        simpleBlockWithItem(ModBlocks.SURVIVAL_MOB_BARRIER);
    }

    private void simpleBlockWithItem(Block block) {
        simpleBlock(block);
        itemModels().withExistingParent(block.getRegistryName().getPath(), modLoc("block/" + block.getRegistryName().getPath()));
    }

    private void generateGenericItemModelBlockState(Block block) {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(block);
        ModelFile existingModel = models().getExistingFile(modLoc("block/" + block.getRegistryName().getPath()));

        vbb.setModels(vbb.partialState(), vbb.partialState().modelForState().modelFile(existingModel).build());

        itemModels().withExistingParent(block.getRegistryName().getPath(), modLoc("block/" + block.getRegistryName().getPath()));
    }

    // are we fr
    private void generateGenericItemModelBlockState(Block block, String extra) {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(block);
        ModelFile existingModel = models().getExistingFile(modLoc("block/" + extra));

        vbb.setModels(vbb.partialState(), vbb.partialState().modelForState().modelFile(existingModel).build());
        itemModels().withExistingParent(block.getRegistryName().getPath(), modLoc("block/" + extra));
    }

    private void generateTwoTallItemModelBlockState(Block block) {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(block);
        ModelFile existingModel = models().getExistingFile(modLoc("block/" + block.getRegistryName().getPath()));

        vbb.setModels(vbb.partialState().with(FracturedObelisk.HALF, DoubleBlockHalf.UPPER),
                vbb.partialState().modelForState().modelFile(EMPTY).build());
        vbb.setModels(vbb.partialState().with(FracturedObelisk.HALF, DoubleBlockHalf.LOWER),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        itemModels().withExistingParent(block.getRegistryName().getPath(), modLoc("block/" + block.getRegistryName().getPath()));
    }

    private void generateDecoObelisk() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.DECO_OBELISK_BLOCK);
        ModelFile inactive = models().getExistingFile(modLoc("block/obelisk_inactive"));
        ModelFile active = models().getExistingFile(modLoc("block/obelisk_active"));

        vbb.setModels(vbb.partialState().with(DecoObeliskBlock.HALF, DoubleBlockHalf.UPPER),
                vbb.partialState().modelForState().modelFile(EMPTY).build());

        vbb.setModels(vbb.partialState().with(DecoObeliskBlock.HALF, DoubleBlockHalf.LOWER)
                        .with(DecoObeliskBlock.FILLED, false),
                vbb.partialState().modelForState().modelFile(inactive).build());

        vbb.setModels(vbb.partialState().with(DecoObeliskBlock.HALF, DoubleBlockHalf.LOWER)
                        .with(DecoObeliskBlock.FILLED, true),
                vbb.partialState().modelForState().modelFile(active).build());

        itemModels().withExistingParent("obelisk", modLoc("block/obelisk_inactive"));
    }



    private void generateChromaticSteelVaultInfuser() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK);
        ModelFile existingModel = models().withExistingParent("chromatic_steel_vault_infuser", "minecraft:block/orientable");

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.NORTH),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.EAST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(90).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.SOUTH),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(180).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.WEST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(270).build());

        models().withExistingParent("chromatic_steel_vault_infuser", "minecraft:block/orientable")
                .texture("top", modLoc("block/chromatic_steel_vault_infuser_top"))
                .texture("front", modLoc("block/chromatic_steel_vault_infuser_front_on"))
                .texture("side", modLoc("block/chromatic_steel_vault_infuser_side"));
        itemModels().withExistingParent("chromatic_steel_vault_infuser", modLoc("block/chromatic_steel_vault_infuser"));
    }

    private void generateChromaticIronVaultInfuser() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.VAULT_INFUSER_BLOCK);
        ModelFile existingModel = models().withExistingParent("vault_infuser", "minecraft:block/orientable");

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.NORTH),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.EAST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(90).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.SOUTH),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(180).build());

        vbb.setModels(vbb.partialState().with(VaultInfuserBlock.FACING, Direction.WEST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(270).build());


        models().withExistingParent("vault_infuser", "minecraft:block/orientable")
                .texture("top", modLoc("block/chromatic_iron_vault_infuser_top"))
                .texture("front", modLoc("block/chromatic_iron_vault_infuser_front_on"))
                .texture("side", modLoc("block/chromatic_iron_vault_infuser_side"));

        itemModels().withExistingParent("chromatic_iron_vault_infuser", modLoc("block/vault_infuser"));
    }

    private void generateAugmentTableBlock() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.AUGMENT_CRAFTING_TABLE);
        ModelFile existingModel = models().getExistingFile(modLoc("block/augment_crafting_table"));

        vbb.setModels(vbb.partialState().with(AugmentCraftingTableBlock.FACING, Direction.NORTH),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(AugmentCraftingTableBlock.FACING, Direction.EAST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(90).build());

        vbb.setModels(vbb.partialState().with(AugmentCraftingTableBlock.FACING, Direction.SOUTH),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(180).build());

        vbb.setModels(vbb.partialState().with(AugmentCraftingTableBlock.FACING, Direction.WEST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(270).build());

        itemModels().withExistingParent("augment_crafting_table", modLoc("block/augment_crafting_table"));
    }




    private void generateGatewayChannelingBlock() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.GATEWAY_CHANNELING_BLOCK);
        ModelFile existingModel = models().getExistingFile(modLoc("block/gateway_channeling_block"));

        vbb.setModels(vbb.partialState().with(GatewayChannelingBlock.USED, false),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(GatewayChannelingBlock.USED, true),
                vbb.partialState().modelForState().modelFile(existingModel).build());


        itemModels().withExistingParent("gateway_channeling_block", modLoc("block/gateway_channeling_block"));
    }

    private void generateDecoMonolith() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.DECO_MONOLITH_BLOCK);
        ModelFile extinguished = models().getExistingFile(modLoc("block/monolith_extinguished"));
        ModelFile lit = models().getExistingFile(modLoc("block/monolith_lit"));
        ModelFile destroyed = models().getExistingFile(modLoc("block/monolith_destroyed"));

        vbb.setModels(vbb.partialState().with(DecoMonolithBlock.STATE, DecoMonolithBlock.State.EXTINGUISHED),
                vbb.partialState().modelForState().modelFile(extinguished).build());

        vbb.setModels(vbb.partialState().with(DecoMonolithBlock.STATE, DecoMonolithBlock.State.LIT),
                vbb.partialState().modelForState().modelFile(lit).build());

        vbb.setModels(vbb.partialState().with(DecoMonolithBlock.STATE, DecoMonolithBlock.State.DESTROYED),
                vbb.partialState().modelForState().modelFile(destroyed).build());


        itemModels().withExistingParent("monolith", modLoc("block/monolith_lit"));
    }

    private void generateMonolithController() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.MONOLITH_CONTROLLER);

        BlockModelBuilder b = models().withExistingParent("monolith_controller", modLoc("block/base/nothing"))
                .texture("particle", modLoc("block/monolith_controller"));

        vbb.setModels(vbb.partialState(), vbb.partialState().modelForState().modelFile(b).build());
    }

    private void generateWeavingStation() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.WEAVING_STATION);
        ModelFile existingModel = models().getExistingFile(modLoc("block/weaving_station"));

        vbb.setModels(vbb.partialState().with(WeavingStationBlock.FACING, Direction.NORTH),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(WeavingStationBlock.FACING, Direction.EAST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(90).build());

        vbb.setModels(vbb.partialState().with(WeavingStationBlock.FACING, Direction.SOUTH),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(180).build());

        vbb.setModels(vbb.partialState().with(WeavingStationBlock.FACING, Direction.WEST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(270).build());

        itemModels().withExistingParent("weaving_station", modLoc("block/weaving_station"));
    }

    private void generateXLBackpack() {
        VariantBlockStateBuilder vbb = this.getVariantBuilder(ModBlocks.XL_BACKPACK);
        ModelFile existingModel = models().getExistingFile(modLoc("block/xl_backpack"));

        vbb.setModels(vbb.partialState().with(BackpackBlock.FACING, Direction.NORTH),
                vbb.partialState().modelForState().modelFile(existingModel).build());

        vbb.setModels(vbb.partialState().with(BackpackBlock.FACING, Direction.EAST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(90).build());

        vbb.setModels(vbb.partialState().with(BackpackBlock.FACING, Direction.SOUTH),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(180).build());

        vbb.setModels(vbb.partialState().with(BackpackBlock.FACING, Direction.WEST),
                vbb.partialState().modelForState().modelFile(existingModel).rotationY(270).build());

        itemModels().withExistingParent("xl_backpack", modLoc("block/xl_backpack"));
    }
}
