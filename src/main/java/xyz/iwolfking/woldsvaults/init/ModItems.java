package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.BasicMobEggItem;
import iskallia.vault.item.BasicScavengerItem;
import iskallia.vault.item.ItemVaultCrystalSeal;
import iskallia.vault.item.LootableItem;
import iskallia.vault.item.VaultBasicFoodItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.lib.item.MultiLootableItem;
import xyz.iwolfking.woldsvaults.items.*;
import xyz.iwolfking.woldsvaults.items.alchemy.CatalystItem;
import xyz.iwolfking.woldsvaults.items.alchemy.DecoPotionItem;
import xyz.iwolfking.woldsvaults.items.alchemy.AlchemyIngredientItem;
import xyz.iwolfking.woldsvaults.items.fruits.HastyPomegranteItem;
import xyz.iwolfking.woldsvaults.items.fruits.PoltergeistPlum;
import xyz.iwolfking.woldsvaults.items.fruits.WisdomFruitItem;
import xyz.iwolfking.woldsvaults.items.gear.*;
import xyz.iwolfking.woldsvaults.items.rings.*;

import java.util.Random;

import static iskallia.vault.init.ModItems.GEAR_GROUP;
import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    public static final Random rand = new Random();
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_MONOLITH;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_UNHINGED;

    public static ItemVaultCrystalSeal CRYSTAL_SEAL_SPIRITS;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_ENCHANTER;

    public static ItemVaultCrystalSeal CRYSTAL_SEAL_TITAN;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_DOOMSAYER;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_ZEALOT;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_WARRIOR;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_CORRUPT;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_ALCHEMY;
    public static ItemVaultCrystalSeal CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD;


    public static VaultBattleStaffItem BATTLESTAFF;
    //public static VaultBowItem BOW;
    public static VaultTridentItem TRIDENT;
    public static VaultPlushieItem PLUSHIE;
    public static VaultLootSackItem LOOT_SACK;

    //public static VaultAmuletItem VAULT_AMULET;

    public static LootableItem GEM_BOX;
    public static LootableItem SUPPLY_BOX;
    public static LootableItem AUGMENT_BOX;
    public static LootableItem INSCRIPTION_BOX;
    public static LootableItem OMEGA_BOX;
    public static LootableItem CATALYST_BOX;
    public static LootableItem ENIGMA_EGG;
    public static LootableItem VAULTAR_BOX;
    public static LootableItem UNIDENTIFIED_GATEWAY_PEARL;
    public static MultiLootableItem EXQUISITE_BOX;
    public static TargetedModBox TARGETED_MOD_BOX;

    public static BasicScavengerItem BENITOITE_GEMSTONE;
    public static BasicScavengerItem WUTODIC_GEMSTONE;
    public static BasicScavengerItem ECHOING_GEMSTONE;
    public static BasicScavengerItem POGGING_GEMSTONE;
    public static BasicScavengerItem ENDER_HEART;
    public static BasicScavengerItem ENDER_EYE;
    public static BasicScavengerItem ENDER_CRYSTAL;
    public static BasicScavengerItem ENDER_ARTIFACT;

    public static final BasicMobEggItem WOLD_EGG = new BasicMobEggItem(WoldsVaults.id("wold_spawn_egg"), () -> ModEntities.WOLD, 1447446, DyeColor.GRAY.getId(), (new Item.Properties()).tab(VAULT_MOD_GROUP));
    public static final BasicMobEggItem BOOGIEMAN_EGG = new BasicMobEggItem(WoldsVaults.id("boogieman_spawn_egg"), () -> iskallia.vault.init.ModEntities.BOOGIEMAN, 333333, DyeColor.GRAY.getId(), (new Item.Properties()).tab(VAULT_MOD_GROUP));
    public static final BasicMobEggItem MONSTER_EYE_EGG = new BasicMobEggItem(WoldsVaults.id("monster_eye_spawn_egg"), () -> iskallia.vault.init.ModEntities.MONSTER_EYE, 333333, DyeColor.GRAY.getId(), (new Item.Properties()).tab(VAULT_MOD_GROUP));
    public static final BasicMobEggItem ROBOT_EGG = new BasicMobEggItem(WoldsVaults.id("robot_spawn_egg"), () -> iskallia.vault.init.ModEntities.ROBOT, 333333, DyeColor.GRAY.getId(), (new Item.Properties()).tab(VAULT_MOD_GROUP));
    public static final BasicMobEggItem BLUE_BLAZE_EGG = new BasicMobEggItem(WoldsVaults.id("blue_blaze_spawn_egg"), () -> iskallia.vault.init.ModEntities.BLUE_BLAZE, 333333, DyeColor.GRAY.getId(), (new Item.Properties()).tab(VAULT_MOD_GROUP));

    // Alchemy Objective things
    public static final AlchemyIngredientItem ROTTEN_HEART = new AlchemyIngredientItem(WoldsVaults.id("rotten_heart"), AlchemyIngredientItem.AlchemyIngredientType.DEADLY);
    public static final AlchemyIngredientItem ROTTEN_APPLE = new AlchemyIngredientItem(WoldsVaults.id("rotten_apple"), AlchemyIngredientItem.AlchemyIngredientType.RUTHLESS);
    public static final AlchemyIngredientItem VERDANT_GLOBULE = new AlchemyIngredientItem(WoldsVaults.id("verdant_globule"), AlchemyIngredientItem.AlchemyIngredientType.NEUTRAL);
    public static final AlchemyIngredientItem ERRATIC_EMBER = new AlchemyIngredientItem(WoldsVaults.id("erratic_ember"), AlchemyIngredientItem.AlchemyIngredientType.VOLATILE);
    public static final AlchemyIngredientItem REFINED_POWDER = new AlchemyIngredientItem(WoldsVaults.id("refined_powder"), AlchemyIngredientItem.AlchemyIngredientType.REFINED);
    public static final AlchemyIngredientItem AURIC_CRYSTAL = new AlchemyIngredientItem(WoldsVaults.id("auric_crystal"), AlchemyIngredientItem.AlchemyIngredientType.EMPOWERED);
    public static final BasicItem INGREDIENT_TEMPLATE = new BasicItem(WoldsVaults.id("ingredient_template"));
    public static final DecoPotionItem DECO_POTION = new DecoPotionItem(WoldsVaults.id("deco_potion"));
    public static final CatalystItem CATALYST_STABILITY = new CatalystItem(WoldsVaults.id("catalyst_stability"), CatalystItem.CatalystType.STABILIZING);
    public static final CatalystItem CATALYST_AMPLIFYING = new CatalystItem(WoldsVaults.id("catalyst_amplifying"), CatalystItem.CatalystType.AMPLIFYING);
    public static final CatalystItem CATALYST_FOCUSING = new CatalystItem(WoldsVaults.id("catalyst_focusing"), CatalystItem.CatalystType.FOCUSING);
    public static final CatalystItem CATALYST_TEMPORAL = new CatalystItem(WoldsVaults.id("catalyst_temporal"), CatalystItem.CatalystType.TEMPORAL);
    public static final CatalystItem CATALYST_UNSTABLE = new CatalystItem(WoldsVaults.id("catalyst_unstable"), CatalystItem.CatalystType.UNSTABLE);


    public static final AltarResetItem ALTAR_DECATALYZER;

    //Angel Rings
    public static final ChromaticIronAngelRing CHROMATIC_IRON_ANGEL_RING;
    public static final ChromaticGoldAngelRing CHROMATIC_GOLD_ANGEL_RING;
    public static final ChromaticSteelAngelRing CHROMATIC_STEEL_ANGEL_RING;
    public static final BlackChromaticSteelAngelRing BLACK_CHROMATIC_STEEL_ANGEL_RING;
    public static final PrismaticAngelRing PRISMATIC_ANGEL_RING;

    //Orbs
    public static final ExpertiseOrbItem EXPERTISE_ORB_ITEM;
    public static final SkillOrbItem SKILL_ORB_ITEM;

    public static final BasicItem ECCENTRIC_FOCUS;
    public static final BasicItem WEAPON_TYPE_FOCUS;
    public static final BasicItem BLAZING_FOCUS;
    public static final BasicItem SUSPENSION_FOCUS;
   // public static final BasicItem WEAPON_TYPE_SETTER;

    public static BasicItem FRENZY_CAPSTONE;
    public static BasicItem PROSPEROUS_CAPSTONE;
    public static BasicItem ALL_SEEING_EYE_CAPSTONE;
    public static BasicItem ENCHANTED_CAPSTONE;
    public static BasicItem COMMUNITY_TOKEN;
    public static BasicItem RESEARCH_TOKEN;

    public static BasicItem VENDOOR_CAPSTONE;
    public static BasicItem WAXING_AUGMENTER;
    public static BasicItem WANING_AUGMENTER;
    public static BasicItem REPAIR_AUGMENTER;
    public static BasicItem CRYSTAL_REINFORCEMENT;
    public static BasicItem RESONATING_REINFORCEMENT;
    public static BasicItem STYLISH_FOCUS;
    public static BasicItem MERCY_ORB;
    public static BasicItem UBER_CHAOS_CATALYST;
    public static HastyPomegranteItem HASTY_POMEGRANATE;
    public static WisdomFruitItem WISDOM_FRUIT;
    public static ToolModifierNullifyingItem CHISELING_FOCUS;
    public static AirMobilityItem ZEPHYR_CHARM;
    public static VaultBasicFoodItem VAULT_ROCK_CANDY;

    public static BasicItem CHROMATIC_GOLD_NUGGET;
    public static BasicItem CHROMATIC_GOLD_INGOT;
    public static BasicItem SMASHED_VAULT_GEM;
    public static BasicItem SMASHED_VAULT_GEM_CLUSTER;
    public static BasicItem WOLD_STAR_CHUNK;
    public static BasicItem WOLD_STAR;
    public static BasicItem POG_PRISM;
    public static BasicItem VAULT_DECO_SCROLL;
    public static BasicItem ARCANE_ESSENCE;
    public static BasicItem ARCANE_SHARD;
    public static BasicItem EXTRAORDINARY_POG_PRISM;
    public static BasicItem HEART_OF_CHAOS;
    public static BasicItem GEM_REAGENT_ASHIUM;
    public static BasicItem GEM_REAGENT_BOMIGNITE;
    public static BasicItem GEM_REAGENT_UPALINE;
    public static BasicItem GEM_REAGENT_ISKALLIUM;
    public static BasicItem GEM_REAGENT_GORGINITE;
    public static BasicItem GEM_REAGENT_PETEZANITE;
    public static BasicItem GEM_REAGENT_XENIUM;
    public static BasicItem GEM_REAGENT_SPARKLETINE;
    public static BasicItem GEM_REAGENT_TUBIUM;
    public static BasicItem AUGMENT_PIECE;
    public static PoltergeistPlum POLTERGEIST_PLUM;

    //Zealot items
    public static BasicItem TOME_OF_TENOS;
    public static BasicItem VELARA_APPLE;
    public static BasicItem IDONA_DAGGER;
    public static BasicItem WENDARR_GEM;

    //Crystal Modification Items
    public static LayoutModificationItem LAYOUT_MANIPULATOR;

    //Backpack?
    public static BackpackItem XL_BACKPACK;

    public static VaultRangItem RANG;
    public static VaultMapItem MAP;


    public static BasicItem GREEDY_VAULT_ROCK;
    public static BasicItem POGOMINIUM_INGOT;
    public static BasicItem INFUSED_DRIFTWOOD;
    public static BasicItem PRISMATIC_FIBER;
    public static BasicItem SPARK_OF_INSPIRATION;
    public static BasicItem VAULT_DIAMOND_NUGGET;
    public static BasicItem CHUNK_OF_POWER;
    public static BasicItem SOUL_ICHOR;
    public static BasicItem CHROMA_CORE;

    public static RecipeBlueprintItem RECIPE_BLUEPRINT;

    public static GodReputationItem GOD_OFFERING;

    public static DecayingItem RUINED_ESSENCE;
    public static LocatorItem OBELISK_RESONATOR;
    public static BasicItem NULLITE_FRAGMENT;
    public static BasicItem NULLITE_CRYSTAL;

    public static TrinketPouchItem TRINKET_POUCH;

    public static FilterNecklaceItem FILTER_NECKLACE;


    public static CompanionRerollingItem COMPANION_REROLLER;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(CRYSTAL_SEAL_MONOLITH);
        registry.register(CRYSTAL_SEAL_UNHINGED);
        registry.register(CRYSTAL_SEAL_SPIRITS);
        registry.register(CRYSTAL_SEAL_ENCHANTER);
        registry.register(CRYSTAL_SEAL_TITAN);
        registry.register(CRYSTAL_SEAL_DOOMSAYER);
        registry.register(CRYSTAL_SEAL_WARRIOR);
        registry.register(CRYSTAL_SEAL_CORRUPT);
        registry.register(CRYSTAL_SEAL_ALCHEMY);
        registry.register(CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD);
        registry.register(BATTLESTAFF);
        registry.register(TRIDENT);
        registry.register(PLUSHIE);
        registry.register(LOOT_SACK);
        registry.register(GEM_BOX);
        registry.register(SUPPLY_BOX);
        registry.register(AUGMENT_BOX);
        registry.register(INSCRIPTION_BOX);
        registry.register(OMEGA_BOX);
        registry.register(CATALYST_BOX);
        registry.register(ENIGMA_EGG);
        registry.register(BENITOITE_GEMSTONE);
        registry.register(WUTODIC_GEMSTONE);
        registry.register(ECHOING_GEMSTONE);
        registry.register(POGGING_GEMSTONE);
        registry.register(ENDER_HEART);
        registry.register(ENDER_EYE);
        registry.register(ENDER_ARTIFACT);
        registry.register(ENDER_CRYSTAL);
        registry.register(ALTAR_DECATALYZER);
        registry.register(CHROMATIC_IRON_ANGEL_RING);
        registry.register(CHROMATIC_GOLD_ANGEL_RING);
        registry.register(CHROMATIC_STEEL_ANGEL_RING);
        registry.register(BLACK_CHROMATIC_STEEL_ANGEL_RING);
        registry.register(PRISMATIC_ANGEL_RING);
        registry.register(EXPERTISE_ORB_ITEM);
        registry.register(SKILL_ORB_ITEM);
        registry.register(WOLD_EGG);
        registry.register(BOOGIEMAN_EGG);
        registry.register(ROBOT_EGG);
        registry.register(MONSTER_EYE_EGG);
        registry.register(BLUE_BLAZE_EGG);
        registry.register(FRENZY_CAPSTONE);
        registry.register(PROSPEROUS_CAPSTONE);
        registry.register(ALL_SEEING_EYE_CAPSTONE);
        registry.register(ENCHANTED_CAPSTONE);
        registry.register(VENDOOR_CAPSTONE);
        registry.register(WAXING_AUGMENTER);
        registry.register(WANING_AUGMENTER);
        registry.register(REPAIR_AUGMENTER);
        registry.register(CRYSTAL_REINFORCEMENT);
        registry.register(RESONATING_REINFORCEMENT);
        registry.register(CHISELING_FOCUS);
        registry.register(ZEPHYR_CHARM);
        registry.register(STYLISH_FOCUS);
        registry.register(VAULT_ROCK_CANDY);
        registry.register(UBER_CHAOS_CATALYST);
        registry.register(HASTY_POMEGRANATE);
        registry.register(VAULTAR_BOX);
        registry.register(MERCY_ORB);
        registry.register(WISDOM_FRUIT);
        registry.register(CHROMATIC_GOLD_NUGGET);
        registry.register(CHROMATIC_GOLD_INGOT);
        registry.register(SMASHED_VAULT_GEM);
        registry.register(SMASHED_VAULT_GEM_CLUSTER);
        registry.register(WOLD_STAR_CHUNK);
        registry.register(WOLD_STAR);
        registry.register(POG_PRISM);
        registry.register(VAULT_DECO_SCROLL);
        registry.register(ARCANE_ESSENCE);
        registry.register(ARCANE_SHARD);
        registry.register(EXTRAORDINARY_POG_PRISM);
        registry.register(HEART_OF_CHAOS);
        registry.register(GEM_REAGENT_GORGINITE);
        registry.register(GEM_REAGENT_BOMIGNITE);
        registry.register(GEM_REAGENT_ISKALLIUM);
        registry.register(GEM_REAGENT_PETEZANITE);
        registry.register(GEM_REAGENT_TUBIUM);
        registry.register(GEM_REAGENT_ASHIUM);
        registry.register(GEM_REAGENT_XENIUM);
        registry.register(GEM_REAGENT_SPARKLETINE);
        registry.register(GEM_REAGENT_UPALINE);
        registry.register(AUGMENT_PIECE);
        registry.register(IDONA_DAGGER);
        registry.register(WENDARR_GEM);
        registry.register(VELARA_APPLE);
        registry.register(TOME_OF_TENOS);
        registry.register(POLTERGEIST_PLUM);
        registry.register(CRYSTAL_SEAL_ZEALOT);
        registry.register(XL_BACKPACK);
        registry.register(COMMUNITY_TOKEN);
        registry.register(RESEARCH_TOKEN);
        registry.register(ECCENTRIC_FOCUS);
        registry.register(RANG);
        registry.register(MAP);
        registry.register(LAYOUT_MANIPULATOR);
        registry.register(WEAPON_TYPE_FOCUS);
        registry.register(GREEDY_VAULT_ROCK);
        registry.register(POGOMINIUM_INGOT);
        registry.register(INFUSED_DRIFTWOOD);
        registry.register(SPARK_OF_INSPIRATION);
        registry.register(VAULT_DIAMOND_NUGGET);
        registry.register(EXQUISITE_BOX);
        registry.register(CHUNK_OF_POWER);
        registry.register(SOUL_ICHOR);
        registry.register(BLAZING_FOCUS);
        registry.register(SUSPENSION_FOCUS);
        registry.register(TARGETED_MOD_BOX);
        registry.register(UNIDENTIFIED_GATEWAY_PEARL);
        registry.register(TRINKET_POUCH);
        registry.register(RUINED_ESSENCE);
        registry.register(OBELISK_RESONATOR);
        registry.register(NULLITE_FRAGMENT);
        registry.register(NULLITE_CRYSTAL);
        registry.register(GOD_OFFERING);
        registry.register(PRISMATIC_FIBER);
        registry.register(RECIPE_BLUEPRINT);
        registry.register(CHROMA_CORE);
        registry.register(FILTER_NECKLACE);
        registry.register(COMPANION_REROLLER);

        registry.register(ROTTEN_HEART);
        registry.register(ROTTEN_APPLE);
        registry.register(VERDANT_GLOBULE);
        registry.register(ERRATIC_EMBER);
        registry.register(REFINED_POWDER);
        registry.register(AURIC_CRYSTAL);
        registry.register(INGREDIENT_TEMPLATE);
        registry.register(DECO_POTION);
        registry.register(CATALYST_STABILITY);
        registry.register(CATALYST_AMPLIFYING);
        registry.register(CATALYST_FOCUSING);
        registry.register(CATALYST_TEMPORAL);
        registry.register(CATALYST_UNSTABLE);
    }

    static {
        CRYSTAL_SEAL_MONOLITH = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_monolith"));
        CRYSTAL_SEAL_UNHINGED = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_unhinged"));
        CRYSTAL_SEAL_SPIRITS = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_spirits"));
        CRYSTAL_SEAL_ENCHANTER = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_enchanter"));
        CRYSTAL_SEAL_TITAN = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_titan"));
        CRYSTAL_SEAL_DOOMSAYER = new ItemVaultCrystalSeal(VaultMod.id("crystal_seal_doomsayer"));
        CRYSTAL_SEAL_ZEALOT = new ItemVaultCrystalSeal(WoldsVaults.id("crystal_seal_zealot"));
        CRYSTAL_SEAL_WARRIOR = new ItemVaultCrystalSeal(WoldsVaults.id("crystal_seal_warrior"));
        CRYSTAL_SEAL_CORRUPT = new ItemVaultCrystalSeal(WoldsVaults.id("crystal_seal_corrupt"));
        CRYSTAL_SEAL_ALCHEMY = new ItemVaultCrystalSeal(WoldsVaults.id("crystal_seal_alchemy"));
        CRYSTAL_SEAL_RAID_ROCK_INFINITE_HARD = new ItemVaultCrystalSeal(WoldsVaults.id("crystal_seal_raid_rock_infinite_hard"));
        BATTLESTAFF =  new VaultBattleStaffItem(VaultMod.id("battlestaff"), (new Item.Properties()).tab(GEAR_GROUP).stacksTo(1));
        TRIDENT = new VaultTridentItem(VaultMod.id("trident"), (new Item.Properties()).tab(GEAR_GROUP).stacksTo(1));
        PLUSHIE = new VaultPlushieItem(VaultMod.id("plushie"), (new Item.Properties()).tab(GEAR_GROUP).stacksTo(1));
        LOOT_SACK = new VaultLootSackItem(VaultMod.id("loot_sack"), (new Item.Properties()).tab(GEAR_GROUP).stacksTo(1));
        GEM_BOX = new LootableItem(VaultMod.id("gem_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.GEM_BOX.POOL.getRandom(rand).generateItemStack()));
        SUPPLY_BOX = new LootableItem(VaultMod.id("supply_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.SUPPLY_BOX.POOL.getRandom(rand).generateItemStack()));
        AUGMENT_BOX = new LootableItem(VaultMod.id("augment_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.AUGMENT_BOX.POOL.getRandom(rand).generateItemStack()));
        INSCRIPTION_BOX = new LootableItem(WoldsVaults.id("inscription_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.INSCRIPTION_BOX.POOL.getRandom(rand).generateItemStack()));
        OMEGA_BOX = new LootableItem(WoldsVaults.id("omega_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.OMEGA_BOX.POOL.getRandom(rand).generateItemStack()));
        CATALYST_BOX = new LootableItem(WoldsVaults.id("catalyst_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.CATALYST_BOX.POOL.getRandom(rand).generateItemStack()));
        ENIGMA_EGG = new LootableItem(WoldsVaults.id("enigma_egg"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.ENIGMA_EGG.POOL.getRandom(rand).generateItemStack()));
        VAULTAR_BOX = new LootableItem(WoldsVaults.id("vaultar_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.VAULTAR_BOX.POOL.getRandom(rand).generateItemStack()));
        UNIDENTIFIED_GATEWAY_PEARL = new LootableItem(WoldsVaults.id("unidentified_gateway_pearl"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.GATEWAY_PEARL.POOL.getRandom(rand).generateItemStack()));
        EXQUISITE_BOX = new MultiLootableItem(WoldsVaults.id("exquisite_box"), (new Item.Properties()).tab(VAULT_MOD_GROUP), () -> (ModConfigs.EXQUISITE_BOX.POOL.getRandom(rand).generateItemStack()), 3);
        BENITOITE_GEMSTONE = new BasicScavengerItem("benitoite_gemstone");
        WUTODIC_GEMSTONE = new BasicScavengerItem("wutodic_gemstone");
        ECHOING_GEMSTONE = new BasicScavengerItem("echoing_gemstone");
        POGGING_GEMSTONE = new BasicScavengerItem("pogging_gemstone");
        ENDER_HEART = new BasicScavengerItem("ender_heart");
        ENDER_ARTIFACT = new BasicScavengerItem("ender_artifact");
        ENDER_CRYSTAL = new BasicScavengerItem("ender_crystal");
        ENDER_EYE = new BasicScavengerItem("enderman_eye");
        ALTAR_DECATALYZER = new AltarResetItem(VaultMod.id("altar_recatalyzer"), (new Item.Properties().tab(VAULT_MOD_GROUP).rarity(Rarity.RARE)));
        CHROMATIC_IRON_ANGEL_RING = new ChromaticIronAngelRing();
        CHROMATIC_GOLD_ANGEL_RING= new ChromaticGoldAngelRing();
        CHROMATIC_STEEL_ANGEL_RING = new ChromaticSteelAngelRing();
        BLACK_CHROMATIC_STEEL_ANGEL_RING = new BlackChromaticSteelAngelRing();
        PRISMATIC_ANGEL_RING = new PrismaticAngelRing();
        EXPERTISE_ORB_ITEM = new ExpertiseOrbItem(WoldsVaults.id("expertise_orb"));
        SKILL_ORB_ITEM = new SkillOrbItem(WoldsVaults.id("skill_orb"));
        FRENZY_CAPSTONE = new BasicItem(WoldsVaults.id("capstone_frenzy"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        PROSPEROUS_CAPSTONE = new BasicItem(WoldsVaults.id("capstone_prosperous"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        ALL_SEEING_EYE_CAPSTONE = new BasicItem(WoldsVaults.id("capstone_all_seeing_eye"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        ENCHANTED_CAPSTONE = new BasicItem(WoldsVaults.id("capstone_enchanted"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        VENDOOR_CAPSTONE = new BasicItem(WoldsVaults.id("capstone_vendoors"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        WAXING_AUGMENTER = new BasicItem(WoldsVaults.id("waxing_augmenter"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        WANING_AUGMENTER = new BasicItem(WoldsVaults.id("waning_augmenter"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        REPAIR_AUGMENTER = new BasicItem(WoldsVaults.id("repair_augmenter"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        CRYSTAL_REINFORCEMENT = new BasicItem(WoldsVaults.id("crystal_reinforcement"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        RESONATING_REINFORCEMENT = new BasicItem(WoldsVaults.id("resonating_reinforcement"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        STYLISH_FOCUS = new BasicItem(WoldsVaults.id("stylish_focus"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        MERCY_ORB = new BasicItem(WoldsVaults.id("mercy_orb"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        UBER_CHAOS_CATALYST = new BasicItem(WoldsVaults.id("uber_chaos_catalyst"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        HASTY_POMEGRANATE = new HastyPomegranteItem(WoldsVaults.id("hasty_pomegranate"), 1200);
        WISDOM_FRUIT = new WisdomFruitItem(WoldsVaults.id("wisdom_fruit"));
        CHISELING_FOCUS = new ToolModifierNullifyingItem(WoldsVaults.id("chiseling_focus"), (new Item.Properties()).tab(VAULT_MOD_GROUP));
        ZEPHYR_CHARM = new AirMobilityItem(WoldsVaults.id("zephyr_charm"));
        VAULT_ROCK_CANDY = new VaultBasicFoodItem(WoldsVaults.id("vault_rock_candy"), (new FoodProperties.Builder()).alwaysEat().fast().nutrition(8).saturationMod(1.4F).build());
        CHROMATIC_GOLD_NUGGET = new BasicItem(VaultMod.id("chromatic_gold_nugget"));
        CHROMATIC_GOLD_INGOT = new BasicItem(VaultMod.id("chromatic_gold_ingot"));
        SMASHED_VAULT_GEM = new BasicItem(VaultMod.id("smashed_vault_gem"));
        SMASHED_VAULT_GEM_CLUSTER = new BasicItem(VaultMod.id("smashed_vault_gem_cluster"));
        WOLD_STAR_CHUNK = new BasicItem(VaultMod.id("wold_star_chunk"));
        WOLD_STAR = new BasicItem(VaultMod.id("wold_star"));
        POG_PRISM = new BasicItem(VaultMod.id("pog_prism"));
        VAULT_DECO_SCROLL = new BasicItem(WoldsVaults.id("general_decor_scroll"));
        ARCANE_ESSENCE = new BasicItem(WoldsVaults.id("arcane_essence"));
        ARCANE_SHARD = new BasicItem(WoldsVaults.id("arcane_shard"));
        EXTRAORDINARY_POG_PRISM = new BasicItem(WoldsVaults.id("extraordinary_pog_prism"));
        HEART_OF_CHAOS = new BasicItem(WoldsVaults.id("heart_of_chaos"));
        GEM_REAGENT_ASHIUM = new BasicItem(VaultMod.id("gem_reagent_ashium"));
        GEM_REAGENT_BOMIGNITE = new BasicItem(VaultMod.id("gem_reagent_bomignite"));
        GEM_REAGENT_GORGINITE = new BasicItem(VaultMod.id("gem_reagent_gorginite"));
        GEM_REAGENT_PETEZANITE = new BasicItem(VaultMod.id("gem_reagent_petzanite"));
        GEM_REAGENT_ISKALLIUM = new BasicItem(VaultMod.id("gem_reagent_iskallium"));
        GEM_REAGENT_TUBIUM = new BasicItem(VaultMod.id("gem_reagent_tubium"));
        GEM_REAGENT_UPALINE = new BasicItem(VaultMod.id("gem_reagent_upaline"));
        GEM_REAGENT_XENIUM = new BasicItem(VaultMod.id("gem_reagent_xenium"));
        GEM_REAGENT_SPARKLETINE = new BasicItem(VaultMod.id("gem_reagent_sparkletine"));
        AUGMENT_PIECE = new BasicItem(WoldsVaults.id("augment_piece"));
        TOME_OF_TENOS = new BasicItem(WoldsVaults.id("tome_of_tenos"));
        IDONA_DAGGER = new BasicItem(WoldsVaults.id("idona_dagger"));
        VELARA_APPLE = new BasicItem(WoldsVaults.id("velara_apple"));
        WENDARR_GEM = new BasicItem(WoldsVaults.id("wendarr_gem"));
        ECCENTRIC_FOCUS = new BasicItem(WoldsVaults.id("eccentric_focus"));
        COMMUNITY_TOKEN = new BasicItem(WoldsVaults.id("community_token"));
        GREEDY_VAULT_ROCK = new BasicItem(WoldsVaults.id("greedy_vault_rock"));
        POGOMINIUM_INGOT = new BasicItem(WoldsVaults.id("pogominium_ingot"));
        INFUSED_DRIFTWOOD = new BasicItem(WoldsVaults.id("infused_driftwood"));
        SPARK_OF_INSPIRATION = new BasicItem(WoldsVaults.id("spark_of_inspiration"));
        VAULT_DIAMOND_NUGGET = new BasicItem(WoldsVaults.id("vault_diamond_nugget"));
        CHUNK_OF_POWER = new BasicItem(WoldsVaults.id("chunk_of_power"));
        SOUL_ICHOR = new BasicItem(WoldsVaults.id("soul_ichor"));
        PRISMATIC_FIBER = new BasicItem(WoldsVaults.id("prismatic_fiber"));
        RECIPE_BLUEPRINT = new RecipeBlueprintItem(WoldsVaults.id("recipe_blueprint"), new Item.Properties().tab(VAULT_MOD_GROUP));
        RESEARCH_TOKEN = new ResearchTokenItem(WoldsVaults.id("research_token"), new Item.Properties().stacksTo(1));
        POLTERGEIST_PLUM = new PoltergeistPlum(WoldsVaults.id("poltergeist_plum"));
        XL_BACKPACK = new BackpackItem(() -> 180, () -> 8, () -> ModBlocks.XL_BACKPACK, Item.Properties::fireResistant);
        XL_BACKPACK.setRegistryName(WoldsVaults.MOD_ID, "xl_backpack");
        RANG = new VaultRangItem(VaultMod.id("rang"), new Item.Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
        MAP = new VaultMapItem(VaultMod.id("map"), (new Item.Properties()).tab(GEAR_GROUP).stacksTo(1));
        LAYOUT_MANIPULATOR = new LayoutModificationItem(VAULT_MOD_GROUP, WoldsVaults.id("layout_manipulator"));
        WEAPON_TYPE_FOCUS = new BasicItem(WoldsVaults.id("accoutre_focus"));
        BLAZING_FOCUS = new BasicItem(WoldsVaults.id("blazing_focus"));
        SUSPENSION_FOCUS = new BasicItem(WoldsVaults.id("suspension_focus"));
        CHROMA_CORE = new BasicItem(WoldsVaults.id("chroma_core"));
        TARGETED_MOD_BOX = new TargetedModBox(WoldsVaults.id("targeted_mod_box"));
        TRINKET_POUCH = new TrinketPouchItem(WoldsVaults.id("trinket_pouch"));
        RUINED_ESSENCE = new DecayingItem(WoldsVaults.id("ruined_essence"), 300);
        OBELISK_RESONATOR = new LocatorItem(WoldsVaults.id("obelisk_resonator"), ModBlocks.FRACTURED_OBELISK, 64);
        NULLITE_FRAGMENT = new BasicItem(WoldsVaults.id("nullite_fragment"));
        NULLITE_CRYSTAL = new BasicItem(WoldsVaults.id("nullite_crystal"));
        GOD_OFFERING = new GodReputationItem(WoldsVaults.id("god_offering"));
        FILTER_NECKLACE = new FilterNecklaceItem(WoldsVaults.id("filter_necklace"), 9);
        COMPANION_REROLLER = new CompanionRerollingItem(WoldsVaults.id("companion_reroller"));
        //WEAPON_TYPE_SETTER = new WeaponTypeSettingItem(WoldsVaults.id("weapon_augmenter"), new Item.Properties().tab(VAULT_MOD_GROUP));
    }
}
