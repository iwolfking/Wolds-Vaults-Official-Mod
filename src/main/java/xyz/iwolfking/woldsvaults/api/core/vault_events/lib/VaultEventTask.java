package xyz.iwolfking.woldsvaults.api.core.vault_events.lib;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface VaultEventTask {

    void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault);

}
