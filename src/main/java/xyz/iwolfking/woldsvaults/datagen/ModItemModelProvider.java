package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;


public class ModItemModelProvider extends ItemModelProvider {


    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, WoldsVaults.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.WEAPON_TYPE_FOCUS);
        simpleItem(ModItems.ARCANE_ESSENCE);
        simpleItem(ModItems.ARCANE_SHARD);
        simpleItem(ModItems.AUGMENT_PIECE);
        simpleItem(ModItems.ARCANE_ESSENCE);
        simpleItem(ModItems.BLACK_CHROMATIC_STEEL_ANGEL_RING);
        simpleItem(ModItems.BLAZING_FOCUS);
        simpleItem(ModItems.ALL_SEEING_EYE_CAPSTONE);
        simpleItem(ModItems.ENCHANTED_CAPSTONE);
        simpleItem(ModItems.FRENZY_CAPSTONE);

        spawnEgg(ModItems.BLUE_BLAZE_EGG);
        spawnEgg(ModItems.BOOGIEMAN_EGG);


        simpleResource("basic_pouch_classic");
        simpleResource("basic_pouch_g");
        simpleResource("basic_pouch_r");

    }


    private ItemModelBuilder simpleItem(Item item) {
        return withExistingParent(item.getRegistryName().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(WoldsVaults.MOD_ID, "item/" + item.getRegistryName().getPath()));
    }

    private ItemModelBuilder simpleResource(String resource) {
        return withExistingParent(resource,
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(WoldsVaults.MOD_ID, "item/" + resource));
    }

    private ItemModelBuilder handheldItem(Item item) {
        return withExistingParent(item.getRegistryName().getPath(),
                new ResourceLocation("item/handheld")).texture("layer0",
                new ResourceLocation(WoldsVaults.MOD_ID, "item/" + item.getRegistryName().getPath()));
    }

    private void spawnEgg(Item item) {
        withExistingParent(item.getRegistryName().getPath(), new ResourceLocation("item/template_spawn_egg"));
    }
}
