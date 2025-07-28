package xyz.iwolfking.woldsvaults.mixins.vhatcaniroll;

import com.llamalad7.mixinextras.sugar.Local;
import com.radimous.vhatcaniroll.logic.Items;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "vhatcaniroll")
        }
)
@Mixin(value = Items.class, remap = false)
public abstract class MixinItems {
//    @Inject(method = "getWoldGearItems", at = @At("RETURN"))
//    private static void addItems(CallbackInfoReturnable<List<ItemStack>> cir, @Local(ordinal = 0) List<ItemStack> woldItems) {
////        woldItems.add(withTransmog(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.MAP), new ResourceLocation("the_vault:gear/map/common")));
//
//        ItemStack etchingStack = new ItemStack(ModItems.ETCHING);
//        AttributeGearData data = AttributeGearData.read(etchingStack);
//        data.createOrReplaceAttributeValue(ModGearAttributes.STATE, VaultGearState.UNIDENTIFIED);
//        data.write(etchingStack);
//        woldItems.add(etchingStack);
//    }
}
