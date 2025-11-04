package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.ArrayList;
import java.util.List;

public class WeightedTask implements VaultEventTask {

    private final WeightedList<VaultEventTask> tasks;

    public WeightedTask(WeightedList<VaultEventTask> tasks) {
        this.tasks = tasks;
    }


    @Override
    public void performTask(BlockPos pos, ServerPlayer player, Vault vault) {
        tasks.getRandom().ifPresent(vaultEventTask -> vaultEventTask.performTask(pos, player, vault));
    }

    public static class Builder {
        private final WeightedList<VaultEventTask> tasks = new WeightedList<>();

        public Builder task(VaultEventTask task, double weight) {
            tasks.add(task, weight);
            return this;
        }

        public WeightedTask build() {
            return new WeightedTask(tasks);
        }
    }

}
