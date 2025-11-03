package xyz.iwolfking.woldsvaults.api.core.vault_events.lib;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class VaultEventTask {

    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {

    }
}
