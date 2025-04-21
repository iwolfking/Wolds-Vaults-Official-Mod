package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.crafting.recipe.CatalystForgeRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mixin(value = CatalystForgeRecipe.class, remap = false)
public abstract class MixinCatalystForgeRecipe extends VaultForgeRecipe {
    protected MixinCatalystForgeRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }

    @Override
    public boolean canCraft(Player player) {
        if(this.getId().equals(WoldsVaults.id("greed_rock"))) {
            return PlayerGreedData.get().get(player).hasCompletedHerald() || player.isCreative();
        }

        return true;
    }
}
