package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DelayedExecutionHelper {
        private static final List<ScheduledTask> TASKS = new ArrayList<>();

        /** Runs {@code task} after {@code delayTicks} server ticks, under the cast that scheduled it. */
        public static void schedule(ServerLevel level, int delayTicks, Runnable task) {
            TASKS.add(new ScheduledTask(level, delayTicks, CastContext.capture(task)));
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                List<ScheduledTask> due = new ArrayList<>();
                Iterator<ScheduledTask> iter = TASKS.iterator();
                while (iter.hasNext()) {
                    ScheduledTask t = iter.next();
                    if (--t.ticksRemaining <= 0) {
                        due.add(t);
                        iter.remove();
                    }
                }
                for (ScheduledTask t : due) {
                    t.task.run();
                }
            }
        }

        private static class ScheduledTask {
            final ServerLevel level;
            int ticksRemaining;
            final Runnable task;

            ScheduledTask(ServerLevel level, int delayTicks, Runnable task) {
                this.level = level;
                this.ticksRemaining = delayTicks;
                this.task = task;
            }
        }

        public static void init() {
            MinecraftForge.EVENT_BUS.register(DelayedExecutionHelper.class);
        }
}
