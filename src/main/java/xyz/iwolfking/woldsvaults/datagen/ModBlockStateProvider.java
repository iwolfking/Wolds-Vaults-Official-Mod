package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, WoldsVaults.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // THEYRE ALL JUST MODELS AND IM TOO
        generateChromaticSteelVaultInfuserModel();
        generateChromaticIronVaultInfuserModel();

        simpleBlock(ModBlocks.HELLISH_SAND_BLOCK);
        simpleBlock(ModBlocks.INFUSED_DRIFTWOOD_PLANKS);
        simpleBlock(ModBlocks.ISKALLIAN_LEAVES_BLOCK);
        simpleBlock(ModBlocks.NULLITE_ORE);
        simpleBlock(ModBlocks.PRISMATIC_FIBER_BLOCK);
    }

    private void generateChromaticSteelVaultInfuserModel() {
        models().withExistingParent("chromatic_steel_vault_infuser", "minecraft:block/orientable")
                .texture("top", modLoc("block/chromatic_steel_vault_infuser_top"))
                .texture("front", modLoc("block/chromatic_steel_vault_infuser_front_on"))
                .texture("side", modLoc("block/chromatic_steel_vault_infuser_side"));
        itemModels().withExistingParent("chromatic_steel_vault_infuser", modLoc("block/chromatic_steel_vault_infuser"));
    }

    private void generateChromaticIronVaultInfuserModel() {
        models().withExistingParent("vault_infuser", "minecraft:block/orientable")
                .texture("top", modLoc("block/chromatic_iron_vault_infuser_top"))
                .texture("front", modLoc("block/chromatic_iron_vault_infuser_front_on"))
                .texture("side", modLoc("block/chromatic_iron_vault_infuser_side"));

        itemModels().withExistingParent("chromatic_iron_vault_infuser", modLoc("block/vault_infuser"));
    }

    private void generateBlockWithModel(Block block) {

    }
}
