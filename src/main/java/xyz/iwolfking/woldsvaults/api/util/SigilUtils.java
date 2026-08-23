package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.config.sigil.SigilDefinitionsConfig;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

import javax.annotation.Nullable;
import java.util.Optional;
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

    /**
     * The vault's difficulty as a multiplier: 1.0 plus the sigil's {@code difficulty} from
     * {@code sigils/definitions.json}. An unknown sigil contributes nothing.
     */
    public static float getDifficultyMultiplier(@Nullable String sigil) {
        if(sigil == null) {
            return 1.0F;
        }

        Optional<SigilDefinitionsConfig.SigilDefinition> definition = ModConfigs.SIGIL_DEFINITIONS.get(sigil.toLowerCase());

        if(definition.isEmpty()) {
            WoldsVaults.LOGGER.warn("Sigil '{}' has no entry in sigils/definitions.json, counting its difficulty as 0", sigil);
            return 1.0F;
        }

        return 1.0F + definition.get().getDifficulty();
    }

    /**
     * As {@link #getDifficultyMultiplier(String)}, multiplied by the vault medallion's objective difficulty bonus;
     * a null vault gives the sigil-only multiplier.
     */
    public static float getDifficultyMultiplier(@Nullable String sigil, @Nullable Vault vault) {
        float multiplier = getDifficultyMultiplier(sigil);
        if(vault == null) {
            return multiplier;
        }
        return multiplier * (float) GreedMedallionEffects.objectiveDifficultyMultiplier(vault);
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
