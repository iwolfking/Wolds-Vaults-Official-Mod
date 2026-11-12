package xyz.iwolfking.woldsvaults.builds;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/** Runs build follow-up work a few server ticks later, after curio and pouch ticks have seen the new items. */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class BuildTasks {
    private static final List<Task> TASKS = new ArrayList<>();

    private BuildTasks() {
    }

    public static synchronized void later(int ticks, Runnable action) {
        TASKS.add(new Task(Math.max(1, ticks), action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        synchronized (BuildTasks.class) {
            Iterator<Task> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                task.ticks--;
                if (task.ticks <= 0) {
                    due.add(task.action);
                    iterator.remove();
                }
            }
        }
        for (Runnable action : due) {
            try {
                action.run();
            } catch (RuntimeException | LinkageError e) {
                WoldLogger.error("[wvbuilds] A delayed build task failed", e);
            }
        }
    }

    private static final class Task {
        private int ticks;
        private final Runnable action;

        private Task(int ticks, Runnable action) {
            this.ticks = ticks;
            this.action = action;
        }
    }
}
