package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import javax.annotation.Nullable;

public class SigilUtils {
    public static void addStacksFromSigil(Vault vault) {
        addStacks(vault, vault.has(Vault.SIGIL) ? vault.get(Vault.SIGIL) : null);
    }

    /**
     * Adds the challenge stacks the vault's difficulty calls for (see {@link ObjectiveDifficulty#challengeStacks}),
     * once per vault; the {@code challenged} marker records that it happened.
     */
    public static void addStacks(Vault vault, @Nullable String sigil) {
        Modifiers vaultModifiers = vault.get(Vault.MODIFIERS);

        if(vaultModifiers.hasModifier(VaultMod.id("challenged"))) {
            return;
        }

        int stackCount = ObjectiveDifficulty.challengeStacks(ObjectiveDifficulty.multiplier(sigil, vault));

        if(stackCount != 0) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("challenge_stack"), stackCount);
            VaultModifierUtils.addModifier(vault, VaultMod.id("challenged"), 1);
        }
    }

    /**
     * The vault's difficulty as a multiplier: 1.0 plus the sigil's {@code difficulty} from
     * {@code sigils/definitions.json}. An unknown sigil contributes nothing.
     */
    public static float getDifficultyMultiplier(@Nullable String sigil) {
        return (float) ObjectiveDifficulty.multiplier(sigil, null);
    }

    /**
     * As {@link #getDifficultyMultiplier(String)}, multiplied by the vault medallion's objective difficulty bonus;
     * a null vault gives the sigil-only multiplier.
     */
    public static float getDifficultyMultiplier(@Nullable String sigil, @Nullable Vault vault) {
        return (float) ObjectiveDifficulty.multiplier(sigil, vault);
    }
}
