package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.recipe.AnvilContext;
import iskallia.vault.item.crystal.recipe.CatalystAnvilRecipe;
import iskallia.vault.item.crystal.recipe.CharmAnvilRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CharmAnvilRecipe.class, remap = false)
public class MixinCharmAnvilRecipe {
    @Inject(method = "lambda$onSimpleCraft$2", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultModifierPoolsConfig;getRandom(Lnet/minecraft/resources/ResourceLocation;ILiskallia/vault/core/random/RandomSource;)Ljava/util/List;", ordinal = 0), cancellable = true)
    private static void cancelCraftingOverCapacity(CrystalData crystal, ItemStack output, Integer crystaLevel, CallbackInfo ci) {
        ci.cancel();
    }
}
