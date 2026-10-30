package xyz.iwolfking.woldsvaults.integration.thermal.recipe;

import cofh.thermal.core.ThermalCore;
import cofh.thermal.core.init.TCoreRecipeSerializers;
import cofh.thermal.core.init.TCoreRecipeTypes;
import cofh.thermal.lib.util.recipes.ThermalRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ThermalSmelterRecipeBuilder {

    private final List<ItemStack> outputs = new ArrayList<>();
    private final List<IngredientEntry> ingredients = new ArrayList<>();
    private int energy = 1000;
    private float experience = 0.0f;

    private ThermalSmelterRecipeBuilder() {}

    public static ThermalSmelterRecipeBuilder smelter() {
        return new ThermalSmelterRecipeBuilder();
    }

    public ThermalSmelterRecipeBuilder addOutput(ItemLike item, int count) {
        this.outputs.add(new ItemStack(item, count));
        return this;
    }

    public ThermalSmelterRecipeBuilder addOutput(ItemLike item) {
        return addOutput(item, 1);
    }

    public ThermalSmelterRecipeBuilder addOutput(ItemStack stack) {
        this.outputs.add(stack);
        return this;
    }


    public ThermalSmelterRecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public ThermalSmelterRecipeBuilder experience(float experience) {
        this.experience = experience;
        return this;
    }


    public ThermalSmelterRecipeBuilder addIngredient(ItemLike item, int count) {
        this.ingredients.add(new IngredientEntry(Ingredient.of(item), count));
        return this;
    }

    public ThermalSmelterRecipeBuilder addIngredient(TagKey<Item> tag, int count) {
        this.ingredients.add(new IngredientEntry(Ingredient.of(tag), count));
        return this;
    }

    public ThermalSmelterRecipeBuilder addIngredient(List<Ingredient> ingredients, int count) {
        this.ingredients.add(new IngredientEntry(ingredients, count));
        return this;
    }

    @SafeVarargs
    public final ThermalSmelterRecipeBuilder addMultiIngredient(int count, TagKey<Item>... tags) {
        List<Ingredient> list = new ArrayList<>();
        for (TagKey<Item> tag : tags) {
            list.add(Ingredient.of(tag));
        }
        this.ingredients.add(new IngredientEntry(list, count));
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.outputs, this.ingredients, this.energy, this.experience));
    }


    private static class IngredientEntry {
        private final List<Ingredient> ingredients;
        private final int count;

        public IngredientEntry(Ingredient ingredient, int count) {
            this.ingredients = List.of(ingredient);
            this.count = count;
        }

        public IngredientEntry(List<Ingredient> ingredients, int count) {
            this.ingredients = ingredients;
            this.count = count;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();

            if (ingredients.size() == 1) {
                JsonElement ingJson = ingredients.get(0).toJson();
                if (ingJson.isJsonObject()) {
                    JsonObject ingObj = ingJson.getAsJsonObject();
                    ingObj.entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
                }
            } else {
                JsonArray valueArray = new JsonArray();
                for (Ingredient ing : ingredients) {
                    valueArray.add(ing.toJson());
                }
                json.add("value", valueArray);
            }

            if (this.count > 1) {
                json.addProperty("count", this.count);
            }

            return json;
        }
    }


    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final List<ItemStack> outputs;
        private final List<IngredientEntry> ingredients;
        private final int energy;
        private final float experience;

        public Result(ResourceLocation id, List<ItemStack> outputs, List<IngredientEntry> ingredients, int energy, float experience) {
            this.id = id;
            this.outputs = outputs;
            this.ingredients = ingredients;
            this.energy = energy;
            this.experience = experience;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray ingredientsArray = new JsonArray();
            for (IngredientEntry ing : this.ingredients) {
                ingredientsArray.add(ing.toJson());
            }
            json.add("ingredients", ingredientsArray);

            JsonArray resultsArray = new JsonArray();
            for (ItemStack stack : this.outputs) {
                JsonObject outObj = new JsonObject();
                outObj.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                if (stack.getCount() > 1) {
                    outObj.addProperty("count", stack.getCount());
                }
                resultsArray.add(outObj);
            }
            json.add("result", resultsArray);

            json.addProperty("energy", this.energy);
            if (this.experience > 0.0f) {
                json.addProperty("experience", this.experience);
            }
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ThermalCore.RECIPE_SERIALIZERS.get(TCoreRecipeTypes.ID_RECIPE_SMELTER);
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