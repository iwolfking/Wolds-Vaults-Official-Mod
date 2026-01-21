package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.recipe.anvil.TreasureKeyAnvilRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(value = TreasureKeyAnvilRecipe.class, remap = false)
public class MixinTreasureKeyAnvilRecipe {
    @Redirect(method = "onSimpleCraft", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
        private boolean allowCraftingInVaults(Optional instance) {
            return false;
        }
}
