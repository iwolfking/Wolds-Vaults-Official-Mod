package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.AugmentItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VaultAugmentInfusionRecipe extends EnchantingApparatusRecipe {
    public static final String RECIPE_ID = "vault_augment_infusion";

    public VaultAugmentInfusionRecipe(ResourceLocation id, List<Ingredient> pedestalItems, int manaCost) {
        this.id = id;
        this.pedestalItems = pedestalItems;
        this.sourceCost = manaCost;
        this.result = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.INFUSED_AUGMENT);
        this.reagent = Ingredient.of(ModItems.AUGMENT);
    }

    @Override
    public RecipeType<?> getType() {
        return ArsRecipeTypes.AUGMENT_INFUSION_APPARATUS_TYPE;
    }

    @Override
    public boolean isMatch(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile, @Nullable Player player) {
        pedestalItems = pedestalItems.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        return this.pedestalItems.size() == pedestalItems.size() && doItemsMatch(pedestalItems, this.pedestalItems) && doesReagentMatch(reagent, player);
    }

    public boolean doesReagentMatch(ItemStack stack, @Nullable Player player) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof AugmentItem && AugmentItem.getTheme(stack).isPresent();
    }

    @Override
    public boolean doesReagentMatch(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AugmentItem && AugmentItem.getTheme(stack).isPresent();
    }

    public ItemStack createOutput(ItemStack inputReagent) {
        ItemStack infusedOutput = new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.INFUSED_AUGMENT);
        
        AugmentItem.getTheme(inputReagent).ifPresent(themeKey -> {
            infusedOutput.getOrCreateTag().putString("theme", themeKey.getId().toString());
        });

        return infusedOutput;
    }

    @Override
    public ItemStack assemble(EnchantingApparatusTile inv) {
        ItemStack reagentStack = inv.catalystItem;
        return createOutput(reagentStack);
    }

    @Override
    public ItemStack getResult(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile tile) {
        return createOutput(reagent);
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.INFUSED_AUGMENT);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ArsRecipeSerializers.VAULT_AUGMENT_INFUSION;
    }

    @Override
    public JsonElement asRecipe() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", "woldsvaults:" + RECIPE_ID);
        jsonobject.addProperty("sourceCost", getSourceCost());

        JsonArray pedestalArr = new JsonArray();
        for (Ingredient i : this.pedestalItems) {
            JsonObject object = new JsonObject();
            object.add("item", i.toJson());
            pedestalArr.add(object);
        }
        jsonobject.add("pedestalItems", pedestalArr);
        return jsonobject;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<VaultAugmentInfusionRecipe> {

        @Override
        public VaultAugmentInfusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            int manaCost = GsonHelper.getAsInt(json, "sourceCost", 0);
            JsonArray pedestalItems = GsonHelper.getAsJsonArray(json, "pedestalItems");
            List<Ingredient> stacks = new ArrayList<>();

            for (JsonElement e : pedestalItems) {
                JsonObject obj = e.getAsJsonObject();
                Ingredient input = GsonHelper.isArrayNode(obj, "item")
                        ? Ingredient.fromJson(GsonHelper.getAsJsonArray(obj, "item"))
                        : Ingredient.fromJson(GsonHelper.getAsJsonObject(obj, "item"));
                stacks.add(input);
            }
            return new VaultAugmentInfusionRecipe(recipeId, stacks, manaCost);
        }

        @Nullable
        @Override
        public VaultAugmentInfusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int manaCost = buffer.readInt();
            int length = buffer.readInt();
            List<Ingredient> stacks = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                try {
                    stacks.add(Ingredient.fromNetwork(buffer));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            return new VaultAugmentInfusionRecipe(recipeId, stacks, manaCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, VaultAugmentInfusionRecipe recipe) {
            buf.writeInt(recipe.getSourceCost());
            buf.writeInt(recipe.pedestalItems.size());
            for (Ingredient i : recipe.pedestalItems) {
                i.toNetwork(buf);
            }
        }
    }
}