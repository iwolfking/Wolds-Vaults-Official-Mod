package xyz.iwolfking.woldsvaults.integration.occultism;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
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
import java.util.function.Supplier;

public class DynamicRitualRecipeBuilder {
    private final ResourceLocation customId;
    private final ResourceLocation pentacleId;
    private final ResourceLocation ritualType;
    private final Ingredient activationItem;
    private final ResourceLocation ritualDummy;
    private final List<Ingredient> ingredients = new ArrayList<>();

    private final String jsonKey;
    private final ResourceLocation resultItemRegistryName;
    private final Supplier<RecipeSerializer<?>> serializerSupplier;
    
    private int duration = 30;

    private DynamicRitualRecipeBuilder(ResourceLocation customId, ResourceLocation pentacleId, ResourceLocation ritualType, 
                                       Ingredient activationItem, ResourceLocation ritualDummy, String jsonKey, 
                                       ResourceLocation resultItemRegistryName, Supplier<RecipeSerializer<?>> serializerSupplier) {
        this.customId = customId;
        this.pentacleId = pentacleId;
        this.ritualType = ritualType;
        this.activationItem = activationItem;
        this.ritualDummy = ritualDummy;
        this.jsonKey = jsonKey;
        this.resultItemRegistryName = resultItemRegistryName;
        this.serializerSupplier = serializerSupplier;
    }


    public static DynamicRitualRecipeBuilder augment(ResourceLocation themeId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        return new DynamicRitualRecipeBuilder(
                themeId, pentacleId, ritualType, activationItem, ritualDummy, 
                "theme", ModItems.AUGMENT.getRegistryName(), OccultismRecipeSerializers.AUGMENT_RITUAL
        );
    }

    public static DynamicRitualRecipeBuilder augmentPool(ResourceLocation themeId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        return new DynamicRitualRecipeBuilder(
                themeId, pentacleId, ritualType, activationItem, ritualDummy,
                "theme", ModItems.AUGMENT.getRegistryName(), OccultismRecipeSerializers.AUGMENT_POOL_RITUAL
        );
    }

    public static DynamicRitualRecipeBuilder companionRelic(ResourceLocation poolId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        return new DynamicRitualRecipeBuilder(
                poolId, pentacleId, ritualType, activationItem, ritualDummy, 
                "poolId", ModItems.COMPANION_RELIC.getRegistryName(), OccultismRecipeSerializers.COMPANION_RITUAL
        );
    }

    public static DynamicRitualRecipeBuilder vaultCrystal(ResourceLocation poolId, ResourceLocation pentacleId, ResourceLocation ritualType, Ingredient activationItem, ResourceLocation ritualDummy) {
        return new DynamicRitualRecipeBuilder(
                poolId, pentacleId, ritualType, activationItem, ritualDummy,
                "crystalRitualType", ModItems.VAULT_CRYSTAL.getRegistryName(), OccultismRecipeSerializers.VAULT_CRYSTAL_RITUAL
        );
    }

    public DynamicRitualRecipeBuilder addIngredient(Item item) {
        this.ingredients.add(Ingredient.of(item));
        return this;
    }

    public DynamicRitualRecipeBuilder addIngredient(ResourceLocation itemId) {
        JsonObject json = new JsonObject();
        json.addProperty("item", itemId.toString());
        this.ingredients.add(Ingredient.fromJson(json));
        return this;
    }

    public DynamicRitualRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new Result(recipeId, customId, pentacleId, ritualType, activationItem, ritualDummy, ingredients, duration, jsonKey, resultItemRegistryName, serializerSupplier));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ResourceLocation customId;
        private final ResourceLocation pentacleId;
        private final ResourceLocation ritualType;
        private final Ingredient activationItem;
        private final ResourceLocation ritualDummy;
        private final List<Ingredient> ingredients;
        private final int duration;
        
        private final String jsonKey;
        private final ResourceLocation resultItemRegistryName;
        private final Supplier<RecipeSerializer<?>> serializerSupplier;

        public Result(ResourceLocation id, ResourceLocation customId, ResourceLocation pentacleId, ResourceLocation ritualType, 
                      Ingredient activationItem, ResourceLocation ritualDummy, List<Ingredient> ingredients, int duration, 
                      String jsonKey, ResourceLocation resultItemRegistryName, Supplier<RecipeSerializer<?>> serializerSupplier) {
            this.id = id;
            this.customId = customId;
            this.pentacleId = pentacleId;
            this.ritualType = ritualType;
            this.activationItem = activationItem;
            this.ritualDummy = ritualDummy;
            this.ingredients = ingredients;
            this.duration = duration;
            this.jsonKey = jsonKey;
            this.resultItemRegistryName = resultItemRegistryName;
            this.serializerSupplier = serializerSupplier;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty(this.jsonKey, this.customId.toString());
            json.addProperty("pentacle_id", this.pentacleId.toString());
            json.addProperty("ritual_type", this.ritualType.toString());
            json.addProperty("duration", this.duration);

            json.add("activation_item", this.activationItem.toJson());

            JsonObject dummyObj = new JsonObject();
            dummyObj.addProperty("item", this.ritualDummy.toString()); 
            json.add("ritual_dummy", dummyObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", this.resultItemRegistryName.toString());
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
        public RecipeSerializer<?> getType() { return this.serializerSupplier.get(); }

        @Nullable @Override public JsonObject serializeAdvancement() { return null; }
        @Nullable @Override public ResourceLocation getAdvancementId() { return null; }
    }
}