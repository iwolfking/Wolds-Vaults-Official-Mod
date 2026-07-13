package xyz.iwolfking.woldsvaults.integration.occultism.init;


import net.minecraft.world.item.crafting.RecipeSerializer;
import xyz.iwolfking.woldsvaults.integration.occultism.AugmentRitualRecipe;


public class OccultismRecipeSerializers {
    public static final RecipeSerializer<?> AUGMENT_RITUAL = new AugmentRitualRecipe.Serializer();
}
