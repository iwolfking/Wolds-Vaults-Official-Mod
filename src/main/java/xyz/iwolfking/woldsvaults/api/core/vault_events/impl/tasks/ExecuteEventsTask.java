package xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.function.Supplier;

public class ExecuteEventsTask implements VaultEventTask {

    private final Supplier<WeightedList<VaultEvent>> events;
    private final int count;

    public ExecuteEventsTask(Supplier<WeightedList<VaultEvent>> events, int count) {
        this.events = events;
        this.count = count;
    }

    @Override
    public void performTask(Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        for(int i = 0; i < count; i++) {
            events.get().getRandom().ifPresent(vaultEvent -> vaultEvent.triggerEvent(pos, player, vault));
        }
    }

    public static class Builder {
        private WeightedList<VaultEvent> events = new WeightedList<>();
        private int count = 1;

        public Builder event(VaultEvent event, double weight) {
            this.events.add(event, weight);
            return this;
        }

        public Builder events(WeightedList<VaultEvent> events) {
            this.events = events;
            return this;
        }

        public Builder amount(int count) {
            this.count = count;
            return this;
        }

        public ExecuteEventsTask build() {
            return new ExecuteEventsTask(() -> events, count);
        }
    }
}
