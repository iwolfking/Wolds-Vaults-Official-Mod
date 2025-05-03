package xyz.iwolfking.woldsvaults.config.recipes.weaving;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.config.recipe.ForgeRecipesConfig;
import iskallia.vault.init.ModConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModItems;
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
        weavingRecipes.add(new ConfigWeavingRecipe(TrinketPouchItem.create(WoldsVaults.id("basic_vanilla"))));
    }

    public List<ConfigWeavingRecipe> getConfigRecipes() {
        return this.weavingRecipes;
    }
}
