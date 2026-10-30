package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketHelper;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.CombinedTrinketItem;

import java.util.List;
import java.util.Optional;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;

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

    @Inject(method = "getTrinkets(Ljava/util/Map;Ljava/lang/Class;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static <T extends TrinketEffect<?>> void addPouchTrinkets(Map<String, List<Tuple<ItemStack, Integer>>> slots,
            Class<? super T> effectClass, CallbackInfoReturnable<List<TrinketHelper.TrinketStack<T>>> callback) {
        List<TrinketHelper.TrinketStack<T>> result = null;
        for (Tuple<ItemStack, Integer> entry : slots.getOrDefault("trinket_pouch", List.of())) {
            if (entry.getB() == 0 && PouchHelper.isPouch(entry.getA())) {
                PouchContents contents = PouchCapability.get(entry.getA());
                for (int index : PouchHelper.validSelection(entry.getA(), contents, contents.activeIndices())) {
                    if (result == null) {
                        result = new ArrayList<>(callback.getReturnValue());
                    }
                    ItemStack stack = contents.getStackInSlot(index);
                    result.addAll(TrinketHelper.getTrinkets(Map.of(PouchHelper.color(stack), List.of(new Tuple<>(stack, index))), effectClass));
                }
            }
        }
        if (result != null) {
            callback.setReturnValue(result);
        }
    }
}
