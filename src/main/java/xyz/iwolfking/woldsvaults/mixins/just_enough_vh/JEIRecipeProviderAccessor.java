package xyz.iwolfking.woldsvaults.mixins.just_enough_vh;

import dev.attackeight.just_enough_vh.jei.JEIRecipeProvider;
import iskallia.vault.config.entry.ChanceItemStackEntry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = JEIRecipeProvider.class, remap = false)
public interface JEIRecipeProviderAccessor {
    @Invoker
    static ItemStack invokeAddLoreToRecyclerOutput(ChanceItemStackEntry entry) {throw new UnsupportedOperationException();}
}
