package xyz.iwolfking.woldsvaults.api.helper;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.modifiers.vault.DifficultyLockModifier;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

public class NormalizedHelper {
    public static void handleAddingNormalizedToVault(Vault vault, ServerLevel level) {
        if(!level.getGameRules().getBoolean(ModGameRules.NORMALIZED_ENABLED)) {
            return;
        }

        boolean hasGeneratedModifiers = false;

        for(VaultModifier<?> modifier : vault.get(Vault.MODIFIERS).getModifiers()) {
            if(modifier.getId().equals(VaultMod.id("normalized")) || modifier instanceof DifficultyLockModifier) {
                hasGeneratedModifiers = true;
                break;
            }
        }

        if(!hasGeneratedModifiers) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("normalized"), 1);
        }
    }
}
