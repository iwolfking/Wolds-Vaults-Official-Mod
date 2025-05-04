package xyz.iwolfking.woldsvaults.config.recipes.weaving;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.config.recipe.ForgeRecipesConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeavingRecipesConfig extends ForgeRecipesConfig<ConfigWeavingRecipe, WeavingForgeRecipe>
{

    @Expose
    private final List<ConfigWeavingRecipe> weavingRecipes = new ArrayList<>();

    public WeavingRecipesConfig() {
        super(ForgeRecipeType.valueOf("WEAVING"));
    }

    protected void reset() {
        ConfigWeavingRecipe basicPouch1 = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("basic_vanilla")));
        basicPouch1.addInput(new ItemStack(ModItems.MAGIC_SILK, 9));
        basicPouch1.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 3));
        basicPouch1.addInput(new ItemStack(ModItems.BENITOITE_GEM, 1));
        basicPouch1.addInput(new ItemStack(ModItems.PAINITE_GEM, 1));
        weavingRecipes.add(basicPouch1);

        ConfigWeavingRecipe basicPouch2 = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("basic_alt_r")));
        basicPouch2.addInput(new ItemStack(ModItems.MAGIC_SILK, 9));
        basicPouch2.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 3));
        basicPouch2.addInput(new ItemStack(ModItems.PAINITE_GEM, 1));
        basicPouch2.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 1));
        weavingRecipes.add(basicPouch2);

        ConfigWeavingRecipe basicPouch3 = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("basic_alt_g")));
        basicPouch3.addInput(new ItemStack(ModItems.MAGIC_SILK, 9));
        basicPouch3.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 3));
        basicPouch3.addInput(new ItemStack(ModItems.BENITOITE_GEM, 1));
        basicPouch3.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 1));
        weavingRecipes.add(basicPouch3);

        ConfigWeavingRecipe standardPouch = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("standard")));
        standardPouch.addInput(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 4));
        standardPouch.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 9));
        standardPouch.addInput(new ItemStack(ModItems.BENITOITE_GEM, 32));
        standardPouch.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 32));
        standardPouch.addInput(new ItemStack(ModItems.PAINITE_GEM, 32));
        weavingRecipes.add(standardPouch);

        ConfigWeavingRecipe explorer = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("explorer")));
        explorer.addInput(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 4));
        explorer.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 32));
        explorer.addInput(new ItemStack(ModItems.BENITOITE_GEM, 48));
        explorer.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 96));
        weavingRecipes.add(explorer);

        ConfigWeavingRecipe light = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("light")));
        light.addInput(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 4));
        light.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 32));
        light.addInput(new ItemStack(ModItems.BENITOITE_GEM, 96));
        light.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 48));
        weavingRecipes.add(light);

        ConfigWeavingRecipe heavy = new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("light")));
        heavy.addInput(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 4));
        heavy.addInput(new ItemStack(ModItems.VAULT_DIAMOND, 32));
        heavy.addInput(new ItemStack(ModItems.PAINITE_GEM, 96));
        heavy.addInput(new ItemStack(ModItems.ALEXANDRITE_GEM, 48));
        weavingRecipes.add(heavy);

    }

    public List<ConfigWeavingRecipe> getConfigRecipes() {
        return this.weavingRecipes;
    }
}
