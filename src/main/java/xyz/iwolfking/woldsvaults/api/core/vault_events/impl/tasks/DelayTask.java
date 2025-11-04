package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

public class DelayTask implements VaultEventTask {
    private final int delay;

    public DelayTask(int delay) {
        this.delay = delay;
    }

    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        WoldsVaults.LOGGER.debug("Delaying Vault Task for {} ticks", delay);
    }

    public int getDelay() {
        return delay;
    }
}
