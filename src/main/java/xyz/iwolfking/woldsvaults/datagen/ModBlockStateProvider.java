package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.AugmentCraftingTableBlock;
import xyz.iwolfking.woldsvaults.blocks.FracturedObelisk;
import xyz.iwolfking.woldsvaults.blocks.VaultInfuserBlock;
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
        generateGenericItemModelBlockState(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL);
        generateGenericItemModelBlockState(ModBlocks.CARD_VENDOR_PEDESTAL, "god_shop_pedestal");
        generateGenericItemModelBlockState(ModBlocks.DUNGEON_PEDESTAL_BLOCK);
        generateGenericItemModelBlockState(ModBlocks.ETCHING_PEDESTAL);
        generateTwoTallItemModelBlockState(ModBlocks.FRACTURED_OBELISK);


        simpleBlock(ModBlocks.HELLISH_SAND_BLOCK);
        simpleBlock(ModBlocks.INFUSED_DRIFTWOOD_PLANKS);
        simpleBlock(ModBlocks.ISKALLIAN_LEAVES_BLOCK);
        simpleBlock(ModBlocks.NULLITE_ORE);
        simpleBlock(ModBlocks.PRISMATIC_FIBER_BLOCK);
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
}
