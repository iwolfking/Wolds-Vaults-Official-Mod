package xyz.iwolfking.woldsvaults.integration.occultism;

import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.AugmentItem;
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
import net.minecraft.tags.Tag;
import xyz.iwolfking.woldsvaults.init.ModRecipeSerializers;
import xyz.iwolfking.woldsvaults.integration.occultism.init.OccultismRecipeSerializers;

import javax.annotation.Nullable;
import java.util.List;

public class AugmentRitualRecipe extends RitualRecipe {
    private final ResourceLocation themeId;

    public AugmentRitualRecipe(ResourceLocation id, String group, ResourceLocation pentacleId, ResourceLocation ritualType, ItemStack ritualDummy,
                               EntityType<?> entityToSummon, CompoundTag entityNbt, Ingredient activationItem, NonNullList<Ingredient> input, int duration, int spiritMaxAge, ResourceLocation spiritJobType,
                               TagKey<EntityType<?>> entityToSacrifice, String entityToSacrificeDisplayName, Ingredient itemToUse, String command, ResourceLocation themeId) {
        super(id, group, pentacleId, ritualType, ritualDummy, AugmentItem.create(themeId), entityToSummon, entityNbt, activationItem, input, duration, spiritMaxAge, spiritJobType, entityToSacrifice, entityToSacrificeDisplayName, itemToUse, command);
        this.themeId = themeId;
    }
    public ResourceLocation getThemeId() {
        return this.themeId;
    }

    @Override
    public ItemStack getResultItem() {
        return AugmentItem.create(this.themeId);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OccultismRecipeSerializers.AUGMENT_RITUAL;
    }

    public static class Serializer extends RitualRecipe.Serializer {
        
        @Override
        public AugmentRitualRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            RitualRecipe base = super.fromJson(recipeId, json);
            
            if (!json.has("theme")) {
                throw new JsonParseException("Augment Ritual Recipe is missing its mandatory 'theme' property!");
            }
            ResourceLocation themeId = ResourceLocation.parse(GsonHelper.getAsString(json, "theme"));

            return new AugmentRitualRecipe(
                    base.getId(), base.getGroup(), base.getPentacleId(), base.getRitualType(), base.getRitualDummy(),
                    base.getEntityToSummon(), base.getEntityNbt(), base.getActivationItem(), base.getIngredients(), base.getDuration(),
                    base.getSpiritMaxAge(), base.getSpiritJobType(), base.getEntityToSacrifice(),
                    base.getEntityToSacrificeDisplayName(), base.getItemToUse(), base.getCommand(), themeId
            );
        }

        @Override
        public AugmentRitualRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            RitualRecipe base = super.fromNetwork(recipeId, buffer);
            ResourceLocation themeId = buffer.readResourceLocation();

            return new AugmentRitualRecipe(
                    base.getId(), base.getGroup(), base.getPentacleId(), base.getRitualType(), base.getRitualDummy(),
                    base.getEntityToSummon(), base.getEntityNbt(), base.getActivationItem(), base.getIngredients(), base.getDuration(),
                    base.getSpiritMaxAge(), base.getSpiritJobType(), base.getEntityToSacrifice(),
                    base.getEntityToSacrificeDisplayName(), base.getItemToUse(), base.getCommand(), themeId
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RitualRecipe recipe) {
            super.toNetwork(buffer, recipe);
            if (recipe instanceof AugmentRitualRecipe augmentRecipe) {
                buffer.writeResourceLocation(augmentRecipe.getThemeId());
            }
        }
    }
}