package xyz.iwolfking.woldsvaults.mixins.jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.init.ModItems;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.common.load.registration.SubtypeRegistration;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "jei")
        }
)
@Mixin(value = SubtypeRegistration.class, remap = false)
public class MixinSubtypeRegistration {
    @WrapOperation(method = "useNbtForSubtypes([Lnet/minecraft/world/item/Item;)V",at = @At(value = "INVOKE", target = "Lmezz/jei/common/load/registration/SubtypeRegistration;registerSubtypeInterpreter(Lnet/minecraft/world/item/Item;Lmezz/jei/api/ingredients/subtypes/IIngredientSubtypeInterpreter;)V"))
    private void removeNbtInterpretersForPackedDataItems(SubtypeRegistration instance, Item item, IIngredientSubtypeInterpreter iIngredientSubtypeInterpreter, Operation<Void> original) {
        if (item == ModItems.TRINKET || item == ModItems.ETCHING) {
            return;
        }
//        if (item instanceof IdentifiableItem) {
//            WoldLogger.error("Attempted to use NBT subtype interpreter for identifiable item {}", item.getRegistryName().toString());
//        }
        original.call(instance, item, iIngredientSubtypeInterpreter);
    }
}
