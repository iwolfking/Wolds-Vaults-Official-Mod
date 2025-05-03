package xyz.iwolfking.woldsvaults.config.recipes.weaving;

import iskallia.vault.config.entry.ItemEntry;
import iskallia.vault.config.entry.recipe.ConfigForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.TargetedModBox;

import java.util.List;

public class ConfigWeavingRecipe extends ConfigForgeRecipe<WeavingForgeRecipe> {
    public ConfigWeavingRecipe(ItemStack output) {
        super(output);
    }

    @Override
    public WeavingForgeRecipe makeRecipe() {
        ItemStack out = this.output.createItemStack();
        List<ItemStack> in = this.inputs.stream().map(ItemEntry::createItemStack).toList();
        return new WeavingForgeRecipe(this.id, out, in);
    }



}
