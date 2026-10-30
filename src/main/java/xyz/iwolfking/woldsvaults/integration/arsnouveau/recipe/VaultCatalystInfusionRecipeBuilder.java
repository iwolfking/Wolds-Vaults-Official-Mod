package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.init.ModRecipeSerializers;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VaultCatalystInfusionRecipeBuilder {
    private final ResourceLocation poolId;
    private final List<Ingredient> pedestalItems = new ArrayList<>();
    private String infusionAction = "roll_pool";
    private int sizeOffset = 0;
    private int sourceCost = 0;

    private VaultCatalystInfusionRecipeBuilder(ResourceLocation poolId) {
        this.poolId = poolId;
    }

    public static VaultCatalystInfusionRecipeBuilder infusingPool(ResourceLocation poolId) {
        return new VaultCatalystInfusionRecipeBuilder(poolId);
    }

    public VaultCatalystInfusionRecipeBuilder addPedestalItem(Item item) {
        this.pedestalItems.add(Ingredient.of(item));
        return this;
    }

    public VaultCatalystInfusionRecipeBuilder addPedestalItem(Ingredient ingredient) {
        this.pedestalItems.add(ingredient);
        return this;
    }

    public VaultCatalystInfusionRecipeBuilder action(String action) {
        this.infusionAction = action;
        return this;
    }

    public VaultCatalystInfusionRecipeBuilder sizeOffset(int offset) {
        this.sizeOffset = offset;
        return this;
    }

    public VaultCatalystInfusionRecipeBuilder manaCost(int cost) {
        this.sourceCost = cost;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new Result(recipeId, this.pedestalItems, this.poolId, this.infusionAction, this.sizeOffset, this.sourceCost));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final List<Ingredient> pedestalItems;
        private final ResourceLocation poolId;
        private final String infusionAction;
        private final int sizeOffset;
        private final int sourceCost;

        public Result(ResourceLocation id, List<Ingredient> pedestalItems, ResourceLocation poolId, String infusionAction, int sizeOffset, int sourceCost) {
            this.id = id;
            this.pedestalItems = pedestalItems;
            this.poolId = poolId;
            this.infusionAction = infusionAction;
            this.sizeOffset = sizeOffset;
            this.sourceCost = sourceCost;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("pool_id", this.poolId.toString());
            json.addProperty("infusion_action", this.infusionAction);
            json.addProperty("size_offset", this.sizeOffset);
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
            return ArsRecipeSerializers.VAULT_CATALYST_INFUSION;
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