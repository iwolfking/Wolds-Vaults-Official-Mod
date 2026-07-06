package xyz.iwolfking.woldsvaults.integration.occultism;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.init.ModItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.integration.occultism.init.OccultismRecipeSerializers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AugmentRitualRecipeBuilder {
    private final ResourceLocation themeId;
    private final ResourceLocation pentacleId;
    private final ResourceLocation ritualType;
    private final Ingredient activationItem;
    private final ResourceLocation ritualDummy;
    private final List<Ingredient> ingredients = new ArrayList<>();
    
    private int duration = 30;

    private AugmentRitualRecipeBuilder(ResourceLocation themeId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        this.themeId = themeId;
        this.pentacleId = pentacleId;
        this.ritualType = ritualType;
        this.activationItem = activationItem;
        this.ritualDummy = ritualDummy;
    }

    public static AugmentRitualRecipeBuilder create(ResourceLocation themeId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        return new AugmentRitualRecipeBuilder(themeId, pentacleId, ritualType, activationItem, ritualDummy);
    }

    public AugmentRitualRecipeBuilder addIngredient(Item item) {
        this.ingredients.add(Ingredient.of(item));
        return this;
    }

    public AugmentRitualRecipeBuilder addIngredient(ResourceLocation itemId) {
        JsonObject json = new JsonObject();
        json.addProperty("item", itemId.toString());
        this.ingredients.add(Ingredient.fromJson(json));
        return this;
    }

    public AugmentRitualRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new Result(recipeId, themeId, pentacleId, ritualType, activationItem, ritualDummy, ingredients, duration));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ResourceLocation themeId;
        private final ResourceLocation pentacleId;
        private final ResourceLocation ritualType;
        private final Ingredient activationItem;
        private final ResourceLocation ritualDummy;
        private final List<Ingredient> ingredients;
        private final int duration;

        public Result(ResourceLocation id, ResourceLocation themeId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy, List<Ingredient> ingredients, int duration) {
            this.id = id;
            this.themeId = themeId;
            this.pentacleId = pentacleId;
            this.ritualType = ritualType;
            this.activationItem = activationItem;
            this.ritualDummy = ritualDummy;
            this.ingredients = ingredients;
            this.duration = duration;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("theme", this.themeId.toString());
            json.addProperty("pentacle_id", this.pentacleId.toString());
            json.addProperty("ritual_type", this.ritualType.toString());
            json.addProperty("duration", this.duration);

            json.add("activation_item", this.activationItem.toJson());

            JsonObject dummyObj = new JsonObject();
            dummyObj.addProperty("item", this.ritualDummy.toString()); 
            json.add("ritual_dummy", dummyObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", ModItems.AUGMENT.getRegistryName().toString());
            json.add("result", resultObj);

            JsonArray ingredientArr = new JsonArray();
            for (Ingredient ing : this.ingredients) {
                ingredientArr.add(ing.toJson());
            }
            json.add("ingredients", ingredientArr);
        }

        @Override
        public ResourceLocation getId() { return this.id; }

        @Override
        public RecipeSerializer<?> getType() { return OccultismRecipeSerializers.AUGMENT_RITUAL; }

        @Nullable @Override public JsonObject serializeAdvancement() { return null; }
        @Nullable @Override public ResourceLocation getAdvancementId() { return null; }
    }
}