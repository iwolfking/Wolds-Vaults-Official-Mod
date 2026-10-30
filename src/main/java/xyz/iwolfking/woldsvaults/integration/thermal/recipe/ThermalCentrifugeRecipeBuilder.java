package xyz.iwolfking.woldsvaults.integration.thermal.recipe;

import cofh.thermal.core.ThermalCore;
import cofh.thermal.core.init.TCoreRecipeTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ThermalCentrifugeRecipeBuilder {

    private final Ingredient ingredient;
    private final List<ItemStack> itemResults = new ArrayList<>();
    private final List<FluidStack> fluidResults = new ArrayList<>();
    private int energy = 400;

    private ThermalCentrifugeRecipeBuilder(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public static ThermalCentrifugeRecipeBuilder centrifuge(Ingredient ingredient) {
        return new ThermalCentrifugeRecipeBuilder(ingredient);
    }

    public static ThermalCentrifugeRecipeBuilder centrifuge(Item item) {
        return centrifuge(Ingredient.of(item));
    }

    public ThermalCentrifugeRecipeBuilder addResult(ItemStack stack) {
        this.itemResults.add(stack);
        return this;
    }

    public ThermalCentrifugeRecipeBuilder addResult(Item item) {
        return addResult(new ItemStack(item));
    }

    public ThermalCentrifugeRecipeBuilder addResult(FluidStack stack) {
        this.fluidResults.add(stack);
        return this;
    }

    public ThermalCentrifugeRecipeBuilder energy(int energy) {
        this.energy = energy;
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (this.itemResults.isEmpty() && this.fluidResults.isEmpty()) {
            throw new IllegalStateException("Thermal Centrifuge recipe requires at least one item or fluid result: " + id);
        }
        consumer.accept(new Result(id, this.ingredient, this.itemResults, this.fluidResults, this.energy));
    }

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final Ingredient ingredient;
        private final List<ItemStack> itemResults;
        private final List<FluidStack> fluidResults;
        private final int energy;

        public Result(ResourceLocation id, Ingredient ingredient, List<ItemStack> itemResults, List<FluidStack> fluidResults, int energy) {
            this.id = id;
            this.ingredient = ingredient;
            this.itemResults = itemResults;
            this.fluidResults = fluidResults;
            this.energy = energy;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", this.ingredient.toJson());

            JsonArray resultArray = new JsonArray();

            for (ItemStack itemStack : this.itemResults) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("item", ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
                if (itemStack.getCount() > 1) {
                    itemObj.addProperty("count", itemStack.getCount());
                }
                resultArray.add(itemObj);
            }

            for (FluidStack fluidStack : this.fluidResults) {
                JsonObject fluidObj = new JsonObject();
                fluidObj.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString());
                fluidObj.addProperty("amount", fluidStack.getAmount());
                resultArray.add(fluidObj);
            }

            json.add("result", resultArray);
            json.addProperty("energy", this.energy);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ThermalCore.RECIPE_SERIALIZERS.get(TCoreRecipeTypes.ID_RECIPE_CENTRIFUGE);
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