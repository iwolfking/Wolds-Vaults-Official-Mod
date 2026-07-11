package xyz.iwolfking.woldsvaults.integration.arsnouveau.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.hollingsworth.arsnouveau.setup.RecipeRegistry;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.InfusedCatalystItem;
import net.minecraft.nbt.CompoundTag;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class VaultCatalystInfusionRecipe extends EnchantingApparatusRecipe {
    public static final String RECIPE_ID = "vault_catalyst_infusion";

    private final ResourceLocation poolId;
    private final String infusionAction;
    private final int sizeOffset;

    public VaultCatalystInfusionRecipe(ResourceLocation id, List<Ingredient> pedestalItems, ResourceLocation poolId, String infusionAction, int sizeOffset, int manaCost) {
        this.id = id;
        this.pedestalItems = pedestalItems;
        this.poolId = poolId;
        this.infusionAction = infusionAction;
        this.sizeOffset = sizeOffset;
        this.sourceCost = manaCost;
        this.result = createOutput();
        this.reagent = Ingredient.of(ModItems.VAULT_CATALYST); 
    }

    @Override
    public RecipeType<?> getType() {
        return ArsRecipeTypes.CATALYST_APPARATUS_TYPE;
    }

    @Override
    public boolean isMatch(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile enchantingApparatusTile, @Nullable Player player) {
        pedestalItems = pedestalItems.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        return this.pedestalItems.size() == pedestalItems.size() && doItemsMatch(pedestalItems, this.pedestalItems) && doesReagentMatch(reagent, player);
    }

    public boolean doesReagentMatch(ItemStack stack, @Nullable Player player) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == ModItems.VAULT_CATALYST;
    }

    @Override
    public boolean doesReagentMatch(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.VAULT_CATALYST;
    }

    public ItemStack createOutput() {
        ItemStack infusedOutput = new ItemStack(ModItems.VAULT_CATALYST_INFUSED);

        int checkLevel = 0;

        Optional<CompoundTag> rolledData = ModConfigs.CATALYST.generate(this.poolId, checkLevel, ChunkRandom.ofNanoTime());

        if (rolledData.isPresent()) {
            infusedOutput.getOrCreateTag().merge(rolledData.get());
        } else {
            InfusedCatalystItem.setSize(infusedOutput, 10);
        }

        int calculatedSize = InfusedCatalystItem.getSize(infusedOutput).orElse(10) + sizeOffset;
        InfusedCatalystItem.setSize(infusedOutput, Math.max(0, calculatedSize));

        switch (infusionAction) {
            case "make_super" -> InfusedCatalystItem.setSuper(infusedOutput, true);
            case "make_greedy" -> InfusedCatalystItem.setGreedy(infusedOutput, true);
        }

        return infusedOutput;
    }

    @Override
    public ItemStack assemble(EnchantingApparatusTile inv) {
        return createOutput();
    }

    @Override
    public ItemStack getResult(List<ItemStack> pedestalItems, ItemStack reagent, EnchantingApparatusTile tile) {
        return assemble(tile);
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(ModItems.VAULT_CATALYST_INFUSED);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ArsRecipeSerializers.VAULT_GEAR_ENCHANTING_APPARATUS;
    }

    @Override
    public JsonElement asRecipe() {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", "woldsvaults:" + RECIPE_ID);
        jsonobject.addProperty("pool_id", poolId.toString());
        jsonobject.addProperty("infusion_action", infusionAction);
        jsonobject.addProperty("size_offset", sizeOffset);
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

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<VaultCatalystInfusionRecipe> {

        @Override
        public VaultCatalystInfusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ResourceLocation poolId = ResourceLocation.parse(GsonHelper.getAsString(json, "pool_id"));
            String infusionAction = GsonHelper.getAsString(json, "infusion_action", "roll_pool");
            int sizeOffset = GsonHelper.getAsInt(json, "size_offset", 0);
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
            return new VaultCatalystInfusionRecipe(recipeId, stacks, poolId, infusionAction, sizeOffset, manaCost);
        }

        @Nullable
        @Override
        public VaultCatalystInfusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ResourceLocation poolId = buffer.readResourceLocation();
            String infusionAction = buffer.readUtf();
            int sizeOffset = buffer.readInt();
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
            return new VaultCatalystInfusionRecipe(recipeId, stacks, poolId, infusionAction, sizeOffset, manaCost);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, VaultCatalystInfusionRecipe recipe) {
            buf.writeResourceLocation(recipe.poolId);
            buf.writeUtf(recipe.infusionAction);
            buf.writeInt(recipe.sizeOffset);
            buf.writeInt(recipe.getSourceCost());
            buf.writeInt(recipe.pedestalItems.size());
            for (Ingredient i : recipe.pedestalItems) {
                i.toNetwork(buf);
            }
        }
    }
}