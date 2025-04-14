package xyz.iwolfking.woldsvaults.config.recipes.mod_box;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.config.recipe.ForgeRecipesConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModBoxRecipesConfig extends ForgeRecipesConfig<ConfigModBoxRecipe, ModBoxForgeRecipe>
{

    @Expose
    private final List<ConfigModBoxRecipe> modBoxRecipes = new ArrayList<>();

    public ModBoxRecipesConfig() {
        super(ForgeRecipeType.valueOf("MOD_BOX"));
    }

    protected void reset() {
        for(String key : ModConfigs.MOD_BOX.POOL.keySet()) {
            if(key.equals("None")) {
                continue;
            }
            ConfigModBoxRecipe recipe = new ConfigModBoxRecipe(new ResourceLocation("woldsvaults", key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ENGLISH)), key);
            recipe.addInput(new ItemStack(ModItems.MOD_BOX));
            recipe.addInput(new ItemStack(ModItems.MYSTICAL_POWDER));
            modBoxRecipes.add(recipe);
        }
    }

    public List<ConfigModBoxRecipe> getConfigRecipes() {
        return this.modBoxRecipes;
    }
}
