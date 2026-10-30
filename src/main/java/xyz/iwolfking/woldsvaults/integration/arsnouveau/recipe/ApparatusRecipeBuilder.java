package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.setup.RecipeRegistry;
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

public class ApparatusRecipeBuilder {

    private final ItemStack result;
    private Ingredient reagent = Ingredient.EMPTY;
    private final List<Ingredient> pedestalItems = new ArrayList<>();
    private boolean keepNbtOfReagent = false;
    private int sourceCost = 0;

    private ApparatusRecipeBuilder(ItemStack result) {
        this.result = result;
    }

    public static ApparatusRecipeBuilder builder(ItemLike result) {
        return new ApparatusRecipeBuilder(new ItemStack(result));
    }

    public static ApparatusRecipeBuilder builder(ItemLike result, int count) {
        return new ApparatusRecipeBuilder(new ItemStack(result, count));
    }

    public static ApparatusRecipeBuilder builder(ItemStack result) {
        return new ApparatusRecipeBuilder(result);
    }


    public ApparatusRecipeBuilder withReagent(ItemLike item) {
        this.reagent = Ingredient.of(item);
        return this;
    }

    public ApparatusRecipeBuilder withReagent(TagKey<Item> tag) {
        this.reagent = Ingredient.of(tag);
        return this;
    }

    public ApparatusRecipeBuilder withReagent(Ingredient ingredient) {
        this.reagent = ingredient;
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(ItemLike item) {
        this.pedestalItems.add(Ingredient.of(item));
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(TagKey<Item> tag) {
        this.pedestalItems.add(Ingredient.of(tag));
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(Ingredient ingredient) {
        this.pedestalItems.add(ingredient);
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(int count, ItemLike item) {
        for (int i = 0; i < count; i++) {
            withPedestalItem(item);
        }
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(int count, TagKey<Item> tag) {
        for (int i = 0; i < count; i++) {
            withPedestalItem(tag);
        }
        return this;
    }

    public ApparatusRecipeBuilder withPedestalItem(int count, Ingredient ingredient) {
        for (int i = 0; i < count; i++) {
            withPedestalItem(ingredient);
        }
        return this;
    }


    public ApparatusRecipeBuilder keepNbtOfReagent(boolean keepNbt) {
        this.keepNbtOfReagent = keepNbt;
        return this;
    }

    public ApparatusRecipeBuilder withSourceCost(int sourceCost) {
        this.sourceCost = sourceCost;
        return this;
    }


    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(id, this.result, this.reagent, this.pedestalItems, this.keepNbtOfReagent, this.sourceCost));
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(this.result.getItem());
        build(consumer, ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "apparatus/" + itemId.getPath()));
    }


    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final ItemStack result;
        private final Ingredient reagent;
        private final List<Ingredient> pedestalItems;
        private final boolean keepNbtOfReagent;
        private final int sourceCost;

        public Result(ResourceLocation id, ItemStack result, Ingredient reagent, List<Ingredient> pedestalItems, boolean keepNbtOfReagent, int sourceCost) {
            this.id = id;
            this.result = result;
            this.reagent = reagent;
            this.pedestalItems = pedestalItems;
            this.keepNbtOfReagent = keepNbtOfReagent;
            this.sourceCost = sourceCost;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray reagentArray = new JsonArray();
            reagentArray.add(this.reagent.toJson());
            json.add("reagent", reagentArray);

            JsonArray pedestalArray = new JsonArray();
            for (Ingredient pedestalItem : this.pedestalItems) {
                JsonObject itemWrapper = new JsonObject();
                itemWrapper.add("item", pedestalItem.toJson());
                pedestalArray.add(itemWrapper);
            }
            json.add("pedestalItems", pedestalArray);

            JsonObject outputObj = new JsonObject();
            outputObj.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result.getItem()).toString());
            if (this.result.getCount() > 1) {
                outputObj.addProperty("count", this.result.getCount());
            }
            json.add("output", outputObj);

            if (this.keepNbtOfReagent) {
                json.addProperty("keepNbtOfReagent", true);
            }
            if (this.sourceCost > 0) {
                json.addProperty("sourceCost", this.sourceCost);
            }
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeRegistry.APPARATUS_SERIALIZER;
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