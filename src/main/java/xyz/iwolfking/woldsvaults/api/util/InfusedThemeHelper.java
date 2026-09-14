package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.UUID;

public class InfusedThemeHelper {
    public static boolean handleThemeInfusion(Vault vault, ResourceLocation themeId, RandomSource random, String sigil) {
        if (themeId == null) {
            return false;
        }




        UUID vaultOwnerId = vault.get(Vault.OWNER);
        UUID vaultId = vault.get(Vault.ID);
        VaultDifficulty difficulty = VaultDifficulty.NORMAL;

        if (vaultOwnerId != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                if(GameruleHelper.isEnabled(ModGameRules.ENABLE_RANDOM_THEME_INFUSION, server.overworld())) {
                    return false;
                }

                //If this vault is a greed challenge vault, don't ever infuse the theme.
                PlayerGreedTreeData treeData = PlayerGreedTreeData.get(server);
                GreedTree greedTree = treeData.getGreedTree(vaultOwnerId);
                if (greedTree != null) {
                    GreedChallengeSlot activeSlot = greedTree.getActiveChallengeSlot();
                    if (activeSlot != null && vaultId.equals(activeSlot.getChallengeVaultId())) {
                        return false;
                    }
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
