package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.jei;

import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.integration.jei.VaultRecyclerRecipeJEI;
import iskallia.vault.integration.jei.VaultRecyclerRecipeJEICategory;
import iskallia.vault.item.InscriptionItem;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.CharmItem;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.item.gear.VoidStoneItem;
import iskallia.vault.item.tool.JewelItem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.vhapi.api.data.api.CustomRecyclerOutputs;

import java.util.List;

import static iskallia.vault.integration.jei.VaultRecyclerRecipeJEICategory.addTooltip;

@Mixin(value = VaultRecyclerRecipeJEICategory.class, remap = false)
public class MixinVaultRecyclerRecipeJEICategory {
    /**
     * @author radimous
     * @reason support custom outputs in recycler jei category
     */
    @Overwrite
    public void setRecipe(IRecipeLayoutBuilder builder, VaultRecyclerRecipeJEI recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addItemStack(recipe.getInput()).addTooltipCallback(addTooltip(
            List.of(new TextComponent("Output item's chance and quantity is based off this items quality"))));
        if (recipe.getInput().getItem() instanceof TrinketItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getTrinketRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getTrinketRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getTrinketRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof JewelItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getJewelRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getJewelRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getJewelRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof InscriptionItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getInscriptionRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getInscriptionRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getInscriptionRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof MagnetItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getMagnetRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getMagnetRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getMagnetRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof CharmItem || recipe.getInput().getItem() instanceof VaultCharmItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getCharmRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getCharmRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getCharmRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof VoidStoneItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getVoidStoneRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getVoidStoneRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getVoidStoneRecyclingOutput(), 2)));
            return;
        }

        if (recipe.getInput().getItem() instanceof VaultGearItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getGearRecyclingOutput(), 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getGearRecyclingOutput(), 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), ModConfigs.VAULT_RECYCLER.getGearRecyclingOutput(), 2)));
            return;
        }

        if(CustomRecyclerOutputs.CUSTOM_OUTPUTS.containsKey(recipe.getInput().getItem().getRegistryName())) {
            var outputs = CustomRecyclerOutputs.CUSTOM_OUTPUTS.get(recipe.getInput().getItem().getRegistryName());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 5).addItemStack(recipe.getOutput1()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), outputs, 0)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 67, 5).addItemStack(recipe.getOutput2()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), outputs, 1)));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 5).addItemStack(recipe.getOutput3()).addTooltipCallback(addTooltip(VaultRecyclerRecipeJEI.getRelatedTooltip(recipe.getInput(), outputs, 2)));
            return;
        }
    }
}
