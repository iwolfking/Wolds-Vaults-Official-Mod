package xyz.iwolfking.woldsvaults.recipes.lib;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Map;

public class NbtAwareRecipe implements FinishedRecipe {
    private final FinishedRecipe base;
    private final Map<Character, IngredientWithNBT> nbtOverrides;

    public NbtAwareRecipe(FinishedRecipe base, Map<Character, IngredientWithNBT> overrides) {
        this.base = base;
        this.nbtOverrides = overrides;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        base.serializeRecipeData(json);

        JsonObject key = json.getAsJsonObject("key");
        for (Map.Entry<Character, IngredientWithNBT> entry : nbtOverrides.entrySet()) {
            key.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
        }
    }

    @Override
    public ResourceLocation getId() {
        return base.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return base.getType();
    }

    @Override
    public JsonObject serializeAdvancement() {
        return base.serializeAdvancement();
    }

    @Override
    public ResourceLocation getAdvancementId() {
        return base.getAdvancementId();
    }

    public record IngredientWithNBT(String itemId, String nbtJson) {

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "forge:nbt");
            obj.addProperty("item", itemId);
            obj.add("nbt", JsonParser.parseString(nbtJson));
            return obj;
        }
    }
}
