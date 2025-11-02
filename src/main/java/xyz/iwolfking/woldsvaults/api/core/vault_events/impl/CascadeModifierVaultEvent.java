package xyz.iwolfking.woldsvaults.api.core.vault_events.impl;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.Objectives;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.objectives.EnchantedElixirObjective;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.BasicVaultEvent;

public class CascadeModifierVaultEvent extends BasicVaultEvent {
    public CascadeModifierVaultEvent(String eventName, String eventDescription, String primaryColor) {
        super(eventName, eventDescription, primaryColor);
    }

    @Override
    public void triggerEvent(BlockPos pos, ServerPlayer player, Vault vault) {
        Objectives objectives = vault.get(Vault.OBJECTIVES);
        objectives.forEach(EnchantedElixirObjective.class, enchantedElixirObjective -> {
            enchantedElixirObjective.setShouldCascadeRandomly(true);
            return true;
        });
        super.triggerEvent(pos, player, vault);
    }
}
