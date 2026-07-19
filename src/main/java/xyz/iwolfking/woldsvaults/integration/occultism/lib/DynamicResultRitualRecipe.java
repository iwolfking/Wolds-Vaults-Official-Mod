package xyz.iwolfking.woldsvaults.integration.occultism.lib;

import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DynamicResultRitualRecipe extends RitualRecipe {
    private final ResourceLocation customId;
    private final Supplier<RecipeSerializer<?>> serializerSupplier;
    private final Function<ResourceLocation, ItemStack> resultFactory;

    public DynamicResultRitualRecipe(RitualRecipe base, ResourceLocation customId, 
                                     Supplier<RecipeSerializer<?>> serializerSupplier, 
                                     Function<ResourceLocation, ItemStack> resultFactory) {
        super(base.getId(), base.getGroup(), base.getPentacleId(), base.getRitualType(), 
              base.getRitualDummy(), resultFactory.apply(customId), base.getEntityToSummon(), 
              base.getEntityNbt(), base.getActivationItem(), base.getIngredients(), 
              base.getDuration(), base.getSpiritMaxAge(), base.getSpiritJobType(), 
              base.getEntityToSacrifice(), base.getEntityToSacrificeDisplayName(), 
              base.getItemToUse(), base.getCommand());
        
        this.customId = customId;
        this.serializerSupplier = serializerSupplier;
        this.resultFactory = resultFactory;
    }

    public ResourceLocation getCustomId() {
        return this.customId;
    }

    @Override
    public ItemStack getResultItem() {
        return this.resultFactory.apply(this.customId);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializerSupplier.get();
    }

    public static class Serializer extends RitualRecipe.Serializer {
        private final String jsonKey;
        private final Supplier<RecipeSerializer<?>> serializerSupplier;
        private final Function<ResourceLocation, ItemStack> resultFactory;

        public Serializer(String jsonKey, Supplier<RecipeSerializer<?>> serializerSupplier, Function<ResourceLocation, ItemStack> resultFactory) {
            this.jsonKey = jsonKey;
            this.serializerSupplier = serializerSupplier;
            this.resultFactory = resultFactory;
        }

        @Override
        public DynamicResultRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            RitualRecipe base = super.fromJson(recipeId, json);
            if (!json.has(this.jsonKey)) {
                throw new JsonParseException("Ritual recipe is missing its mandatory '" + this.jsonKey + "' property!");
            }
            ResourceLocation customId = ResourceLocation.parse(GsonHelper.getAsString(json, this.jsonKey));
            return new DynamicResultRitualRecipe(base, customId, serializerSupplier, resultFactory);
        }

        @Override
        public DynamicResultRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            RitualRecipe base = super.fromNetwork(recipeId, buffer);
            ResourceLocation customId = buffer.readResourceLocation();
            return new DynamicResultRitualRecipe(base, customId, serializerSupplier, resultFactory);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RitualRecipe recipe) {
            super.toNetwork(buffer, recipe);
            if (recipe instanceof DynamicResultRitualRecipe dynamicRecipe) {
                buffer.writeResourceLocation(dynamicRecipe.getCustomId());
            }
        }
    }
}