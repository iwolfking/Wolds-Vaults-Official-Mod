package xyz.iwolfking.woldsvaults.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
public class ServerKiller extends AbstractAppender {
    private boolean isShuttingDown = false;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });

    protected ServerKiller() {
        super("WoldsLogListener", null, null);
    }


    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        if (event.getLoggerName().contains("net.minecraft.server.MinecraftServer") && message.contains("Stopping server")) {
            System.out.println("[Wold's Vaults] Stopping server detected.");
            this.isShuttingDown = true;
        }
        if (this.isShuttingDown && message.contains("All dimensions are saved")) {
            System.out.println("[Wold's Vaults] Instance will shut down in 30 seconds.");
            scheduler.schedule(() -> {
                System.out.println("[Wold's Vaults] Forcing shut down.");
                ServerKiller.forceKillProcess();
            }, 30L, TimeUnit.SECONDS);
        }
    }

    public static void register() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        ServerKiller listener = new ServerKiller();
        rootLogger.addAppender((Appender)listener);
        listener.start();
    }

    public static void forceKillProcess() {
        try {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            String pid = runtimeMxBean.getName().split("@")[0];
            LOGGER.warn("Forcing process termination. PID: " + pid);
            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Failed to kill process: " + e.getMessage(), e);
        }
    }
}
