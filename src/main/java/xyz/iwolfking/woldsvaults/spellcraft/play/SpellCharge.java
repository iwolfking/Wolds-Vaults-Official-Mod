package xyz.iwolfking.woldsvaults.spellcraft.play;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.spellcraft.bar.EffectiveBar;
import xyz.iwolfking.woldsvaults.spellcraft.projectile.SpellParticles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Charge: holding a slot key whose spell has Charge winds it up over the table's fixed charge time, like drawing a bow,
 * while the player uses the spell's item so every client shows the bow pose. Releasing reports how full it got. The
 * charge belongs to the slot's occupant and ends without casting when the occupant changes, and when the player's held
 * keys are ended ({@link SpellBarInput#cancelAll}, {@link SpellBarInput#releaseAll}).
 */
public final class SpellCharge {
    private static final Map<UUID, Charging> CHARGING = new HashMap<>();

    private SpellCharge() {
    }

    private record Charging(int slot, String occupant, InteractionHand hand, int fullTicks, int[] held) {
    }

    public static void start(ServerPlayer player, int slot, String occupant, InteractionHand hand, int fullTicks) {
        stop(player);
        CHARGING.put(player.getUUID(), new Charging(slot, occupant, hand, Math.max(1, fullTicks), new int[1]));
        player.startUsingItem(hand);
    }

    /**
     * Ends charging on {@code slot} and returns how full it got (0-1), or -1 if that slot wasn't charging or its
     * occupant changed since the charge began.
     */
    public static float release(ServerPlayer player, int slot, String occupant) {
        Charging charging = CHARGING.get(player.getUUID());
        if (charging == null || charging.slot() != slot) {
            return -1.0F;
        }
        stop(player);
        if (!charging.occupant().equals(occupant)) {
            return -1.0F;
        }
        return Math.min(1.0F, charging.held()[0] / (float) charging.fullTicks());
    }

    /** Ends any charge of the player without casting and returns whether one was running. */
    static boolean cancel(ServerPlayer player) {
        return stop(player);
    }

    private static boolean stop(ServerPlayer player) {
        Charging charging = CHARGING.remove(player.getUUID());
        if (charging != null && player.isUsingItem() && player.getUsedItemHand() == charging.hand()) {
            player.stopUsingItem();
        }
        return charging != null;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Charging charging = CHARGING.get(player.getUUID());
        if (charging == null) {
            return;
        }
        EffectiveBar bar = SpellBars.current(player);
        if (bar == null || !bar.slot(charging.slot()).occupantKey().equals(charging.occupant())) {
            stop(player);
            return;
        }
        int held = ++charging.held()[0];
        SpellParticles.chargeRing(player, held, held / (float) charging.fullTicks());
        if (held == charging.fullTicks()) {
            SpellParticles.chargeFull(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CHARGING.clear();
    }
}
