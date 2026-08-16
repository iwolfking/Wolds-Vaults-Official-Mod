package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.CrystalWorkbenchTileEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.items.GreedMedallionItem;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;

@Mixin(value = CrystalWorkbenchTileEntity.class, remap = false)
public class MixinCrystalWorkbenchTileEntity {

    /**
     * Grows the unique-ingredient inventory from six slots to seven so the workbench has somewhere
     * to hold a greed medallion. 6 is the only occurrence of that constant in the constructor
     * (verified against the shipped 3.21.6 bytecode), so the constant match is unambiguous.
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 6))
    private int addMedallionSlot(int uniqueIngredientCount) {
        return uniqueIngredientCount + 1;
    }

    @Inject(method = "lambda$new$2", at = @At(value = "HEAD"), cancellable = true)
    private static void addAdditionalUniqueIngredients(Integer slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(slot == 4) {
            cir.setReturnValue(stack.is(ModItems.LAYOUT_MANIPULATOR));
        }
        else if(slot == 5) {
            cir.setReturnValue(stack.getItem() instanceof IVaultCrystalModifier);
        }
        else if(slot == 6) {
            cir.setReturnValue(stack.getItem() instanceof GreedMedallionItem);
        }
    }

}
