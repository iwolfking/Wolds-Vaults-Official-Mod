package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.gear.item.VaultGearItem;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EtchingHelper.class, remap = false)
public class MixinEtchingHelper {
    @WrapOperation(method = "getEtchings", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;hasData(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean checkEquipmentSlotValidity(ItemStack itemStack, Operation<Boolean> original, @Local(name = "slot") EquipmentSlot slot, @Local(argsOnly = true) LivingEntity entity) {
        if(original.call(itemStack)) {
            if(itemStack.getItem() instanceof VaultGearItem gear) {
                if(!gear.isIntendedForSlot(itemStack, slot)) {
                    if(slot.equals(EquipmentSlot.OFFHAND) && entity instanceof EntityPlayer_BetterCombat entityPlayerBetterCombat) {
                        if(entityPlayerBetterCombat.getCurrentAttack() != null && entityPlayerBetterCombat.getCurrentAttack().isOffHand()) {
                            return gear.isIntendedForSlot(itemStack, EquipmentSlot.MAINHAND);
                        }
                    }

                    return false;
                }
            }
        }

        return false;
    }
}
