package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.PylonExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;
import java.util.function.Supplier;

public class AddVaultTimeTask implements VaultEventTask {

    private final int ticksToAdd;

    public AddVaultTimeTask(int ticksToAdd) {
        this.ticksToAdd = ticksToAdd;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        if(vault.has(Vault.CLOCK)) {
            TickClock clock = vault.get(Vault.CLOCK);
            clock.addModifier(new PylonExtension(player, ticksToAdd));
        }
    }

}
