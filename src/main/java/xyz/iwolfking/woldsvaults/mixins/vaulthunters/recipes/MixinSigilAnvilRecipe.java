package xyz.iwolfking.woldsvaults.mixins.vaulthunters.recipes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.SigilAnvilRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.GreedyVaultModifier;

@Mixin(value = SigilAnvilRecipe.class, remap = false)
public class MixinSigilAnvilRecipe {
    @Inject(method = "onSimpleCraft", at = @At(value = "INVOKE", target = "Liskallia/vault/item/SigilItem;getSigilId(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;"), cancellable = true)
    private void cancelIfGreedyRock(AnvilContext context, CallbackInfoReturnable<Boolean> cir, @Local CrystalData data) {
        for(VaultModifierStack modStack : data.getModifiers().getList()) {
            if(modStack.getModifier() instanceof GreedyVaultModifier) {
                cir.setReturnValue(false);
            }
            else if(modStack.getModifier() instanceof GroupedModifier groupedModifier) {
                for(VaultModifier<?> mod : groupedModifier.properties().getChildren()) {
                    if(mod instanceof GreedyVaultModifier) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}
