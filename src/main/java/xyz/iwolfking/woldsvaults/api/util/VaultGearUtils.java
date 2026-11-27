package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class VaultGearUtils {
    public static ItemStack gearWithTransmog(ItemStack stack, ResourceLocation transmog) {
        ItemStack displayStack = stack.copy();
        VaultGearData gearData = VaultGearData.read(displayStack);
        gearData.setState(VaultGearState.IDENTIFIED);
        gearData.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, transmog);
        gearData.write(displayStack);
        return displayStack;
    }
}
