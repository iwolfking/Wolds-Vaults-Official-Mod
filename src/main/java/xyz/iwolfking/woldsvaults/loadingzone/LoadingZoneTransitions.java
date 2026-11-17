package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.bullet.BulletBill;
import xyz.iwolfking.woldsvaults.modifiers.gear.lightning.LightningFiend;
import xyz.iwolfking.woldsvaults.network.message.LoadingZoneFadeMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Watches players walk into loading zones, fades their screen out, teleports them once the destination is ready and fades
// back in. An arrival marker is ready at once; a pocket may need a few seconds of generating, so the screen holds black until
// it lands or gives up. The server owns the teleport; the client's fade is only for show and ends by itself.
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LoadingZoneTransitions {
    public static final int FADE_OUT_TICKS = 8;
    // the hold covers the teleport packet's trip, so a laggy client still lands while the screen is black
    public static final int HOLD_TICKS = 6;
    public static final int FADE_IN_TICKS = 10;
    public static final int MAX_WAIT_TICKS = 200;
    private static final int EDGE_CHECK_TICKS = 5;

    private static final Map<UUID, Transition> TRANSITIONS = new HashMap<>();
    private static final Map<UUID, Vec3> LAST_POSITIONS = new HashMap<>();
    // after arriving, zones wait until the player has stepped out of every area, so landing in one can't bounce them back
    private static final Set<UUID> DISARMED = new HashSet<>();

    private record Transition(LoadingZoneDestination destination, long start, long arrived) {
    }

    private LoadingZoneTransitions() {
    }

    public static boolean inTransition(ServerPlayer player) {
        return TRANSITIONS.containsKey(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();
        long now = player.server.getTickCount();
        Transition transition = TRANSITIONS.get(id);
        if (transition != null) {
            tickTransition(player, transition, now);
            return;
        }

        Vec3 position = player.position();
        Vec3 last = LAST_POSITIONS.put(id, position);
        if (last == null || player.isSpectator() || player.isPassenger() || LightningFiend.inForm(player) || BulletBill.inForm(player)) {
            return;
        }
        if (now % EDGE_CHECK_TICKS == 0 && PocketTerritory.inPocket(player)) {
            LoadingZoneDestination out = Pockets.pastEdge(player);
            if (out != null) {
                begin(player, out, now);
                return;
            }
        }
        List<LoadingZoneShape> zones = LoadingZones.zonesAt(player.level, player.blockPosition());
        boolean insideArea = false;
        for (LoadingZoneShape zone : zones) {
            boolean entered = zone.isGate() ? zone.crossedBy(last.x, last.z, position.x, position.z, position.y) : zone.contains(position.x, position.y, position.z);
            if (entered && !zone.isGate()) {
                insideArea = true;
            }
            if (entered && !DISARMED.contains(id)) {
                start(player, zone, last, now);
                return;
            }
        }
        if (!insideArea) {
            DISARMED.remove(id);
        }
    }

    // the destination is asked from the first tick, so a pocket's chunks start loading while the screen is still fading
    private static void tickTransition(ServerPlayer player, Transition transition, long now) {
        UUID id = player.getUUID();
        if (transition.arrived() >= 0L) {
            if (now - transition.arrived() >= HOLD_TICKS + FADE_IN_TICKS) {
                TRANSITIONS.remove(id);
            }
            return;
        }
        long elapsed = now - transition.start();
        LoadingZoneDestination.Landing landing = transition.destination().poll(player);
        if (landing != null && elapsed >= FADE_OUT_TICKS) {
            player.teleportTo(player.getLevel(), landing.pos().x, landing.pos().y, landing.pos().z, landing.yaw(), 0.0F);
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            LAST_POSITIONS.put(id, landing.pos());
            DISARMED.add(id);
            TRANSITIONS.put(id, new Transition(transition.destination(), transition.start(), now));
            if (!transition.destination().instant()) {
                ModNetwork.sendToClient(new LoadingZoneFadeMessage(0, HOLD_TICKS, FADE_IN_TICKS), player);
            }
        } else if (elapsed >= FADE_OUT_TICKS + MAX_WAIT_TICKS) {
            TRANSITIONS.remove(id);
            DISARMED.add(id);
            ModNetwork.sendToClient(new LoadingZoneFadeMessage(0, 0, FADE_IN_TICKS), player);
            player.displayClientMessage(new TextComponent("The way didn't open"), true);
        }
    }

    private static void start(ServerPlayer player, LoadingZoneShape zone, Vec3 last, long now) {
        LoadingZoneDestination destination = switch (zone.leads()) {
            case ARRIVAL -> LoadingZones.arrivalFor(player.level, zone)
                    .map(arrival -> LoadingZoneDestination.at(Vec3.atBottomCenterOf(arrival.getBlockPos()), LoadingZoneArrivalBlock.yaw(arrival.getBlockState())))
                    .orElse(null);
            case POCKET -> Pockets.enter(player, zone, last);
            case RETURN -> Pockets.leave(player);
        };
        if (destination == null) {
            DISARMED.add(player.getUUID());
            return;
        }
        begin(player, destination, now);
    }

    private static void begin(ServerPlayer player, LoadingZoneDestination destination, long now) {
        TRANSITIONS.put(player.getUUID(), new Transition(destination, now, -1L));
        int hold = destination.instant() ? HOLD_TICKS : MAX_WAIT_TICKS;
        ModNetwork.sendToClient(new LoadingZoneFadeMessage(FADE_OUT_TICKS, hold, FADE_IN_TICKS), player);
    }

    // nothing can hurt a player who can't see or move
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player && inTransition(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        forget(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        forget(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        forget(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TRANSITIONS.clear();
        LAST_POSITIONS.clear();
        DISARMED.clear();
    }

    private static void forget(UUID id) {
        TRANSITIONS.remove(id);
        LAST_POSITIONS.remove(id);
        DISARMED.remove(id);
    }
}
