package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.config.ResearchesGUIConfig;
import iskallia.vault.init.ModConfigs;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.vhapi.api.registry.CustomCatalystModelRegistry;
import xyz.iwolfking.vhapi.api.registry.CustomInscriptionModelRegistry;
import xyz.iwolfking.vhapi.api.util.ResourceLocUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;
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
        simpleItem(ModItems.CRYSTAL_SEAL_ENCHANTER);
        simpleItem(ModItems.CRYSTAL_SEAL_DOOMSAYER);
        simpleItem(ModItems.CRYSTAL_SEAL_SPIRITS);
        simpleItem(ModItems.CRYSTAL_SEAL_TITAN);
        simpleItem(ModItems.CRYSTAL_SEAL_ALCHEMY);
        simpleItem(ModItems.CRYSTAL_SEAL_UNHINGED);
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
        simpleItem(ModItems.SUPPLY_BOX);
        simpleItem(ModItems.AUGMENT_BOX);
        simpleItem(ModItems.ALTAR_DECATALYZER);
        simpleItem(ModItems.CHROMATIC_GOLD_INGOT);
        simpleItem(ModItems.CHROMATIC_GOLD_NUGGET);
        simpleItem(ModItems.SMASHED_VAULT_GEM);
        simpleItem(ModItems.SMASHED_VAULT_GEM_CLUSTER);
        simpleItem(ModItems.POG_PRISM);
        simpleItem(ModItems.POGOMINIUM_INGOT);
        simpleItem(ModItems.POLTERGEIST_PLUM);
        simpleItem(ModItems.PRISMATIC_ANGEL_RING);
        simpleItem(ModItems.PRISMATIC_FIBER);
        simpleItem(ModItems.RECIPE_BLUEPRINT);
        simpleItem(ModItems.REPAIR_AUGMENTER);
        getBuilder(ModItems.RESEARCH_TOKEN.getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile(
                        new ResourceLocation("builtin/entity")
                ));
        singleTexture("research_token_base", new ResourceLocation("minecraft", "item/generated"), WoldsVaults.id("item/research_token"));
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
        simpleItem(ModItems.GEM_BOX);
        simpleItem(ModItems.WOLD_STAR);
        simpleItem(ModItems.WOLD_STAR_CHUNK);
        simpleItem(ModItems.GEM_REAGENT_ASHIUM);
        simpleItem(ModItems.GEM_REAGENT_BOMIGNITE);
        simpleItem(ModItems.GEM_REAGENT_GORGINITE);
        simpleItem(ModItems.GEM_REAGENT_ISKALLIUM);
        simpleItem(ModItems.GEM_REAGENT_PETEZANITE);
        simpleItem(ModItems.GEM_REAGENT_SPARKLETINE);
        simpleItem(ModItems.GEM_REAGENT_UPALINE);
        simpleItem(ModItems.GEM_REAGENT_TUBIUM);
        simpleItem(ModItems.GEM_REAGENT_XENIUM);
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
        simpleItem(ModItems.COMPANION_REROLLER);
        simpleItem(ModItems.POGGING_SEED_BASE);
        simpleItem(ModItems.ECHOING_SEED_BASE);
        simpleItem(ModItems.UNINFUSED_TERRASTEEL_INGOT);
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

        //Vault Modifier icon models
        vaultModifier(VaultMod.id("orematic"), "oremania");
        vaultModifier(VaultMod.id("resistant_mobs"));
        vaultModifier(VaultMod.id("phantasmal_mobs"));
        vaultModifier(VaultMod.id("fleet_footed_mobs"), "fleetfooted_mobs");
        vaultModifier(VaultMod.id("witch_party"));
        vaultModifier(VaultMod.id("ghost_party"));
        vaultModifier(VaultMod.id("ghost_town"));
        vaultModifier(VaultMod.id("blinding"));
        vaultModifier(VaultMod.id("corrosive"));
        vaultModifier(VaultMod.id("mildly_enchanted"));
        vaultModifier(VaultMod.id("enchanted"));
        vaultModifier(VaultMod.id("armed"));
        vaultModifier(VaultMod.id("surprise_boxes"));
        vaultModifier(VaultMod.id("fungal"));
        vaultModifier(VaultMod.id("fungal_infestation"));
        vaultModifier(VaultMod.id("safari"));
        vaultModifier(VaultMod.id("winter"));
        vaultModifier(VaultMod.id("retro"));
        vaultModifier(VaultMod.id("sweet_retro"), "candy");
        vaultModifier(VaultMod.id("explosive"));
        vaultModifier(VaultMod.id("electric"));
        vaultModifier(VaultMod.id("acidic"));
        vaultModifier(VaultMod.id("infernal"));
        vaultModifier(VaultMod.id("springy"));
        vaultModifier(VaultMod.id("jumpy_deluxe"));
        vaultModifier(VaultMod.id("plated"));
        vaultModifier(VaultMod.id("stunning"), "hex_stunning");
        vaultModifier(VaultMod.id("blinding"), "hex_blinding");
        vaultModifier(VaultMod.id("raging"));
        vaultModifier(VaultMod.id("spooky"));
        vaultModifier(VaultMod.id("levitation"));
        vaultModifier(VaultMod.id("ghost_town"), "ghost_city");
        vaultModifier(VaultMod.id("vexation"), "vexation");

        skillScroll("colossus");
        skillScroll("expunge");
        ModConfigs.RESEARCHES_GUI = new ResearchesGUIConfig().readConfig();
        ModConfigs.RESEARCHES_GUI.getStyles().forEach((name, s) -> {
            researchToken(ModConfigs.RESEARCHES_GUI.getStyles().get(name).icon);
        });

        CustomInscriptionModelRegistry.getModelMap().forEach(this::vaultInscription);
        CustomCatalystModelRegistry.getModelMap().forEach(this::vaultCatalyst);

        ModItems.COLORED_UNOBTANIUMS.forEach(((dyeColor, basicItem) -> {
            simpleItem(basicItem);
        }));
        simpleItem(ModItems.RAINBOW_UNOBTANIUM);

        ModCompressibleBlocks.getRegisteredBlocks().forEach((k, v) -> {
            for (int i = 0; i < v.size(); i ++) {
                var name = k.name().toLowerCase();
                withExistingParent(Compressium.MODID + ":" + name + "_" + (i + 1), new ResourceLocation(Compressium.MODID, "block/" + name + "_" + (i + 1)));
            }
        });

    }


    private ItemModelBuilder simpleItem(Item item) {
        return withExistingParent(item.getRegistryName().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(WoldsVaults.MOD_ID, "item/" + item.getRegistryName().getPath()));
    }

    public ItemModelBuilder skillScroll(String skillId) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/skills/" + skillId).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "gui/abilities/" + skillId));
    }

    public ItemModelBuilder researchToken(ResourceLocation icon) {
        return getBuilder(new ResourceLocation(WoldsVaults.MOD_ID, "item/researches/" + ResourceLocUtils.getStrippedPath(icon)).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "gui/researches/" + ResourceLocUtils.getStrippedPath(icon)));
    }

    private ItemModelBuilder vaultModifier(ResourceLocation modifierId) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/modifiers/" + modifierId.getPath()).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "item/modifiers/" + modifierId.getPath()));
    }

    private ItemModelBuilder vaultInscription(int modelNumber) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/inscription/" + modelNumber).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "item/inscription/" + modelNumber));
    }

    private ItemModelBuilder vaultInscription(int modelNumber, String modelNameOverride) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/inscription/" + modelNumber).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "item/inscription/" + modelNameOverride));
    }


    private ItemModelBuilder vaultCatalyst(int modelNumber, String modelNameOverride) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/catalyst/" + modelNumber).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "item/catalyst/" + modelNameOverride));
    }



    private ItemModelBuilder vaultModifier(ResourceLocation modifierId, String modifierNameOverride) {
        return getBuilder(new ResourceLocation(VaultMod.MOD_ID, "item/modifiers/" + modifierNameOverride).toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",
                        new ResourceLocation(VaultMod.MOD_ID, "item/modifiers/" + modifierId.getPath()));
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
