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
 * Fury: the per-player resource behind Ultra Rampaging.
 *
 * <p>Named Fury rather than Rage on purpose. Two unrelated counters already own that word - the
 * base mod's {@code ModEffects.RAGE} (an integer mob-effect amplifier capped at 100, fed only by
 * the Barbarian archetype) and {@code VaultChampionRage} (the per-vault counter that decides when
 * a Vault Champion turns up). Neither could carry this one: the first is an integer clamped two
 * orders of magnitude below the values used here, and the second is per-vault rather than
 * per-player-per-fight.
 *
 * <p>Fury accrues and decays whether or not a Rampage ability is toggled on, so the drawback in
 * {@link UltraRampaging#incomingMultiplier} applies to a player who has stopped attacking. Taking
 * the node means being easier to kill, and that is not something the toggle switches off.
 *
 * <p>Never persisted. A counter whose half-life is measured in seconds has no meaning across a
 * save, and carrying one over a vault boundary would hand a player a free opening multiplier.
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

    /**
     * Adds Fury and returns the new total, clamped to the configured cap. A non-positive amount is
     * ignored rather than allowed to drain the pool - every caller is an income source, and a
     * negative arriving here would mean a sign error upstream that is worth surfacing.
     */
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

    /**
     * The scaling drain, run once every {@value #DECAY_INTERVAL_TICKS} ticks.
     *
     * <p>Below the scaling threshold the retained fraction is the flat {@code 0.985^10 = 0.85973},
     * a 2.29 s half-life. Above it the per-tick base is raised to {@code fury / divisor}, so the
     * drain accelerates with the size of the pool. That is deliberate and load-bearing: it pins a
     * player near the threshold, which is the shoulder between the exponential and square-root
     * halves of {@link UltraRampaging#furyMultiplier}. The result is frequent small power spikes
     * rather than a large stack that takes many seconds to bleed off, and the resulting swing in
     * damage is the intended feel of the node, not a defect to be smoothed away later.
     *
     * <p>The drain also bounds the trough analytically: {@code f(x) = x * 0.985^(x/200)} peaks at
     * {@code x = 200 / -ln(0.985) = 13233}, so no income rate whatsoever can leave a player above
     * {@code 13233 / e = 4868} Fury once the decay has run.
     */
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
