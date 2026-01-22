package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicInteger;

public class SigilUtils {
    public static void addStacksFromSigil(Vault vault) {
        Modifiers vaultModifiers = vault.get(Vault.MODIFIERS);

        if(vaultModifiers.hasModifier(VaultMod.id("challenged"))) {
            return;
        }

        String sigil = vault.get(Vault.SIGIL);

        if(sigil != null) {
            int stackCount = getStackCountForSigil(sigil.toLowerCase());

            if(stackCount != 0) {
                VaultModifierUtils.addModifier(vault, VaultMod.id("challenge_stack"), stackCount);
                VaultModifierUtils.addModifier(vault, VaultMod.id("challenged"), 1);
            }
        }


    }

    public static int getStackCountForSigil(String sigil) {
        return switch (sigil) {
            case "adept" -> 3;
            case "expert" -> 6;
            case "master" -> 9;
            case "veteran" -> 12;
            case "legend" -> 16;
            default -> 0;
        };
    }


}
