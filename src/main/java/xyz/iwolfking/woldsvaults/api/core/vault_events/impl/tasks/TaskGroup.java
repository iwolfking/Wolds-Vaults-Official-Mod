package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TaskGroup implements VaultEventTask {

    private final List<VaultEventTask> tasks;

    public TaskGroup(List<VaultEventTask> tasks) {
        this.tasks = tasks;
    }


    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        for(VaultEventTask task : tasks) {
            task.performTask(pos, player, vault);
        }
    }

    public static class Builder {
        private List<VaultEventTask> tasks = new ArrayList<>();

        public Builder task(VaultEventTask task) {
            tasks.add(task);
            return this;
        }

        public TaskGroup build() {
            return new TaskGroup(tasks);
        }


    }
}
