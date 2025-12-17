package xyz.iwolfking.woldsvaults.mixins.vaulthunters.recipes;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.crafting.recipe.JewelCraftingRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = JewelCraftingRecipe.class, remap = false)
public abstract class MixinJewelCraftingRecipe extends VaultForgeRecipe {
    protected MixinJewelCraftingRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }
}
