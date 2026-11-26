package xyz.iwolfking.woldsvaults.client.simon;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.ISimpleModelGeometry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A block model that can hold faces an ordinary one can't. Its file keeps the usual "elements" and adds "facets": any
 * four-cornered face at any angle, given as corners in model pixels, their texture coordinates and a texture. A bevel needs
 * this, since its slopes are trapezoids and a slope on an already tilted box turns about two axes at once, where elements are
 * limited to rectangles turned about one. Elements still bake as normal. Each facet is shaded by its own direction once it's
 * been rotated into place, with Minecraft's formula, because the game's own face shade only picks from six directions.
 */
public final class FacetedModel implements ISimpleModelGeometry<FacetedModel> {
    public static final ResourceLocation LOADER_ID = WoldsVaults.id("faceted");

    private static final int CORNERS = 4;
    private static final int VERTEX_SIZE = 8;

    private final ModelLoaderRegistry.VanillaProxy elements;
    private final List<Facet> facets;

    private FacetedModel(ModelLoaderRegistry.VanillaProxy elements, List<Facet> facets) {
        this.elements = elements;
        this.facets = facets;
    }

    @Override
    public void addQuads(IModelConfiguration owner, IModelBuilder<?> builder, ModelBakery bakery, Function<Material, TextureAtlasSprite> sprites, ModelState transform, ResourceLocation location) {
        this.elements.addQuads(owner, builder, bakery, sprites, transform, location);
        for (Facet facet : this.facets) {
            builder.addGeneralQuad(facet.bake(sprites.apply(owner.resolveTexture(facet.texture)), transform.getRotation()));
        }
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> models, Set<Pair<String, String>> missing) {
        Set<Material> textures = new HashSet<>(this.elements.getTextures(owner, models, missing));
        for (Facet facet : this.facets) {
            textures.add(owner.resolveTexture(facet.texture));
        }
        return textures;
    }

    private record Facet(float[] corners, float[] uv, String texture) {
        BakedQuad bake(TextureAtlasSprite sprite, Transformation rotation) {
            Matrix4f matrix = rotation.getMatrix();
            Vector3f[] placed = new Vector3f[CORNERS];
            for (int i = 0; i < CORNERS; i++) {
                Vector4f corner = new Vector4f(this.corners[i * 3] / 16.0F - 0.5F, this.corners[i * 3 + 1] / 16.0F - 0.5F, this.corners[i * 3 + 2] / 16.0F - 0.5F, 1.0F);
                corner.transform(matrix);
                placed[i] = new Vector3f(corner.x() + 0.5F, corner.y() + 0.5F, corner.z() + 0.5F);
            }
            Vector3f across = placed[2].copy();
            across.sub(placed[0]);
            Vector3f other = placed[3].copy();
            other.sub(placed[1]);
            across.cross(other);
            across.normalize();
            int grey = (int) (LightUtil.diffuseLight(across.x(), across.y(), across.z()) * 255.0F);
            int color = 0xFF000000 | grey << 16 | grey << 8 | grey;
            int normal = (Math.round(across.x() * 127.0F) & 255) | (Math.round(across.y() * 127.0F) & 255) << 8 | (Math.round(across.z() * 127.0F) & 255) << 16;

            int[] vertices = new int[CORNERS * VERTEX_SIZE];
            for (int i = 0; i < CORNERS; i++) {
                int at = i * VERTEX_SIZE;
                int opposite = (i + 2) % CORNERS;
                vertices[at] = Float.floatToRawIntBits(placed[i].x());
                vertices[at + 1] = Float.floatToRawIntBits(placed[i].y());
                vertices[at + 2] = Float.floatToRawIntBits(placed[i].z());
                vertices[at + 3] = color;
                vertices[at + 4] = Float.floatToRawIntBits(sprite.getU(this.uv[i * 2] * 0.999D + this.uv[opposite * 2] * 0.001D));
                vertices[at + 5] = Float.floatToRawIntBits(sprite.getV(this.uv[i * 2 + 1] * 0.999D + this.uv[opposite * 2 + 1] * 0.001D));
                vertices[at + 7] = normal;
            }
            return new BakedQuad(vertices, -1, Direction.getNearest(across.x(), across.y(), across.z()), sprite, false);
        }
    }

    public static final class Loader implements IModelLoader<FacetedModel> {
        @Override
        public void onResourceManagerReload(ResourceManager manager) {
        }

        @Override
        public FacetedModel read(JsonDeserializationContext context, JsonObject json) {
            List<Facet> facets = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "facets")) {
                JsonObject facet = element.getAsJsonObject();
                facets.add(new Facet(numbers(facet, "corners", CORNERS * 3), numbers(facet, "uv", CORNERS * 2), GsonHelper.getAsString(facet, "texture")));
            }
            return new FacetedModel(ModelLoaderRegistry.VanillaProxy.Loader.INSTANCE.read(context, json), facets);
        }

        private static float[] numbers(JsonObject json, String key, int count) {
            JsonArray array = GsonHelper.getAsJsonArray(json, key);
            if (array.size() != count) {
                throw new IllegalArgumentException("Expected " + count + " numbers in a facet's " + key + " but found " + array.size());
            }
            float[] values = new float[count];
            for (int i = 0; i < count; i++) {
                values[i] = array.get(i).getAsFloat();
            }
            return values;
        }
    }
}
