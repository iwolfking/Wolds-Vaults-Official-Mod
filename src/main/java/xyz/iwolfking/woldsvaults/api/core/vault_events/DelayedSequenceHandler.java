package xyz.iwolfking.woldsvaults.api.core.vault_events;


import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
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
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class DelayedSequenceHandler {

    private static final List<ActiveSequence> ACTIVE_SEQUENCES = new ArrayList<>();
    private static final List<ActiveSequence> PENDING_SEQUENCES = new ArrayList<>();

    private static boolean processingTick = false;

    public static void startSequence(List<VaultEventTask> tasks, Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
        ActiveSequence seq = new ActiveSequence(tasks, pos, player, vault);

        if (processingTick) {
            PENDING_SEQUENCES.add(seq);
        } else {
            ACTIVE_SEQUENCES.add(seq);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        processingTick = true;

        Iterator<ActiveSequence> iterator = ACTIVE_SEQUENCES.iterator();
        while (iterator.hasNext()) {
            ActiveSequence seq = iterator.next();

            if (seq.waitTicks > 0) {
                seq.waitTicks--;
                continue;
            }

            if (!seq.tasks.isEmpty()) {
                VaultEventTask next = seq.tasks.remove(0);
                if(seq.player != null && seq.vault != null) {
                    next.performTask(seq.pos, seq.player, seq.vault);
                }

                if (next instanceof DelayTask delayed) {
                    seq.waitTicks = delayed.getDelay();
                }
            } else {
                iterator.remove();
            }
        }

        processingTick = false;

        if (!PENDING_SEQUENCES.isEmpty()) {
            ACTIVE_SEQUENCES.addAll(PENDING_SEQUENCES);
            PENDING_SEQUENCES.clear();
        }
    }

    private static class ActiveSequence {
        final List<VaultEventTask> tasks;
        final Supplier<BlockPos> pos;
        final ServerPlayer player;
        final Vault vault;
        int waitTicks = 0;

        ActiveSequence(List<VaultEventTask> tasks, Supplier<BlockPos> pos, ServerPlayer player, Vault vault) {
            this.tasks = new ArrayList<>(tasks);
            this.pos = pos;
            this.player = player;
            this.vault = vault;
        }
    }
}