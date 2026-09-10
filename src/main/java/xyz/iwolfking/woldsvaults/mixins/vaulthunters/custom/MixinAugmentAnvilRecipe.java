package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.recipe.anvil.AugmentAnvilRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.items.InfusedAugmentItem;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PoolCrystalThemeAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ValueCrystalThemeAccessor;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;

@Mixin(value = AugmentAnvilRecipe.class, remap = false)
public class MixinAugmentAnvilRecipe {

    @Inject(method = "onSimpleCraft", at = @At(value = "INVOKE", target = "Liskallia/vault/item/AugmentItem;getTheme(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"), cancellable = true)
    private void preventOverridingMapThemes(AnvilContext context, CallbackInfoReturnable<Boolean> cir, @Local(name = "crystal") CrystalData crystal) {
        if (crystal.getTheme() instanceof PoolCrystalTheme poolCrystalTheme) {
            if (((PoolCrystalThemeAccessor) poolCrystalTheme).getId().getPath().contains("map_themes")) {
                cir.setReturnValue(false);
            }
        }
    }

    @WrapOperation(method = "onSimpleCraft", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 1), remap = true)
    private Item allowInfusedAugmentItems(ItemStack instance, Operation<Item> original) {
        if(instance.getItem() instanceof InfusedAugmentItem) {
            return iskallia.vault.init.ModItems.AUGMENT;
        }

        return original.call(instance);
    }

    @WrapOperation(
        method = "onSimpleCraft", 
        at = @At(
            value = "INVOKE", 
            target = "Liskallia/vault/item/crystal/CrystalData;setTheme(Liskallia/vault/item/crystal/theme/CrystalTheme;)V"
        )
    )
    private void applyInfusedThemeIfApplicable(
        CrystalData crystal, 
        CrystalTheme theme,
        Operation<Void> original, 
        @Local(name = "secondary") ItemStack secondary
    ) {
        if (secondary.getItem() instanceof InfusedAugmentItem && theme instanceof ValueCrystalTheme valueTheme) {
            original.call(crystal, new InfusedCrystalTheme(((ValueCrystalThemeAccessor) valueTheme).getId()));
        } else {
            original.call(crystal, theme);
        }
    }
}