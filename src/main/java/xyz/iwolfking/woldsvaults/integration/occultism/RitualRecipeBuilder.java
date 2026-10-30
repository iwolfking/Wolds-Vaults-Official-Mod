package xyz.iwolfking.woldsvaults.integration.occultism;

import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.datagen.ModLanguageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RitualRecipeBuilder {

    private final ResourceLocation ritualType;
    private final ResourceLocation pentacleId;
    private final Ingredient activationItem;
    private final ItemStack ritualDummy;
    private final ItemStack result;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private String description;
    private String name;

    private int duration = 30;
    private String group = "";

    @Nullable private Ingredient itemToUse;
    @Nullable private TagKey<EntityType<?>> entityToSacrificeTag;
    @Nullable private String entityToSacrificeDisplayName;
    @Nullable private ResourceLocation entityToSummon;
    @Nullable private ResourceLocation spiritJobType;
    private int spiritMaxAge = -1;

    public RitualRecipeBuilder(ResourceLocation ritualType, ResourceLocation pentacleId, Ingredient activationItem, ItemStack ritualDummy, ItemStack result) {
        this.ritualType = ritualType;
        this.pentacleId = pentacleId;
        this.activationItem = activationItem;
        this.ritualDummy = ritualDummy;
        this.result = result;
    }

    public static RitualRecipeBuilder ritual(ResourceLocation ritualType, ResourceLocation pentacleId, Ingredient activationItem, ItemStack ritualDummy, ItemStack result) {
        return new RitualRecipeBuilder(ritualType, pentacleId, activationItem, ritualDummy, result);
    }

    public RitualRecipeBuilder requires(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public RitualRecipeBuilder requires(ItemLike item) {
        return this.requires(Ingredient.of(item));
    }

    public RitualRecipeBuilder requires(TagKey<net.minecraft.world.item.Item> tag) {
        return this.requires(Ingredient.of(tag));
    }

    public RitualRecipeBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public RitualRecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public RitualRecipeBuilder itemToUse(Ingredient itemToUse) {
        this.itemToUse = itemToUse;
        return this;
    }

    public RitualRecipeBuilder entityToSacrifice(TagKey<EntityType<?>> tag, String translationKey) {
        this.entityToSacrificeTag = tag;
        this.entityToSacrificeDisplayName = translationKey;
        return this;
    }

    public RitualRecipeBuilder entityToSacrifice(EntityType<?> entityType) {
        TagKey<EntityType<?>> tagKey = OccultismTagRegistry.getOrCreateEntityTag(entityType);
        return this.entityToSacrifice(tagKey, entityType.getDescriptionId());
    }

    public RitualRecipeBuilder entityToSacrifice(ResourceLocation entityId, String translationKey) {
        TagKey<EntityType<?>> tagKey = OccultismTagRegistry.getOrCreateEntityTag(entityId);
        return this.entityToSacrifice(tagKey, translationKey);
    }

    public RitualRecipeBuilder entityToSummon(ResourceLocation entityType) {
        this.entityToSummon = entityType;
        return this;
    }

    public RitualRecipeBuilder entityToSummon(EntityType<?> entityType) {
        this.entityToSummon = ForgeRegistries.ENTITIES.getKey(entityType);
        return this;
    }

    public RitualRecipeBuilder spiritJobType(ResourceLocation jobType) {
        this.spiritJobType = jobType;
        return this;
    }

    public RitualRecipeBuilder spiritMaxAge(int spiritMaxAge) {
        this.spiritMaxAge = spiritMaxAge;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new Result(
                id, this.group, this.pentacleId, this.ritualType, this.ritualDummy, 
                this.result, this.activationItem, this.ingredients, this.duration,
                this.itemToUse, this.entityToSacrificeTag, this.entityToSacrificeDisplayName,
                this.entityToSummon, this.spiritJobType, this.spiritMaxAge
        ));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final ResourceLocation pentacleId;
        private final ResourceLocation ritualType;
        private final ItemStack ritualDummy;
        private final ItemStack result;
        private final Ingredient activationItem;
        private final List<Ingredient> ingredients;
        private final int duration;

        @Nullable private final Ingredient itemToUse;
        @Nullable private final TagKey<EntityType<?>> entityToSacrificeTag;
        @Nullable private final String entityToSacrificeDisplayName;
        @Nullable private final ResourceLocation entityToSummon;
        @Nullable private final ResourceLocation spiritJobType;
        private final int spiritMaxAge;

        public Result(ResourceLocation id, String group, ResourceLocation pentacleId, ResourceLocation ritualType, 
                      ItemStack ritualDummy, ItemStack result, Ingredient activationItem, List<Ingredient> ingredients, 
                      int duration, @Nullable Ingredient itemToUse, @Nullable TagKey<EntityType<?>> entityToSacrificeTag, 
                      @Nullable String entityToSacrificeDisplayName, @Nullable ResourceLocation entityToSummon, 
                      @Nullable ResourceLocation spiritJobType, int spiritMaxAge) {
            this.id = id;
            this.group = group;
            this.pentacleId = pentacleId;
            this.ritualType = ritualType;
            this.ritualDummy = ritualDummy;
            this.result = result;
            this.activationItem = activationItem;
            this.ingredients = ingredients;
            this.duration = duration;
            this.itemToUse = itemToUse;
            this.entityToSacrificeTag = entityToSacrificeTag;
            this.entityToSacrificeDisplayName = entityToSacrificeDisplayName;
            this.entityToSummon = entityToSummon;
            this.spiritJobType = spiritJobType;
            this.spiritMaxAge = spiritMaxAge;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            json.addProperty("ritual_type", this.ritualType.toString());
            json.add("activation_item", this.activationItem.toJson());
            json.addProperty("pentacle_id", this.pentacleId.toString());
            json.addProperty("duration", this.duration);

            if (this.spiritMaxAge > 0) {
                json.addProperty("spirit_max_age", this.spiritMaxAge);
            }

            if (this.spiritJobType != null) {
                json.addProperty("spirit_job_type", this.spiritJobType.toString());
            }

            if (this.itemToUse != null) {
                json.add("item_to_use", this.itemToUse.toJson());
            }

            if (this.entityToSacrificeTag != null && this.entityToSacrificeDisplayName != null) {
                JsonObject sacrificeObj = new JsonObject();
                sacrificeObj.addProperty("tag", this.entityToSacrificeTag.location().toString());
                sacrificeObj.addProperty("display_name", this.entityToSacrificeDisplayName);
                json.add("entity_to_sacrifice", sacrificeObj);
            }

            if (this.entityToSummon != null) {
                json.addProperty("entity_to_summon", this.entityToSummon.toString());
            }

            JsonObject dummyObj = new JsonObject();
            dummyObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.ritualDummy.getItem())).toString());
            json.add("ritual_dummy", dummyObj);

            JsonArray ingredientsArray = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                ingredientsArray.add(ingredient.toJson());
            }
            json.add("ingredients", ingredientsArray);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result.getItem())).toString());
            if (this.result.getCount() > 1) {
                resultObj.addProperty("count", this.result.getCount());
            }
            json.add("result", resultObj);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RitualRecipe.SERIALIZER;
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