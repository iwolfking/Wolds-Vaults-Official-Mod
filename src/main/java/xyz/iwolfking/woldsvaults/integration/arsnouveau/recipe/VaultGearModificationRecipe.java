package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import iskallia.vault.config.gear.VaultGearTagConfig;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.iwolfking.woldsvaults.init.ModRecipeSerializers;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VaultGearModificationRecipe extends EnchantingApparatusRecipe {
    public static final String RECIPE_ID = "vault_gear_enchanting";
    
    private final String modificationType;
    private final String targetTagGroup;

    public VaultGearModificationRecipe(ResourceLocation id, List<Ingredient> pedestalItems, String modificationType, @Nullable String targetTagGroup, int manaCost) {
        this.id = id;
        this.pedestalItems = pedestalItems;
        this.modificationType = modificationType;
        this.targetTagGroup = targetTagGroup;
        this.sourceCost = manaCost;
        this.result = new ItemStack(ModItems.CHESTPLATE);
        this.reagent = Ingredient.of(ModItems.CHESTPLATE, ModItems.BOOTS);
    }

    @Override
    public RecipeType<?> getType() {
        return ArsRecipeTypes.VAULT_GEAR_APPARATUS_TYPE;
    }

    @Override
    public boolean isMatch(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile, @Nullable Player player) {
        pedestalItems = pedestalItems.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        return this.pedestalItems.size() == pedestalItems.size() && doItemsMatch(pedestalItems, this.pedestalItems) && doesReagentMatch(reagent, player);
    }

    public boolean doesReagentMatch(ItemStack stack, @Nullable Player player) {
        if (stack.isEmpty()) return false;

        if (!VaultGearData.hasData(stack)) {
            if (player != null) PortUtil.sendMessage(player, new TranslatableComponent("ars_nouveau.enchanting.invalid_gear"));
            return false;
        }

        VaultGearData data = VaultGearData.read(stack);
        if (!data.isModifiable()) {
            if (player != null) PortUtil.sendMessage(player, new TranslatableComponent("ars_nouveau.enchanting.unmodifiable"));
            return false;
        }

        return true;
    }

    @Override
    public boolean doesReagentMatch(ItemStack stack) {
        return !stack.isEmpty() && VaultGearData.hasData(stack) && VaultGearData.read(stack).isModifiable();
    }

    @Override
    public ItemStack assemble(EnchantingApparatusTile inv) {
        ItemStack gearStack = inv.catalystItem.copy();
        Random random = inv.getLevel() != null ? inv.getLevel().random : new Random();

        switch (modificationType) {
            case "reforge_all" -> VaultGearModifierHelper.reForgeAllModifiers(gearStack, random);
            case "reforge_implicits" -> VaultGearModifierHelper.reForgeAllImplicits(gearStack, random);
            case "add_tag_group" -> {
                if (targetTagGroup != null) {
                    VaultGearTagConfig.ModTagGroup group = ModConfigs.VAULT_GEAR_TAG_CONFIG.getGroupTag(targetTagGroup);
                    if (group != null) {
                        VaultGearModifierHelper.addNewModifierOfGroup(group, gearStack, random);
                    }
                }
            }
        }
        return gearStack;
    }

    @Override
    public ItemStack getResult(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile tile) {
        return assemble(tile);
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(ModItems.CHESTPLATE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ArsRecipeSerializers.VAULT_GEAR_ENCHANTING_APPARATUS;
    }

    @Override
    public JsonElement asRecipe() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", "ars_nouveau:" + RECIPE_ID);
        jsonobject.addProperty("modification_type", modificationType);
        if (targetTagGroup != null) {
            jsonobject.addProperty("tag_group", targetTagGroup);
        }
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

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<VaultGearModificationRecipe> {

        @Override
        public VaultGearModificationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String modificationType = GsonHelper.getAsString(json, "modification_type");
            String tagGroup = json.has("tag_group") ? GsonHelper.getAsString(json, "tag_group") : null;
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
            return new VaultGearModificationRecipe(recipeId, stacks, modificationType, tagGroup, manaCost);
        }

        @Nullable
        @Override
        public VaultGearModificationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String modificationType = buffer.readUtf();
            boolean hasTagGroup = buffer.readBoolean();
            String tagGroup = hasTagGroup ? buffer.readUtf() : null;
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
            return new VaultGearModificationRecipe(recipeId, stacks, modificationType, tagGroup, manaCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, VaultGearModificationRecipe recipe) {
            buf.writeUtf(recipe.modificationType);
            buf.writeBoolean(recipe.targetTagGroup != null);
            if (recipe.targetTagGroup != null) {
                buf.writeUtf(recipe.targetTagGroup);
            }
            buf.writeInt(recipe.getSourceCost());
            buf.writeInt(recipe.pedestalItems.size());
            for (Ingredient i : recipe.pedestalItems) {
                i.toNetwork(buf);
            }
        }
    }
}