package xyz.iwolfking.woldsvaults.integration.jei;

import iskallia.vault.init.ModConfigs;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.lib.GenericLootableConfig;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.integration.jei.category.*;
import xyz.iwolfking.woldsvaults.integration.jei.category.lib.GenericLootableBoxCategory;

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

    public WoldsVaultsJeiPlugin() {}
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return WoldsVaults.id("wolds_jei_integration");
    }

    @Override
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
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.CATALYST_BOX, new TextComponent("Catalyst Box"), ModItems.CATALYST_BOX, CATALYST_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.VAULTAR_BOX, new TextComponent("Vaultar Box"), ModItems.VAULTAR_BOX, VAULTAR_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.INSCRIPTION_BOX, new TextComponent("Inscription Box"), ModItems.INSCRIPTION_BOX, INSCRIPTION_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.AUGMENT_BOX, new TextComponent("Augment Box"), ModItems.AUGMENT_BOX, AUGMENT_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.SUPPLY_BOX, new TextComponent("Supply Box"), ModItems.SUPPLY_BOX, SUPPLY_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.GEM_BOX, new TextComponent("Gem Box"), ModItems.GEM_BOX, GEM_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.OMEGA_BOX, new TextComponent("Omega Box"), ModItems.OMEGA_BOX, OMEGA_BOX));
        registration.addRecipeCategories(new GenericLootableBoxCategory(registration.getJeiHelpers().getGuiHelper(), xyz.iwolfking.woldsvaults.init.ModConfigs.ENIGMA_EGG, new TextComponent("Enigma Egg"), ModItems.ENIGMA_EGG, ENIGMA_EGG));
        registration.addRecipeCategories(new InfuserCraftingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;

        if (world != null) {
            var manager = world.getRecipeManager();
            registration.addRecipes(manager.byType(ModRecipeTypes.INFUSER).values(), InfuserCraftingCategory.UID);
        }

        registration.addRecipes(ENIGMA_EGG, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.ENIGMA_EGG));
        registration.addRecipes(OMEGA_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.OMEGA_BOX));
        registration.addRecipes(GEM_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.GEM_BOX));
        registration.addRecipes(SUPPLY_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.SUPPLY_BOX));
        registration.addRecipes(AUGMENT_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.AUGMENT_BOX));
        registration.addRecipes(INSCRIPTION_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.INSCRIPTION_BOX));
        registration.addRecipes(CATALYST_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.CATALYST_BOX));
        registration.addRecipes(VAULTAR_BOX, List.of(xyz.iwolfking.woldsvaults.init.ModConfigs.VAULTAR_BOX));

    }
}
