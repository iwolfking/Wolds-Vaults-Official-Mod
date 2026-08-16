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
     * The vault's final difficulty expressed as a multiplier: 1.0 for a sigil-less vault, plus
     * every difficulty source stacked on top of it. Only the crystal's sigil contributes today
     * (its value comes from {@code sigils/definitions.json}), so a +3.0 sigil yields 4.0 — the
     * 400% a player sees. A sigil id with no definition contributes nothing and is logged.
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
     * The vault's final difficulty multiplier including its greed medallion. The medallion's
     * "+X% Objective Difficulty" is multiplicative with the sigil, so a +3.0 sigil in a Legend
     * medallion vault reads 4.0 × 1.5 = 6.0. A null vault behaves exactly like
     * {@link #getDifficultyMultiplier(String)}.
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
