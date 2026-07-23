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
    public static final ResourceLocation RANDOM_CHALLENGE_INSCRIPTION = WoldsVaults.id("random_challenge_inscription");
    public static final ResourceLocation RANDOM_OMEGA_INSCRIPTION = WoldsVaults.id("random_omega_inscription");
    public static final ResourceLocation RANDOM_ANY_INSCRIPTION = WoldsVaults.id("random_any_inscription");
    public static final ResourceLocation RANDOM_RESOURCE_INSCRIPTION = WoldsVaults.id("random_resource_inscription");

    public static ItemStack invokeRitual(ResourceLocation type, ItemStack crystalStack) {
        ItemStack output = crystalStack.copy();

        CrystalData.run(output, data -> {
            if (type.equals(EXTENDED)) {
                timeExtensionRitual(data);
            } else if (type.equals(RANDOM_CHALLENGE_INSCRIPTION)) {
                randomInscription(data, output, WoldsVaults.id("challenge_rooms"), 0.5F);
            } else if (type.equals(RANDOM_OMEGA_INSCRIPTION)) {
                randomInscription(data, output, WoldsVaults.id("omega_rooms"), 0.75F);
            } else if (type.equals(RANDOM_ANY_INSCRIPTION)) {
                randomInscription(data, output, WoldsVaults.id("all_rooms"), 0.15F);
            } else if (type.equals(RANDOM_RESOURCE_INSCRIPTION)) {
                randomInscription(data, output, WoldsVaults.id("resource_rooms"), 0.1F);
            }
        });

        return output;
    }

    private static void timeExtensionRitual(CrystalData data) {
        CrystalDataUtils.addModifier(VaultMod.id("extended"), data, 1);
        CrystalDataUtils.addModifierFromPool(WoldsVaults.id("wendarr_curses"), data, 0, 1);
        if (CrystalDataUtils.hasCountOfModifiers(VaultMod.id("extended"), data, 7)) {
            data.getProperties().setUnmodifiable(true);
        }
    }

    private static void randomInscription(CrystalData data, ItemStack crystalStack, ResourceLocation poolId, float breakChance) {
        Random random = new Random();
        CrystalDataUtils.addInscriptionFromPool(poolId, data, crystalStack, 0);
        CrystalDataUtils.addModifierFromPool(WoldsVaults.id("tenos_curses"), data, 0,1);
        if(random.nextFloat() <= breakChance) {
            data.getProperties().setUnmodifiable(true);
        }
    }
}
