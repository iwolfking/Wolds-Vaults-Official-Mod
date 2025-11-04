package xyz.iwolfking.woldsvaults.api.core.vault_events;


import iskallia.vault.core.vault.Vault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.api.core.vault_events.impl.tasks.DelayTask;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.VaultEventTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber
public class DelayedSequenceHandler {

    private static final List<ActiveSequence> ACTIVE_SEQUENCES = new ArrayList<>();

    public static void startSequence(List<VaultEventTask> tasks, BlockPos pos, ServerPlayer player, Vault vault) {
        ACTIVE_SEQUENCES.add(new ActiveSequence(tasks, pos, player, vault));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Iterator<ActiveSequence> iterator = ACTIVE_SEQUENCES.iterator();
            while (iterator.hasNext()) {
                ActiveSequence seq = iterator.next();

                // Countdown waiting
                if (seq.waitTicks > 0) {
                    seq.waitTicks--;
                    continue;
                }

                // If we still have tasks left, run the next one
                if (!seq.tasks.isEmpty()) {
                    VaultEventTask next = seq.tasks.remove(0);
                    next.performTask(seq.pos, seq.player, seq.vault);

                    // If it’s a delayed task, set a delay
                    if (next instanceof DelayTask delayed) {
                        seq.waitTicks = delayed.getDelay();
                    }
                } else {
                    iterator.remove(); // Sequence complete
                }
            }
        }
    }

    private static class ActiveSequence {
        final List<VaultEventTask> tasks;
        final BlockPos pos;
        final ServerPlayer player;
        final Vault vault;
        int waitTicks = 0;

        ActiveSequence(List<VaultEventTask> tasks, BlockPos pos, ServerPlayer player, Vault vault) {
            this.tasks = new ArrayList<>(tasks);
            this.pos = pos;
            this.player = player;
            this.vault = vault;
        }
    }
}