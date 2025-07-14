package xyz.iwolfking.woldsvaults.recipes.lib;


import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.ModRecipeProvider;
import xyz.iwolfking.woldsvaults.init.ModRecipeSerializers;

import java.util.function.Consumer;

public class InfuserRecipeBuilder implements RecipeBuilder {
    private final int infuseDuration;
    private final int inputCount;
    private final Ingredient ingredient;
    private final Ingredient catalyst;
    private final Item result;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    public InfuserRecipeBuilder(ItemLike ingredient, ItemLike catalyst, ItemLike result, int infuseDuration, int inputCount) {
        this.ingredient = Ingredient.of(ingredient);
        this.catalyst = Ingredient.of(catalyst);
        this.result = result.asItem();
        this.infuseDuration = infuseDuration;
        this.inputCount = inputCount;
    }

    @Override
    public RecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);

        this.advancement.parent(new ResourceLocation("recipes/root"))
                .addCriterion("has_the_recipe",
                        RecipeUnlockedTrigger.unlocked(pRecipeId))
                .rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);

        pFinishedRecipeConsumer.accept(
                new InfuserRecipeBuilder.Result(
                        pRecipeId,
                        infuseDuration,
                        inputCount,
                        ingredient,
                        catalyst,
                        result,
                        advancement,
                        new ResourceLocation(pRecipeId.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + pRecipeId.getPath())
                )
        );
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final int infuseDuration;
        private final int inputCount;
        private final Ingredient ingredient;
        private final Ingredient catalyst;
        private final Item result;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation id, int infuseDuration, int inputCount, Ingredient ingredient, Ingredient catalyst, Item result, Advancement.Builder advancement, ResourceLocation advancementId) {
            this.id = id;
            this.infuseDuration = infuseDuration;
            this.inputCount = inputCount;
            this.ingredient = ingredient;
            this.catalyst = catalyst;
            this.result = result;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject pJson) {
            pJson.addProperty("infuseDuration", infuseDuration);
            pJson.addProperty("inputCount", inputCount);
            pJson.add("ingredient", ingredient.toJson());
            pJson.add("catalyst", catalyst.toJson());

            JsonObject obj = new JsonObject();
            obj.addProperty("item", this.result.getRegistryName().toString());
            pJson.add("result", obj);

        }

        @Override
        public ResourceLocation getId() {
            return WoldsVaults.id(this.result.getRegistryName().getPath() + "_infusing");
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipeSerializers.INFUSER;
        }

        @Override
        public @Nullable JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
