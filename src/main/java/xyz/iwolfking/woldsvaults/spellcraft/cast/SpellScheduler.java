package xyz.iwolfking.woldsvaults.spellcraft.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Runs the later parts of a spell (after Delay, a Charge wind-up, Echo) on the server tick. Pending parts are dropped
 * when the player logs out, dies, changes dimension or the server stops.
 */
public final class SpellScheduler {
    private static final List<Task> TASKS = new ArrayList<>();

    private SpellScheduler() {
    }

    public static void schedule(ServerPlayer player, int ticks, Consumer<ServerPlayer> action) {
        TASKS.add(new Task(player.getUUID(), ticks, action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) {
            return;
        }
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
            Task task = it.next();
            if (--task.ticksLeft <= 0) {
                due.add(task);
                it.remove();
            }
        }
        for (Task task : due) {
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(task.player);
            if (player != null && player.isAlive()) {
                task.action.accept(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancel(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        cancel(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        cancel(event.getEntityLiving().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TASKS.clear();
    }

    private static void cancel(UUID player) {
        TASKS.removeIf(task -> task.player.equals(player));
    }

    private static final class Task {
        final UUID player;
        final Consumer<ServerPlayer> action;
        int ticksLeft;

        Task(UUID player, int ticksLeft, Consumer<ServerPlayer> action) {
            this.player = player;
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }
}
