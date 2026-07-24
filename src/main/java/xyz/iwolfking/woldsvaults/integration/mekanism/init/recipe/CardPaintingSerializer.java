package xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.CardEntry;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class CardPaintingSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CardPaintingRecipe> {

    @Override
    public CardPaintingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ItemStackIngredient itemInput = ItemStackIngredientCreator.INSTANCE.deserialize(json.get("itemInput"));
        ChemicalStackIngredient.PigmentStackIngredient pigmentInput =
                PigmentStackIngredientCreator.INSTANCE.deserialize(json.get("chemicalInput"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);

        String colorName = GsonHelper.getAsString(json, "targetColor", "RED");
        CardEntry.Color color = CardEntry.Color.valueOf(colorName.toUpperCase());

        return new CardPaintingRecipe(recipeId, itemInput, pigmentInput, output, color);
    }

    @Nullable
    @Override
    public CardPaintingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ItemStackIngredient itemInput = ItemStackIngredientCreator.INSTANCE.read(buffer);
        ChemicalStackIngredient.PigmentStackIngredient pigmentInput = 
                PigmentStackIngredientCreator.INSTANCE.read(buffer);
        ItemStack output = buffer.readItem();
        CardEntry.Color color = buffer.readEnum(CardEntry.Color.class);

        return new CardPaintingRecipe(recipeId, itemInput, pigmentInput, output, color);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CardPaintingRecipe recipe) {
        recipe.getItemInput().write(buffer);
        recipe.getChemicalInput().write(buffer);
        buffer.writeItem(recipe.getResultItem());
        buffer.writeEnum(recipe.getTargetColor());
    }
}