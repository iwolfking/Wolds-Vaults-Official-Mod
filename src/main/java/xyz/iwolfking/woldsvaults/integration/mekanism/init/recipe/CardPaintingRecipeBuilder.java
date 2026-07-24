package xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe;

import com.google.gson.JsonObject;
import iskallia.vault.core.card.CardEntry;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.MekanismRecipeDeserializers;

import java.util.function.Consumer;

public class CardPaintingRecipeBuilder {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient.PigmentStackIngredient pigmentInput;
    private final ItemStack output;
    private final CardEntry.Color targetColor;

    private CardPaintingRecipeBuilder(ItemStackIngredient itemInput, 
                                      ChemicalStackIngredient.PigmentStackIngredient pigmentInput, 
                                      ItemStack output, 
                                      CardEntry.Color targetColor) {
        this.itemInput = itemInput;
        this.pigmentInput = pigmentInput;
        this.output = output;
        this.targetColor = targetColor;
    }

    public static CardPaintingRecipeBuilder cardPainting(Item inputCard, Pigment pigment, long pigmentAmount, CardEntry.Color targetColor) {
        return new CardPaintingRecipeBuilder(
                ItemStackIngredientCreator.INSTANCE.from(inputCard),
                PigmentStackIngredientCreator.INSTANCE.from(pigment, pigmentAmount),
                new ItemStack(inputCard),
                targetColor
        );
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.itemInput, this.pigmentInput, this.output, this.targetColor));
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ItemStackIngredient itemInput;
        private final ChemicalStackIngredient.PigmentStackIngredient pigmentInput;
        private final ItemStack output;
        private final CardEntry.Color targetColor;

        public Result(ResourceLocation id, 
                      ItemStackIngredient itemInput, 
                      ChemicalStackIngredient.PigmentStackIngredient pigmentInput, 
                      ItemStack output, 
                      CardEntry.Color targetColor) {
            this.id = id;
            this.itemInput = itemInput;
            this.pigmentInput = pigmentInput;
            this.output = output;
            this.targetColor = targetColor;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("itemInput", this.itemInput.serialize());

            json.add("chemicalInput", this.pigmentInput.serialize());

            JsonObject outputJson = new JsonObject();
            outputJson.addProperty("item", this.output.getItem().getRegistryName().toString());
            if (this.output.getCount() > 1) {
                outputJson.addProperty("count", this.output.getCount());
            }
            json.add("output", outputJson);

            json.addProperty("targetColor", this.targetColor.name());
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return MekanismRecipeDeserializers.CARD_PAINTING.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}