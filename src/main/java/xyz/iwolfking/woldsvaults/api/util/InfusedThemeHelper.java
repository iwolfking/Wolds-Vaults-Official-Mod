package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.milestones.trials.GreedTrials;

import java.util.UUID;

public class InfusedThemeHelper {
    private static UUID challengeVaultInConfigure;

    /**
     * Marks the vault a greed challenge crystal is configuring. The base mod links the challenge
     * slot to the vault only after creation, so the slot cannot identify it while the theme rolls.
     */
    public static void beginChallengeConfigure(UUID vaultId) {
        challengeVaultInConfigure = vaultId;
    }

    public static void endChallengeConfigure() {
        challengeVaultInConfigure = null;
    }

    public static boolean handleThemeInfusion(Vault vault, ResourceLocation themeId, RandomSource random, String sigil) {
        if (themeId == null) {
            return false;
        }

        if(VaultModifierUtils.hasCountOfModifiers(vault, WoldsVaults.id("dulled"), 1)) {
            return false;
        }
        if (challengeVaultInConfigure != null && challengeVaultInConfigure.equals(vault.get(Vault.ID))) {
            return false;
        }


        UUID vaultOwnerId = vault.get(Vault.OWNER);
        VaultDifficulty difficulty = VaultDifficulty.NORMAL;

        if (vaultOwnerId != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_RANDOM_THEME_INFUSION, server.overworld())) {
                    return false;
                }

                if (GreedTrials.isOpeningTrial() || GreedTrials.trialRank(vault) > 0) {
                    return false;
                }

                WorldSettings settings = WorldSettings.get(server.overworld());
                difficulty = settings.getPlayerDifficulty(vaultOwnerId);
            }
        }

        if (ThemeModifiersConfig.shouldRandomlyInfuseVaultTheme(themeId, vault.get(Vault.LEVEL).get(), difficulty)) {
            InfusedCrystalTheme infusedTheme = new InfusedCrystalTheme(themeId);
            infusedTheme.configure(vault, random, sigil);
            return true;
        }

        return false;
    }
}
