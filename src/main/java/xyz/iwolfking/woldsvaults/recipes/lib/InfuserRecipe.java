package xyz.iwolfking.woldsvaults.recipes.lib;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.iwolfking.woldsvaults.init.ModRecipeSerializers;
import xyz.iwolfking.woldsvaults.init.ModRecipeTypes;

public class InfuserRecipe implements Recipe<Container> {
    private final ResourceLocation recipeId;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final int inputCount;
    private final Ingredient catalyst;
    private final int infuseDuration;


    public InfuserRecipe(ResourceLocation recipeId, Ingredient input, ItemStack output, int inputCount, Ingredient catalyst, int infuseDuration) {
        this.recipeId = recipeId;
        this.inputs = NonNullList.of(Ingredient.EMPTY, input);
        this.output = output;
        this.inputCount = inputCount;
        this.catalyst = catalyst;
        this.infuseDuration = infuseDuration;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }


    public ItemStack getResultItem() {
        return this.output;
    }


    public NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }


    public ResourceLocation getId() {
        return this.recipeId;
    }


    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFUSER;
    }


    public RecipeType<?> getType() {
        return ModRecipeTypes.INFUSER;
    }


    public ItemStack assemble(IItemHandler inventory) {
        return this.output.copy();
    }

    public ItemStack assemble(Container inv) {
        return this.output.copy();
    }


    public boolean matches(IItemHandler inventory) {
        var input = inventory.getStackInSlot(0);
        var catalyst = inventory.getStackInSlot(1);

        return this.inputs.get(0).test(input) && this.catalyst.test(catalyst);
    }


    public boolean matches(Container inv, Level level) {
        return this.matches(new InvWrapper(inv));
    }


    public int getInputCount() {
        return this.inputCount;
    }


    public Ingredient getCatalyst() {
        return this.catalyst;
    }


    public int getInfuseDuration() {
        return this.infuseDuration;
    }



    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<InfuserRecipe> {
        @Override
        public InfuserRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            var input = Ingredient.fromJson(json.getAsJsonObject("ingredient"));
            var output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int inputCount = GsonHelper.getAsInt(json, "inputCount", 10000);
            var catalyst = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"));
            int infuseDuration = GsonHelper.getAsInt(json, "infuseDuration");

            return new InfuserRecipe(recipeId, input, output, inputCount, catalyst, infuseDuration);
        }

        @Override
        public InfuserRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            var input = Ingredient.fromNetwork(buffer);
            var output = buffer.readItem();
            int inputCount = buffer.readInt();
            var catalyst = Ingredient.fromNetwork(buffer);
            int infuseDuration = buffer.readInt();

            return new InfuserRecipe(recipeId, input, output, inputCount, catalyst, infuseDuration);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, InfuserRecipe recipe) {
            recipe.inputs.get(0).toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeInt(recipe.inputCount);
            recipe.catalyst.toNetwork(buffer);
            buffer.writeInt(recipe.infuseDuration);
        }
    }
}
