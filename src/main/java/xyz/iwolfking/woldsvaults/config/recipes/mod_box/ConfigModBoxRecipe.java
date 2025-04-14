package xyz.iwolfking.woldsvaults.config.recipes.mod_box;

import iskallia.vault.config.entry.ItemEntry;
import iskallia.vault.config.entry.recipe.ConfigForgeRecipe;
import iskallia.vault.item.AugmentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.items.TargetedModBox;

import java.util.List;

public class ConfigModBoxRecipe extends ConfigForgeRecipe<ModBoxForgeRecipe> {
    public ConfigModBoxRecipe(ResourceLocation id, String research) {
        super(id, TargetedModBox.create(research));
    }

    @Override
    public ModBoxForgeRecipe makeRecipe() {
        ItemStack out = this.output.createItemStack();
        List<ItemStack> in = this.inputs.stream().map(ItemEntry::createItemStack).toList();
        return new ModBoxForgeRecipe(this.id, out, in);
    }



}
