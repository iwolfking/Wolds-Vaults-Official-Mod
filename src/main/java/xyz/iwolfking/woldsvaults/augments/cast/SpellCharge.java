package xyz.iwolfking.woldsvaults.augments.cast;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * Charge: holding a slot key whose spell has Charge winds it up over the table's charge time (divided by the spell's
 * haste factor) while the player moves slower; releasing reports how full it got. Charging ends on death, logout or
 * dimension change without casting.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class SpellCharge {
    private static final UUID SLOW_ID = UUID.fromString("6f3a2b1e-9c4d-4e8a-b2f7-5a1c0d3e9b42");
    private static final int PROGRESS_TICKS = 5;
    private static final Map<UUID, Charging> CHARGING = new HashMap<>();

    private SpellCharge() {
    }

    private record Charging(int slot, int fullTicks, String name, int[] held) {
    }

    public static void start(ServerPlayer player, int slot, int fullTicks, String name, float moveMultiplier) {
        stop(player);
        CHARGING.put(player.getUUID(), new Charging(slot, Math.max(1, fullTicks), name, new int[1]));
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SLOW_ID) == null) {
            speed.addTransientModifier(new AttributeModifier(SLOW_ID, "Spell charge", moveMultiplier - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        AugmentCaster.showMessage(player, "Charging " + name + "...");
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
        CHARGING.remove(player.getUUID());
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SLOW_ID);
        }
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
        charging.held()[0]++;
        if (charging.held()[0] % PROGRESS_TICKS == 0) {
            int percent = Math.round(Math.min(1.0F, charging.held()[0] / (float) charging.fullTicks()) * 100.0F);
            AugmentCaster.showMessage(player, "Charging " + charging.name() + " " + percent + "%");
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
