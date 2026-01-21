package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicInteger;

public class SigilUtils {
    public static void addStacksFromSigil(Vault vault) {
        Modifiers vaultModifiers = vault.get(Vault.MODIFIERS);

        //Vault already has had challenge stacks added, abort.
        if(vaultModifiers.hasModifier(VaultMod.id("challenged"))) {
            return;
        }

        AtomicInteger stackCount = new AtomicInteger(0);
        vaultModifiers.getModifiers().forEach(vaultModifier -> {
            if(isSigilModifier(vaultModifier.getId())) {
                stackCount.getAndAdd(getStackCountForSigil(vaultModifier.getId().getPath()));
            }
        });

        if(stackCount.get() != 0) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("challenge_stack"), stackCount.get());
        }
    }

    public static boolean isSigilModifier(ResourceLocation modifierId) {
        return modifierId.getPath().contains("sigil");
    }

    public static int getStackCountForSigil(String sigil) {
        return switch (sigil) {
            case "sigil_adept" -> 3;
            case "sigil_expert" -> 6;
            case "sigil_master" -> 9;
            case "sigil_veteran" -> 12;
            case "sigil_legend" -> 16;
            default -> 0;
        };
    }


}
