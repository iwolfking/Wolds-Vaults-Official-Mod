package xyz.iwolfking.woldsvaults.api.util;

import xyz.iwolfking.woldsvaults.spellcraft.gear.EtchingGlyphs;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class WoldEtchingHelper {
    public static <T> boolean hasEtching(LivingEntity entity, VaultGearAttribute<T> attribute) {
        Boolean granted = EtchingGlyphs.has(entity, attribute);
        if (granted != null) {
            return granted;
        }
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && VaultGearData.hasData(stack)) {
                if(stack.getItem() instanceof VaultGearItem gear && !isEtchingActiveInSlot(entity, stack, gear, slot)) {
                    continue;
                }

                GearDataCache cache = GearDataCache.of(stack);
                if(cache.hasAttribute(attribute)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Whether the etching on a gear item in the given slot applies: the item is intended for that slot,
     * or it is a mainhand weapon held in the offhand while a Better Combat offhand attack is in progress.
     */
    public static boolean isEtchingActiveInSlot(LivingEntity entity, ItemStack stack, VaultGearItem gear, EquipmentSlot slot) {
        if(gear.isIntendedForSlot(stack, slot)) {
            return true;
        }

        if(slot.equals(EquipmentSlot.OFFHAND) && entity instanceof EntityPlayer_BetterCombat entityPlayerBetterCombat) {
            if(entityPlayerBetterCombat.getCurrentAttack() != null && entityPlayerBetterCombat.getCurrentAttack().isOffHand()) {
                return gear.isIntendedForSlot(stack, EquipmentSlot.MAINHAND);
            }
        }

        return false;
    }
}
