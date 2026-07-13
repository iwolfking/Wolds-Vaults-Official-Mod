package xyz.iwolfking.woldsvaults.integration.occultism;

import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RitualRecipeBuilder {

    private final ResourceLocation ritualType;
    private final ResourceLocation pentacleId;
    private final Ingredient activationItem;
    private final ItemStack ritualDummy;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final ItemStack result;
    private int duration = 30;
    private String group = "";

    public RitualRecipeBuilder(ResourceLocation ritualType, ResourceLocation pentacleId, Ingredient activationItem, ItemStack ritualDummy, ItemStack result) {
        this.ritualType = ritualType;
        this.pentacleId = pentacleId;
        this.activationItem = activationItem;
        this.ritualDummy = ritualDummy;
        this.result = result;
    }

    public static RitualRecipeBuilder ritual(ResourceLocation ritualType, ResourceLocation pentacleId, Ingredient activationItem, ItemStack ritualDummy, ItemStack result) {
        return new RitualRecipeBuilder(ritualType, pentacleId, activationItem, ritualDummy, result);
    }

    public RitualRecipeBuilder requires(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public RitualRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public RitualRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.group, this.pentacleId, this.ritualType, this.ritualDummy, this.result, this.activationItem, this.ingredients, this.duration));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final ResourceLocation pentacleId;
        private final ResourceLocation ritualType;
        private final ItemStack ritualDummy;
        private final ItemStack result;
        private final Ingredient activationItem;
        private final List<Ingredient> ingredients;
        private final int duration;

        public Result(ResourceLocation id, String group, ResourceLocation pentacleId, ResourceLocation ritualType, ItemStack ritualDummy, ItemStack result, Ingredient activationItem, List<Ingredient> ingredients, int duration) {
            this.id = id;
            this.group = group;
            this.pentacleId = pentacleId;
            this.ritualType = ritualType;
            this.ritualDummy = ritualDummy;
            this.result = result;
            this.activationItem = activationItem;
            this.ingredients = ingredients;
            this.duration = duration;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            json.addProperty("ritual_type", this.ritualType.toString());
            json.add("activation_item", this.activationItem.toJson());
            json.addProperty("pentacle_id", this.pentacleId.toString());
            json.addProperty("duration", this.duration);

            JsonObject dummyObj = new JsonObject();
            dummyObj.addProperty("item", ForgeRegistries.ITEMS.getKey(this.ritualDummy.getItem()).toString());
            json.add("ritual_dummy", dummyObj);

            JsonArray ingredientsArray = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                ingredientsArray.add(ingredient.toJson());
            }
            json.add("ingredients", ingredientsArray);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result.getItem()).toString());
            if (this.result.getCount() > 1) {
                resultObj.addProperty("count", this.result.getCount());
            }
            json.add("result", resultObj);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RitualRecipe.SERIALIZER;
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