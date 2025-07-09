package xyz.iwolfking.woldsvaults.recipes.lib;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Map;

public class UncheckedRecipe implements FinishedRecipe {
    private final FinishedRecipe base;
    private final Map<Character, ResourceLocation> overrides;

    public UncheckedRecipe(FinishedRecipe base, Map<Character, ResourceLocation> overrides) {
        this.base = base;
        this.overrides = overrides;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        base.serializeRecipeData(json);

        JsonObject key = json.getAsJsonObject("key");
        for (Map.Entry<Character, ResourceLocation> entry : overrides.entrySet()) {
            key.add(String.valueOf(entry.getKey()), toJson(entry.getValue()));
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

    private JsonObject toJson(ResourceLocation rl) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", rl.toString());
        return obj;
    }
}