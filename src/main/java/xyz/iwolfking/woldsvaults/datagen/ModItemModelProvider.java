package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;


public class ModItemModelProvider extends ItemModelProvider {


    public ModItemModelProvider(DataGenerator gen, ExistingFileHelper efh) {
        super(gen, WoldsVaults.MOD_ID, efh);
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
        simpleItem(ModItems.CATALYST_BOX);
        simpleItem(ModItems.CHISELING_FOCUS);
        simpleItem(ModItems.CHROMA_CORE);
        simpleItem(ModItems.CHROMATIC_GOLD_ANGEL_RING);
        simpleItem(ModItems.CHROMATIC_IRON_ANGEL_RING);
        simpleItem(ModItems.CHROMATIC_STEEL_ANGEL_RING);
        simpleItem(ModItems.CHUNK_OF_POWER);
        simpleItem(ModItems.COMMUNITY_TOKEN);
        simpleItem(ModItems.CRYSTAL_REINFORCEMENT);
        simpleItem(ModItems.CRYSTAL_SEAL_CORRUPT);
        simpleItem(ModItems.CRYSTAL_SEAL_WARRIOR);
        simpleItem(ModItems.CRYSTAL_SEAL_ZEALOT);
        simpleItem(ModItems.ECCENTRIC_FOCUS);
        simpleItem(ModItems.ENIGMA_EGG);
        simpleItem(ModItems.EXPERTISE_ORB_ITEM);
        simpleItem(ModItems.EXTRAORDINARY_POG_PRISM);
        simpleItem(ModItems.FILTER_NECKLACE);
        simpleItem(ModItems.VAULT_DECO_SCROLL);
        simpleItem(ModItems.GREEDY_VAULT_ROCK);
        simpleItem(ModItems.HASTY_POMEGRANATE);
        simpleItem(ModItems.HEART_OF_CHAOS);
        simpleItem(ModItems.IDONA_DAGGER);
        simpleItem(ModItems.INFUSED_DRIFTWOOD);
        simpleItem(ModItems.INSCRIPTION_BOX);
        simpleItem(ModItems.LAYOUT_MANIPULATOR);
        simpleItem(ModItems.MERCY_ORB);
        simpleItem(ModItems.NULLITE_CRYSTAL);
        simpleItem(ModItems.NULLITE_FRAGMENT);
        simpleItem(ModItems.OBELISK_RESONATOR);
        simpleItem(ModItems.OMEGA_BOX);
        simpleItem(ModItems.POGOMINIUM_INGOT);
        simpleItem(ModItems.POLTERGEIST_PLUM);
        simpleItem(ModItems.PRISMATIC_ANGEL_RING);
        simpleItem(ModItems.PRISMATIC_FIBER);
        simpleItem(ModItems.RECIPE_BLUEPRINT);
        simpleItem(ModItems.REPAIR_AUGMENTER);
        simpleItem(ModItems.RESEARCH_TOKEN);
        simpleItem(ModItems.RESONATING_REINFORCEMENT);
        simpleItem(ModItems.RUINED_ESSENCE);
        simpleItem(ModItems.SKILL_ORB_ITEM);
        simpleItem(ModItems.SOUL_ICHOR);
        simpleItem(ModItems.SPARK_OF_INSPIRATION);
        simpleItem(ModItems.SUSPENSION_FOCUS);
        simpleItem(ModItems.TARGETED_MOD_BOX);
        simpleItem(ModItems.TOME_OF_TENOS);
        simpleItem(ModItems.UNIDENTIFIED_GATEWAY_PEARL);
        simpleItem(ModItems.VAULT_DIAMOND_NUGGET);
        simpleItem(ModItems.VAULT_ROCK_CANDY);
        simpleItem(ModItems.VAULTAR_BOX);
        simpleItem(ModItems.VELARA_APPLE);
        simpleItem(ModItems.WANING_AUGMENTER);
        simpleItem(ModItems.WAXING_AUGMENTER);
        simpleItem(ModItems.WENDARR_GEM);
        simpleItem(ModItems.WISDOM_FRUIT);
        simpleItem(ModItems.ZEPHYR_CHARM);
        simpleItem(ModItems.AURIC_CRYSTAL);
        simpleItem(ModItems.ERRATIC_EMBER);
        simpleItem(ModItems.INGREDIENT_TEMPLATE);
        simpleItem(ModItems.REFINED_POWDER);
        simpleItem(ModItems.ROTTEN_APPLE);
        simpleItem(ModItems.ROTTEN_HEART);
        simpleItem(ModItems.VERDANT_GLOBULE);
        simpleItem(ModItems.CATALYST_STABILITY);
        simpleItem(ModItems.CATALYST_AMPLIFYING);
        simpleItem(ModItems.CATALYST_FOCUSING);
        simpleItem(ModItems.CATALYST_TEMPORAL);
        simpleItem(ModItems.CATALYST_UNSTABLE);
        simpleItem(ModItems.CRYSTAL_SEAL_ALCHEMY);
        //simpleItem(ModItems.WEAPON_TYPE_SETTER);

        spawnEgg(ModItems.BLUE_BLAZE_EGG);
        spawnEgg(ModItems.BOOGIEMAN_EGG);
        spawnEgg(ModItems.MONSTER_EYE_EGG);
        spawnEgg(ModItems.ROBOT_EGG);
        spawnEgg(ModItems.WOLD_EGG);

        charm("idona_token");
        charm("tenos_token");
        charm("velara_token");
        charm("wendarr_token");

        simpleResource("basic_pouch_classic");
        simpleResource("basic_pouch_g");
        simpleResource("basic_pouch_r");
        simpleResource("explorer_trinket_pouch");
        simpleResource("heavy_trinket_pouch");
        simpleResource("hyper_trinket_pouch");
        simpleResource("light_trinket_pouch");
        simpleResource("looters_trinket_pouch");
        simpleResource("prismatic_trinket_pouch");
        simpleResource("slayer_trinket_pouch");
        simpleResource("warrior_trinket_pouch");
        simpleResource("wizard_trinket_pouch");

        simpleResource("alchemy_bottle");
        simpleLayeredResource("bubbling_contents00", "alchemy_bottle", "bubbling_contents00"); // 1 ingredient
        simpleLayeredResource("bubbling_contents01", "alchemy_bottle", "bubbling_contents01"); // 2 ingredients
        simpleLayeredResource("bubbling_contents02", "alchemy_bottle", "bubbling_contents02"); // 3 ingredients
        simpleLayeredResource("bubbling_contents03", "alchemy_bottle", "bubbling_contents03"); // cooking
        generatePotionItem();

        // are we fr
        itemWithTexture(ModItems.UBER_CHAOS_CATALYST, "vault_catalyst_unhinged");
        itemWithTexture(ModItems.STYLISH_FOCUS, "stylish_orb");
        itemWithTexture(ModItems.CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD, "crystal_seal_raid_infinite_hard");
        itemWithTexture(ModItems.VENDOOR_CAPSTONE, "vendoor_capstone");
        itemWithTexture(ModItems.PROSPEROUS_CAPSTONE, "prosperous_capstone");
        itemWithTexture(ModItems.FRENZY_CAPSTONE, "frenzy_capstone");
        itemWithTexture(ModItems.ENCHANTED_CAPSTONE, "enchanted_capstone");
        itemWithTexture(ModItems.ALL_SEEING_EYE_CAPSTONE, "all_seeing_eye_capstone");

        itemWithTexture(ModItems.TRINKET_POUCH, "standard_trinket_pouch");
        itemWithTexture(ModItems.GOD_OFFERING, "god_blessing_idona");


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

    private ItemModelBuilder simpleLayeredResource(String modelName, String base, String overlay) {
        return withExistingParent(modelName, new ResourceLocation("item/generated"))
                .texture("layer0", new ResourceLocation(WoldsVaults.MOD_ID, "item/" + base))
                .texture("layer1", new ResourceLocation(WoldsVaults.MOD_ID, "item/" + overlay));
    }

    private ItemModelBuilder charm(String resource) {
        return withExistingParent("charm/" + resource,
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

    private ItemModelBuilder itemWithTexture(Item item, String texture) {
        return withExistingParent(item.getRegistryName().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(WoldsVaults.MOD_ID, "item/" + texture));
    }


    private void generatePotionItem() {
        getBuilder(ModItems.DECO_POTION.getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/alchemy_bottle"))
                .override()
                .predicate(WoldsVaults.id("potion_variant"), 0.1f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/bubbling_contents00")))
                .end()
                .override()
                .predicate(WoldsVaults.id("potion_variant"), 0.2f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/bubbling_contents01")))
                .end()
                .override()
                .predicate(WoldsVaults.id("potion_variant"), 0.3f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/bubbling_contents02")))
                .end()
                .override()
                .predicate(WoldsVaults.id("potion_variant"), 0.4f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/bubbling_contents03")))
                .end();
    }
}
