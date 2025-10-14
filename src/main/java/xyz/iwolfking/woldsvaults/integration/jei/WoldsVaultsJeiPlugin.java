package xyz.iwolfking.woldsvaults.integration.jei;

import dev.attackeight.just_enough_vh.jei.ForgeItem;
import dev.attackeight.just_enough_vh.jei.JEIRecipeProvider;
import dev.attackeight.just_enough_vh.jei.RecyclerRecipe;
import dev.attackeight.just_enough_vh.jei.TheVaultJEIPlugin;
import dev.attackeight.just_enough_vh.jei.category.ForgeItemRecipeCategory;
import iskallia.vault.config.VaultRecyclerConfig;
import iskallia.vault.config.entry.recipe.ConfigForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;
import xyz.iwolfking.woldsvaults.config.lib.GenericShopPedestalConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.integration.jei.category.*;
import xyz.iwolfking.woldsvaults.integration.jei.category.lib.GenericLootableBoxCategory;
import xyz.iwolfking.woldsvaults.integration.jei.category.lib.ShopTierCategory;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ShopTierAccessor;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@SuppressWarnings("unused")
public class WoldsVaultsJeiPlugin implements IModPlugin {

    public static final RecipeType<GenericLootableConfig> CATALYST_BOX = RecipeType.create(WoldsVaults.MOD_ID, "catalyst_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> INSCRIPTION_BOX = RecipeType.create(WoldsVaults.MOD_ID, "inscription_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> AUGMENT_BOX = RecipeType.create(WoldsVaults.MOD_ID, "augment_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> SUPPLY_BOX = RecipeType.create(WoldsVaults.MOD_ID, "supply_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> GEM_BOX = RecipeType.create(WoldsVaults.MOD_ID, "gem_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> OMEGA_BOX = RecipeType.create(WoldsVaults.MOD_ID, "omega_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> ENIGMA_EGG = RecipeType.create(WoldsVaults.MOD_ID, "enigma_egg", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> VAULTAR_BOX = RecipeType.create(WoldsVaults.MOD_ID, "vaultar_box", GenericLootableConfig.class);
    public static final RecipeType<GenericLootableConfig> GATEWAY_PEARL = RecipeType.create(WoldsVaults.MOD_ID, "gateway_pearl", GenericLootableConfig.class);

    public static final RecipeType<ShopTierAccessor> ETCHING_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "etching_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> GOD_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "god_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> BLACKSMITH_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "blacksmith_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> RARE_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "rare_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> OMEGA_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "omega_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> SPOOKY_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "spooky_shop_pedestal", ShopTierAccessor.class);
    public static final RecipeType<ShopTierAccessor> CARD_SHOP_PEDESTAL = RecipeType.create(WoldsVaults.MOD_ID, "card_shop_pedestal", ShopTierAccessor.class);

    // requires vault jei mod
    public static final RecipeType<ForgeItem> MOD_BOX_WORKSTATION = RecipeType.create(WoldsVaults.MOD_ID, "mod_box_workstation", ForgeItem.class);
    public static final RecipeType<ForgeItem> AUGMENTS_ASSEMBLY = RecipeType.create(WoldsVaults.MOD_ID, "augment_assembly", ForgeItem.class);
    public static final RecipeType<ForgeItem> WEAVING = RecipeType.create(WoldsVaults.MOD_ID, "weaving", ForgeItem.class);

    public WoldsVaultsJeiPlugin() {}
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return WoldsVaults.id("wolds_jei_integration");
    }

    @Override @SuppressWarnings("removal")
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.ENIGMA_EGG), ENIGMA_EGG);
        registration.addRecipeCatalyst(new ItemStack(ModItems.OMEGA_BOX), OMEGA_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GEM_BOX), GEM_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.SUPPLY_BOX), SUPPLY_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.AUGMENT_BOX), AUGMENT_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.INSCRIPTION_BOX), INSCRIPTION_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CATALYST_BOX), CATALYST_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModItems.VAULTAR_BOX), VAULTAR_BOX);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VAULT_INFUSER_BLOCK), InfuserCraftingCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHROMATIC_STEEL_INFUSER_BLOCK), InfuserCraftingCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(ModItems.UNIDENTIFIED_GATEWAY_PEARL), GATEWAY_PEARL);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ETCHING_PEDESTAL), ETCHING_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GOD_VENDOR_PEDESTAL), GOD_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLACKSMITH_VENDOR_PEDESTAL), BLACKSMITH_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RARE_VENDOR_PEDESTAL), RARE_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.OMEGA_VENDOR_PEDESTAL), OMEGA_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPOOKY_VENDOR_PEDESTAL), SPOOKY_SHOP_PEDESTAL);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CARD_VENDOR_PEDESTAL), CARD_SHOP_PEDESTAL);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VAULT_SALVAGER_BLOCK), TheVaultJEIPlugin.VAULT_RECYCLER);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MOD_BOX_WORKSTATION), MOD_BOX_WORKSTATION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.AUGMENT_CRAFTING_TABLE), AUGMENTS_ASSEMBLY);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WEAVING_STATION), WEAVING);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.CATALYST_BOX, new TextComponent("Catalyst Box"), ModItems.CATALYST_BOX, CATALYST_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.VAULTAR_BOX, new TextComponent("Vaultar Box"), ModItems.VAULTAR_BOX, VAULTAR_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.INSCRIPTION_BOX, new TextComponent("Inscription Box"), ModItems.INSCRIPTION_BOX, INSCRIPTION_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.AUGMENT_BOX, new TextComponent("Augment Box"), ModItems.AUGMENT_BOX, AUGMENT_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.SUPPLY_BOX, new TextComponent("Supply Box"), ModItems.SUPPLY_BOX, SUPPLY_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.GEM_BOX, new TextComponent("Gem Box"), ModItems.GEM_BOX, GEM_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.OMEGA_BOX, new TextComponent("Omega Box"), ModItems.OMEGA_BOX, OMEGA_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.ENIGMA_EGG, new TextComponent("Enigma Egg"), ModItems.ENIGMA_EGG, ENIGMA_EGG));
        registration.addRecipeCategories(new InfuserCraftingCategory(guiHelper));
        registration.addRecipeCategories(new GenericLootableBoxCategory(guiHelper, ModConfigs.GATEWAY_PEARL, new TextComponent("Gateway Pearl"), ModItems.UNIDENTIFIED_GATEWAY_PEARL, GATEWAY_PEARL));

        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Etching Shop Pedestal"), ModBlocks.ETCHING_PEDESTAL.asItem(), ETCHING_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("God Shop Pedestal"), ModBlocks.GOD_VENDOR_PEDESTAL.asItem(), GOD_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Blacksmith Shop Pedestal"), ModBlocks.BLACKSMITH_VENDOR_PEDESTAL.asItem(), BLACKSMITH_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Rare Shop Pedestal"), ModBlocks.RARE_VENDOR_PEDESTAL.asItem(), RARE_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Omega Shop Pedestal"), ModBlocks.OMEGA_VENDOR_PEDESTAL.asItem(), OMEGA_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Spooky Shop Pedestal"), ModBlocks.SPOOKY_VENDOR_PEDESTAL.asItem(), SPOOKY_SHOP_PEDESTAL));
        registration.addRecipeCategories(new ShopTierCategory(guiHelper, new TextComponent("Card Shop Pedestal"), ModBlocks.CARD_VENDOR_PEDESTAL.asItem(), CARD_SHOP_PEDESTAL));

        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, MOD_BOX_WORKSTATION, new ItemStack(ModBlocks.MOD_BOX_WORKSTATION.asItem())));
        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, AUGMENTS_ASSEMBLY, new ItemStack(ModBlocks.AUGMENT_CRAFTING_TABLE.asItem())));
        registration.addRecipeCategories(new ForgeItemRecipeCategory(guiHelper, WEAVING, new ItemStack(ModBlocks.WEAVING_STATION.asItem())));
    }

    @Override @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;

        if (world != null) {
            var manager = world.getRecipeManager();
            registration.addRecipes(manager.byType(ModRecipeTypes.INFUSER).values(), InfuserCraftingCategory.UID);
        }

        registration.addRecipes(ENIGMA_EGG, List.of(ModConfigs.ENIGMA_EGG));
        registration.addRecipes(OMEGA_BOX, List.of(ModConfigs.OMEGA_BOX));
        registration.addRecipes(GEM_BOX, List.of(ModConfigs.GEM_BOX));
        registration.addRecipes(SUPPLY_BOX, List.of(ModConfigs.SUPPLY_BOX));
        registration.addRecipes(AUGMENT_BOX, List.of(ModConfigs.AUGMENT_BOX));
        registration.addRecipes(INSCRIPTION_BOX, List.of(ModConfigs.INSCRIPTION_BOX));
        registration.addRecipes(CATALYST_BOX, List.of(ModConfigs.CATALYST_BOX));
        registration.addRecipes(VAULTAR_BOX, List.of(ModConfigs.VAULTAR_BOX));
        registration.addRecipes(GATEWAY_PEARL, List.of(ModConfigs.GATEWAY_PEARL));

        registerShopPedestalRecipes(registration, ModConfigs.ETCHING_SHOP_PEDESTAL, ETCHING_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.GOD_SHOP_PEDESTAL, GOD_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.BLACKSMITH_SHOP_PEDESTAL, BLACKSMITH_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.RARE_SHOP_PEDESTAL, RARE_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.OMEGA_SHOP_PEDESTAL, OMEGA_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.SPOOKY_SHOP_PEDESTAL, SPOOKY_SHOP_PEDESTAL);
        registerShopPedestalRecipes(registration, ModConfigs.CARD_SHOP_PEDESTAL, CARD_SHOP_PEDESTAL);

        registration.addRecipes(MOD_BOX_WORKSTATION, getForgeRecipes(ModConfigs.MOD_BOX_RECIPES_CONFIG.getConfigRecipes()));
        registration.addRecipes(AUGMENTS_ASSEMBLY, getForgeRecipes(ModConfigs.AUGMENT_RECIPES.getConfigRecipes()));
        registration.addRecipes(WEAVING, getForgeRecipes(ModConfigs.WEAVING_RECIPES_CONFIG.getConfigRecipes()));
        addCustomRecyclerRecipes(registration);
    }


    private void registerShopPedestalRecipes(IRecipeRegistration registration, GenericShopPedestalConfig configInstance, RecipeType<ShopTierAccessor> recipeType) {
        for (Object tier : configInstance.LEVELS) {
            var shopTier = (ShopTierAccessor) tier;
            registration.addRecipes(recipeType, List.of(shopTier));
        }
    }

    private static <R extends VaultForgeRecipe, T extends ConfigForgeRecipe<R>> List<ForgeItem> getForgeRecipes(List<T> configRecipes) {
        List<ForgeItem> recipes = new ArrayList<>();
        configRecipes.forEach(b -> recipes.add(new ForgeItem(b.makeRecipe().getInputs(), b.makeRecipe().getDisplayOutput(100))));
        return recipes;
    }

    private static void addCustomRecyclerRecipes(IRecipeRegistration registration) {
        List<RecyclerRecipe> recipes = new ArrayList<>();
        for (var rec : ModConfigs.CUSTOM_RECYCLER_CONFIG.getOutputs().entrySet()) {
            recipes.add(JEIRecipeProvider.getRecyclerRecipe(new ItemStack(Registry.ITEM.get(rec.getKey())), rec.getValue()));
        }
        registration.addRecipes(TheVaultJEIPlugin.VAULT_RECYCLER, recipes);
    }
}
