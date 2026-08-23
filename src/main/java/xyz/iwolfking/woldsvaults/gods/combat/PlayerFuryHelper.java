package xyz.iwolfking.woldsvaults.gods.combat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fury: the per-player resource behind Ultra Rampaging. It accrues and decays whether or not a
 * Rampage ability is toggled on, and is never persisted.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class PlayerFuryHelper {
    private static final int DECAY_INTERVAL_TICKS = 10;

    private static final Map<UUID, Float> FURY = new ConcurrentHashMap<>();

    private PlayerFuryHelper() {
    }

    public static float get(ServerPlayer player) {
        return FURY.getOrDefault(player.getUUID(), 0.0F);
    }

    /** Adds Fury and returns the new total, clamped to the configured cap; a non-positive amount is ignored. */
    public static float add(ServerPlayer player, float amount) {
        if (amount <= 0.0F) {
            if (amount < 0.0F) {
                WoldsVaults.LOGGER.error("Refusing negative Fury gain {} for {}; income sources must be positive.",
                        amount, player.getGameProfile().getName());
            }
            return get(player);
        }
        UltraRampagingConfig config = UltraRampaging.config();
        float updated = Math.min(get(player) + amount, config.furyCap());
        FURY.put(player.getUUID(), updated);
        UltraRampaging.onFuryChanged(player);
        return updated;
    }

    public static void clear(ServerPlayer player) {
        if (FURY.remove(player.getUUID()) != null) {
            UltraRampaging.onFuryChanged(player);
        }
    }

    /** The drain, run every {@value #DECAY_INTERVAL_TICKS} ticks; above {@code decayScaleFrom} it accelerates. */
    @SubscribeEvent
    public static void decay(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || FURY.isEmpty()) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % DECAY_INTERVAL_TICKS != 0) {
            return;
        }
        UltraRampagingConfig config = UltraRampaging.config();
        Iterator<Map.Entry<UUID, Float>> entries = FURY.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<UUID, Float> entry = entries.next();
            float decayed = decayOnce(entry.getValue(), config);
            if (decayed < config.furyFloor()) {
                entries.remove();
            } else {
                entry.setValue(decayed);
            }
        }
    }

    static float decayOnce(float fury, UltraRampagingConfig config) {
        float exponent = fury <= config.decayScaleFrom() ? 1.0F : fury / config.decayScaleDivisor();
        return fury * (float) Math.pow(Math.pow(config.decayPerTick(), exponent), DECAY_INTERVAL_TICKS);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        FURY.clear();
    }
}
