package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.CatalystAnvilRecipe;
import iskallia.vault.item.crystal.recipe.InscriptionAnvilRecipe;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CatalystAnvilRecipe.class, remap = false)
public class MixinCatalystAnvilRecipe {
    @Inject(method = "onSimpleCraft", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultModifierPoolsConfig;getRandom(Lnet/minecraft/resources/ResourceLocation;ILiskallia/vault/core/random/RandomSource;)Ljava/util/List;", ordinal = 0), cancellable = true)
    private void cancelCraftingOverCapacity(AnvilContext context, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
