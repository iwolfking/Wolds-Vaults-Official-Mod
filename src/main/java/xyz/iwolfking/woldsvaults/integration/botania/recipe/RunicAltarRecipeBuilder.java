package xyz.iwolfking.woldsvaults.integration.botania.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.crafting.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RunicAltarRecipeBuilder {

    private final ItemStack output;
    private final int mana;
    private final List<Ingredient> ingredients = new ArrayList<>();

    private RunicAltarRecipeBuilder(ItemStack output, int mana) {
        this.output = output;
        this.mana = mana;
    }

    public static RunicAltarRecipeBuilder runicAltar(ItemLike output, int count, int mana) {
        return new RunicAltarRecipeBuilder(new ItemStack(output, count), mana);
    }

    public static RunicAltarRecipeBuilder runicAltar(ItemLike output, int mana) {
        return runicAltar(output, 1, mana);
    }

    public RunicAltarRecipeBuilder addIngredient(ItemLike item) {
        this.ingredients.add(Ingredient.of(item));
        return this;
    }

    public RunicAltarRecipeBuilder addIngredient(TagKey<Item> tag) {
        this.ingredients.add(Ingredient.of(tag));
        return this;
    }

    public RunicAltarRecipeBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.output, this.mana, this.ingredients));
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ItemStack output;
        private final int mana;
        private final List<Ingredient> ingredients;

        public Result(ResourceLocation id, ItemStack output, int mana, List<Ingredient> ingredients) {
            this.id = id;
            this.output = output;
            this.mana = mana;
            this.ingredients = ingredients;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonObject outputJson = new JsonObject();
            outputJson.addProperty("item", ForgeRegistries.ITEMS.getKey(this.output.getItem()).toString());
            if (this.output.getCount() > 1) {
                outputJson.addProperty("count", this.output.getCount());
            }
            json.add("output", outputJson);

            json.addProperty("mana", this.mana);

            JsonArray ingredientsArray = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                ingredientsArray.add(ingredient.toJson());
            }
            json.add("ingredients", ingredientsArray);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipeTypes.RUNE_SERIALIZER;
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