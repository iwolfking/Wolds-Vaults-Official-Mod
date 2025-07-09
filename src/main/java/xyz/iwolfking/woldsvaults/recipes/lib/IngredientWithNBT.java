package xyz.iwolfking.woldsvaults.recipes.lib;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record IngredientWithNBT(String itemId, String nbtJson) {

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "forge:nbt");
        obj.addProperty("item", itemId);
        obj.add("nbt", JsonParser.parseString(nbtJson));
        return obj;
    }
}