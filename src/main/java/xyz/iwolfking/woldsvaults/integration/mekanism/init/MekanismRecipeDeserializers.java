package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import mekanism.api.recipes.PaintingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe.CardPaintingRecipe;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe.CardPaintingSerializer;

public class MekanismRecipeDeserializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, WoldsVaults.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CardPaintingRecipe>> CARD_PAINTING =
            SERIALIZERS.register("card_painting", CardPaintingSerializer::new);

}
