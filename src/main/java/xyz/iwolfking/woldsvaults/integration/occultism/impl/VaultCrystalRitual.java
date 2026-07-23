package xyz.iwolfking.woldsvaults.integration.occultism.impl;

import iskallia.vault.VaultMod;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.CrystalDataUtils;

import java.util.Random;

public class VaultCrystalRitual {
    public static final ResourceLocation EXTENDED = WoldsVaults.id("time_extension");

    public static ItemStack invokeRitual(ResourceLocation type, ItemStack crystalStack) {
        ItemStack output = crystalStack.copy();
        CrystalData data = CrystalData.read(output);

        if (type.equals(EXTENDED)) {
           return timeExtensionRitual(data, output);
        }

        return output;
    }

    private static ItemStack timeExtensionRitual(CrystalData data, ItemStack crystalStack) {
        CrystalDataUtils.addModifier(VaultMod.id("extended"), data, new Random().nextInt(1, 3));
        CrystalDataUtils.addModifierFromPool(WoldsVaults.id("wendarr_curses"), data, 0,1);
        if(CrystalDataUtils.hasCountOfModifiers(VaultMod.id("extended"), data, 7)) {
            data.getProperties().setUnmodifiable(true);
        }
        data.write(crystalStack);
        return crystalStack;
    }
}
