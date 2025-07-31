package xyz.iwolfking.woldsvaults.init;

import org.checkerframework.checker.units.qual.A;
import xyz.iwolfking.vhapi.api.data.api.CustomRecyclerOutputs;
import xyz.iwolfking.vhapi.api.loaders.workstation.lib.CustomVaultRecyclerConfig;
import xyz.iwolfking.woldsvaults.config.*;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;
import xyz.iwolfking.woldsvaults.config.lib.GenericShopPedestalConfig;
import xyz.iwolfking.woldsvaults.config.recipes.augment.AugmentRecipesConfig;
import xyz.iwolfking.woldsvaults.config.recipes.mod_box.ModBoxForgeRecipe;
import xyz.iwolfking.woldsvaults.config.recipes.mod_box.ModBoxRecipesConfig;
import xyz.iwolfking.woldsvaults.config.recipes.weaving.WeavingRecipesConfig;

public class ModConfigs {
    public static GenericLootableConfig GEM_BOX;
    public static GenericLootableConfig SUPPLY_BOX;
    public static GenericLootableConfig AUGMENT_BOX;
    public static GenericLootableConfig INSCRIPTION_BOX;
    public static GenericLootableConfig OMEGA_BOX;
    public static GenericLootableConfig CATALYST_BOX;
    public static GenericLootableConfig ENIGMA_EGG;
    public static GenericLootableConfig VAULTAR_BOX;
    public static GenericLootableConfig GATEWAY_PEARL;
    public static GenericLootableConfig EXQUISITE_BOX;

    public static UnhingedScavengerConfig UNHINGED_SCAVENGER;
    public static BallisticBingoConfig BALLISTIC_BINGO_CONFIG;
    public static HauntedBraziersConfig HAUNTED_BRAZIERS;
    public static EnchantedElixirConfig ENCHANTED_ELIXIR;
    public static CorruptedObjectiveConfig CORRUPTED_OBJECTIVE;
    public static AlchemyObjectiveConfig ALCHEMY_OBJECTIVE;

    public static AugmentRecipesConfig AUGMENT_RECIPES;
    public static ModBoxRecipesConfig MOD_BOX_RECIPES_CONFIG;
    public static WeavingRecipesConfig WEAVING_RECIPES_CONFIG;

    public static ThemeTooltipsConfig THEME_TOOLTIPS;

    public static CustomVaultRecyclerConfig CUSTOM_RECYCLER_CONFIG;

    public static EternalAttributesConfig ETERNAL_ATTRIBUTES;

    public static GenericShopPedestalConfig ETCHING_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig GOD_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig BLACKSMITH_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig RARE_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig OMEGA_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig SPOOKY_SHOP_PEDESTAL;
    public static GenericShopPedestalConfig CARD_SHOP_PEDESTAL;

    public static GreedVaultAltarIngredientsConfig GREED_VAULT_ALTAR_INGREDIENTS;
    public static WeaponTypesConfig WEAPON_TYPES;
    public static VaultGearRarityColorConfig VAULT_GEAR_RARITY_COLOR_CONFIG = new VaultGearRarityColorConfig();
    public static TrinketPouchConfig TRINKET_POUCH = new TrinketPouchConfig();
    public static RecipeUnlocksConfig RECIPE_UNLOCKS = new RecipeUnlocksConfig();
    public static ResearchExclusionConfig RESEARCH_EXCLUSIONS = new ResearchExclusionConfig();

    public static void preregister() {
        VAULT_GEAR_RARITY_COLOR_CONFIG = new VaultGearRarityColorConfig().readConfig();
    }

    public static void register() {
        GEM_BOX = new GenericLootableConfig("gem_box").readConfig();
        SUPPLY_BOX = new GenericLootableConfig("supply_box").readConfig();
        AUGMENT_BOX = new GenericLootableConfig("augment_box").readConfig();
        INSCRIPTION_BOX = new GenericLootableConfig("inscription_box").readConfig();
        OMEGA_BOX = new GenericLootableConfig("omega_box").readConfig();
        CATALYST_BOX = new GenericLootableConfig("catalyst_box").readConfig();
        ENIGMA_EGG = new GenericLootableConfig("enigma_egg").readConfig();
        VAULTAR_BOX = new GenericLootableConfig("vaultar_box").readConfig();
        GATEWAY_PEARL = new GenericLootableConfig("gateway_pearl").readConfig();
        EXQUISITE_BOX = new GenericLootableConfig("exquisite_box").readConfig();
        UNHINGED_SCAVENGER = new UnhingedScavengerConfig().readConfig();
        BALLISTIC_BINGO_CONFIG = new BallisticBingoConfig().readConfig();
        HAUNTED_BRAZIERS = new HauntedBraziersConfig().readConfig();
        ENCHANTED_ELIXIR = new EnchantedElixirConfig().readConfig();
        CORRUPTED_OBJECTIVE = new CorruptedObjectiveConfig().readConfig();
        ALCHEMY_OBJECTIVE = new AlchemyObjectiveConfig().readConfig();
        AUGMENT_RECIPES = new AugmentRecipesConfig().readConfig();
        MOD_BOX_RECIPES_CONFIG = new ModBoxRecipesConfig().readConfig();
        THEME_TOOLTIPS = new ThemeTooltipsConfig().readConfig();
        CUSTOM_RECYCLER_CONFIG = new CustomVaultRecyclerConfig().readConfig();
        CustomRecyclerOutputs.CUSTOM_OUTPUTS.putAll(CUSTOM_RECYCLER_CONFIG.getOutputs());
        ETERNAL_ATTRIBUTES = new EternalAttributesConfig().readConfig();
        GREED_VAULT_ALTAR_INGREDIENTS = new GreedVaultAltarIngredientsConfig().readConfig();
        WEAPON_TYPES = new WeaponTypesConfig().readConfig();
        ETCHING_SHOP_PEDESTAL = new GenericShopPedestalConfig("etching_shop_pedestal").readConfig();
        GOD_SHOP_PEDESTAL = new GenericShopPedestalConfig("god_shop_pedestal").readConfig();
        BLACKSMITH_SHOP_PEDESTAL = new GenericShopPedestalConfig("blacksmith_shop_pedestal").readConfig();
        RARE_SHOP_PEDESTAL = new GenericShopPedestalConfig("rare_shop_pedestal").readConfig();
        OMEGA_SHOP_PEDESTAL = new GenericShopPedestalConfig("omega_shop_pedestal").readConfig();
        SPOOKY_SHOP_PEDESTAL = new GenericShopPedestalConfig("spooky_shop_pedestal").readConfig();
        CARD_SHOP_PEDESTAL = new GenericShopPedestalConfig("card_shop_pedestal").readConfig();
        TRINKET_POUCH = new TrinketPouchConfig().readConfig();
        RECIPE_UNLOCKS = new RecipeUnlocksConfig().readConfig();
        WEAVING_RECIPES_CONFIG = new WeavingRecipesConfig().readConfig();
        RESEARCH_EXCLUSIONS = new ResearchExclusionConfig().readConfig();
    }
}
