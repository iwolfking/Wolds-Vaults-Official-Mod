package xyz.iwolfking.woldsvaults.integration.mekanism.init.recipe;

import iskallia.vault.core.card.Card;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.CardItem;
import mekanism.api.annotations.NonNull;
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
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.MekanismRecipeDeserializers;
import xyz.iwolfking.woldsvaults.integration.mekanism.init.ModPigments;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
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

            if(this.getChemicalInput().testType(ModPigments.FOIL_PIGMENT.getChemical())) {
                if(!card.getGroups().contains("Foil")) {
                    if(!card.getEntries().isEmpty()) {
                        card.getEntries().get(0).getGroups().add("Foil");
                    }
                }
            }

            card.getEntries().forEach(cardEntry -> {
                if(!cardEntry.getColors().isEmpty()) {
                    cardEntry.setColors(Set.of(targetColor));
                }
            });

            CardItem.setCard(result, card);
        }

        return result;
    }

    @Override
    public @NotNull List<@NonNull ItemStack> getOutputDefinition() {
        Card card = ModConfigs.BOOSTER_PACK.getOutcomes("the_vault:stat_pack", JavaRandom.ofInternal(123L)).get(0);

        if(this.getChemicalInput().testType(ModPigments.FOIL_PIGMENT.getChemical())) {
            if(!card.getGroups().contains("Foil")) {
                if(!card.getEntries().isEmpty()) {
                    card.getEntries().get(0).getGroups().add("Foil");
                }
            }
        }
        else {
            card.getEntries().forEach(cardEntry -> {
                if(!cardEntry.getColors().isEmpty()) {
                    cardEntry.setColors(Set.of(targetColor));
                }
            });
        }

        return List.of(CardItem.create(card));
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