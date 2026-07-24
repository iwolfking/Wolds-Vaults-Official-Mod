package xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.item.CardItem;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.MekanismRecipeDeserializers;

import javax.annotation.Nonnull;
import java.util.Set;

public class CardPaintingRecipe extends PaintingRecipe {

    private final CardEntry.Color targetColor;

    public CardPaintingRecipe(ResourceLocation id, 
                              ItemStackIngredient itemInput, 
                              ChemicalStackIngredient.PigmentStackIngredient pigmentInput, 
                              ItemStack output,
                              CardEntry.Color targetColor) {
        super(id, itemInput, pigmentInput, output);
        this.targetColor = targetColor;
    }

    public CardEntry.Color getTargetColor() {
        return this.targetColor;
    }

    @Override
    @Nonnull
    public ItemStack getOutput(ItemStack inputItem, PigmentStack inputPigment) {
        ItemStack result = inputItem.copy();
        result.setCount(1);

        if(result.getItem() instanceof CardItem) {
            Card card = iskallia.vault.item.CardItem.getCard(result);

            card.getEntries().forEach(cardEntry -> {
                if(!cardEntry.getColors().isEmpty()) {
                    cardEntry.setColors(Set.of(targetColor));
                }
            });

            CardItem.setCard(result, card);
        }

        return result;
    }

    @Nonnull
    @Override
    public RecipeType<PaintingRecipe> getType() {
        return MekanismRecipeType.PAINTING.get();
    }

    @Nonnull
    @Override
    public RecipeSerializer<CardPaintingRecipe> getSerializer() {
        return MekanismRecipeDeserializers.CARD_PAINTING.get();
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlocks.PAINTING_MACHINE.getName();
    }

    @Nonnull
    @Override
    public ItemStack getToastSymbol() {
        return MekanismBlocks.PAINTING_MACHINE.getItemStack();
    }
}