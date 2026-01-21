package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.DelayedSequenceHandler;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RepeatTask implements VaultEventTask {

    private final VaultEventTask task;
    private final int repeatDelay;
    private final int repeatCount;

    public RepeatTask(VaultEventTask task, int repeatDelay, int repeatCount) {
        this.task = task;
        this.repeatDelay = repeatDelay;
        this.repeatCount = repeatCount;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        List<VaultEventTask> taskList = new ArrayList<>();

        for(int i = 0; i < repeatCount; i++) {
            taskList.add(task);
            if(repeatDelay != 0) {
                taskList.add(new DelayTask(repeatDelay));
            }
        }

        DelayedSequenceHandler.startSequence(taskList, pos, player, vault);
    }
}
