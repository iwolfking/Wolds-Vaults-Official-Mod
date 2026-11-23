package xyz.iwolfking.woldsvaults.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.iwolfking.woldsvaults.client.simon.FacetedModel;

/** Writes the "facets" that {@link FacetedModel} reads alongside a model's ordinary elements. */
final class FacetedModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private final JsonArray facets = new JsonArray();
    private JsonElement elements = new JsonArray();

    private FacetedModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(FacetedModel.LOADER_ID, parent, existingFileHelper);
    }

    static <T extends ModelBuilder<T>> FacetedModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new FacetedModelBuilder<>(parent, existingFileHelper);
    }

    // Forge won't let one builder have both elements and a custom loader, so they're built elsewhere and handed over as JSON
    void elements(JsonElement elements) {
        this.elements = elements;
    }

    // corners are x, y, z in model pixels and wind anticlockwise seen from the side the face shows on
    void facet(float[] corners, float[] uv, String texture) {
        JsonObject facet = new JsonObject();
        facet.add("corners", rounded(corners));
        facet.add("uv", rounded(uv));
        facet.addProperty("texture", texture);
        this.facets.add(facet);
    }

    private static JsonArray rounded(float[] values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(Math.round(value * 10000.0F) / 10000.0F);
        }
        return array;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        super.toJson(json);
        json.add("elements", this.elements);
        json.add("facets", this.facets);
        return json;
    }
}
