package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VaultAugmentInfusionRecipeBuilder {
    private final List<Ingredient> pedestalItems = new ArrayList<>();
    private int sourceCost = 0;

    private VaultAugmentInfusionRecipeBuilder() {}

    public static VaultAugmentInfusionRecipeBuilder builder() {
        return new VaultAugmentInfusionRecipeBuilder();
    }

    public VaultAugmentInfusionRecipeBuilder addPedestalItem(Item item) {
        this.pedestalItems.add(Ingredient.of(item));
        return this;
    }

    public VaultAugmentInfusionRecipeBuilder addPedestalItem(Ingredient ingredient) {
        this.pedestalItems.add(ingredient);
        return this;
    }

    public VaultAugmentInfusionRecipeBuilder manaCost(int cost) {
        this.sourceCost = cost;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new Result(recipeId, this.pedestalItems, this.sourceCost));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final List<Ingredient> pedestalItems;
        private final int sourceCost;

        public Result(ResourceLocation id, List<Ingredient> pedestalItems, int sourceCost) {
            this.id = id;
            this.pedestalItems = pedestalItems;
            this.sourceCost = sourceCost;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("sourceCost", this.sourceCost);

            JsonArray pedestalArr = new JsonArray();
            for (Ingredient ingredient : this.pedestalItems) {
                JsonObject obj = new JsonObject();
                obj.add("item", ingredient.toJson());
                pedestalArr.add(obj);
            }
            json.add("pedestalItems", pedestalArr);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ArsRecipeSerializers.VAULT_AUGMENT_INFUSION;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}