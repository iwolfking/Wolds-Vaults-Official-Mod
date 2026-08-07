package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.items.CombinedTrinketItem;

import java.util.List;
import java.util.Optional;

@Mixin(value = TrinketHelper.class, remap = false)
public abstract class MixinTrinketHelper {
    @Shadow
    private static <T extends TrinketEffect<?>> void addMatchingTrinkets(List<TrinketHelper.TrinketStack<T>> list, @Nullable ItemStack stack, @Nullable TrinketEffect<?> effect, Class<? super T> clazz) {
    }

    @WrapOperation(method = "lambda$getTrinkets$3", at = @At(value = "INVOKE", target = "Liskallia/vault/item/gear/TrinketItem;getTrinket(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"))
    private static <T extends TrinketEffect<?>> Optional<T> addCombinedTrinkets(ItemStack stack, Operation<Optional<T>> original, @Local List<TrinketHelper.TrinketStack<T>> trinkets, @Local Class<? super T> trinketClass) {
        if(stack.getItem() instanceof CombinedTrinketItem) {
            CombinedTrinketItem.getTrinkets(stack).forEach(trinketEffect -> {
                addMatchingTrinkets(trinkets, stack, trinketEffect, trinketClass);
            });
            return Optional.empty();
        }
        return original.call(stack);
    }
}
