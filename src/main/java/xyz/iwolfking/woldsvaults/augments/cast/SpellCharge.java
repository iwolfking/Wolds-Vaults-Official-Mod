package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Charge: holding a slot key whose spell has Charge winds it up over the table's fixed charge time, like drawing a bow.
 * The player uses the off-hand focus or wand meanwhile, so every client shows the bow pose and the bow's slowdown
 * applies; releasing reports how full it got. Charging ends on death, logout or dimension change without casting.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class SpellCharge {
    private static final Map<UUID, Charging> CHARGING = new HashMap<>();

    private SpellCharge() {
    }

    private record Charging(int slot, int fullTicks, int[] held) {
    }

    public static void start(ServerPlayer player, int slot, int fullTicks) {
        stop(player);
        CHARGING.put(player.getUUID(), new Charging(slot, Math.max(1, fullTicks), new int[1]));
        player.startUsingItem(InteractionHand.OFF_HAND);
    }

    /** Ends charging on {@code slot} and returns how full it got (0-1), or -1 if that slot wasn't charging. */
    public static float release(ServerPlayer player, int slot) {
        Charging charging = CHARGING.get(player.getUUID());
        if (charging == null || charging.slot() != slot) {
            return -1.0F;
        }
        stop(player);
        return Math.min(1.0F, charging.held()[0] / (float) charging.fullTicks());
    }

    public static boolean isCharging(ServerPlayer player) {
        return CHARGING.containsKey(player.getUUID());
    }

    private static void stop(ServerPlayer player) {
        if (CHARGING.remove(player.getUUID()) != null && player.isUsingItem() && player.getUsedItemHand() == InteractionHand.OFF_HAND) {
            player.stopUsingItem();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Charging charging = CHARGING.get(player.getUUID());
        if (charging != null) {
            charging.held()[0]++;
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            stop(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            stop(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            stop(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CHARGING.clear();
    }
}
