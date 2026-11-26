package xyz.iwolfking.woldsvaults.loadingzone;

import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.EntityState;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.blocks.LoadingZoneArrivalBlock;
import xyz.iwolfking.woldsvaults.blocks.tiles.LoadingZoneMarkerTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vault pockets: a loading zone can lead to one structure built far off in the vault's own world, so the run carries on
 * (same vault, modifiers and loot) away from the vault's layout, timer and map. A zone opens its pocket the first time
 * someone walks in and keeps it for the rest of the vault; every room's copy of the zone gets its own. While all online
 * runners are in pockets the vault clock is paused and hidden, the way rune boss fights do it, and put back as it was
 * once anyone steps out.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Pockets {
    private static final TicketType<ChunkPos> TICKET = TicketType.create("woldsvaults_pocket", Comparator.comparingLong(ChunkPos::toLong), 100);
    private static final int CHECK_INTERVAL = 10;
    private static final double EDGE_MARGIN = 2.0;
    private static final double FALL_MARGIN = 6.0;
    private static final Map<PocketTemplates.SlotKey, CompletableFuture<?>> SCANS = new ConcurrentHashMap<>();

    private Pockets() {
    }

    @Nullable
    public static LoadingZoneDestination enter(ServerPlayer player, LoadingZoneShape zone, Vec3 from) {
        ServerLevel level = player.getLevel();
        Optional<Vault> vault = ServerVaults.get(level);
        if (vault.isEmpty()) {
            player.displayClientMessage(new TextComponent("Pockets only open inside a vault"), true);
            return null;
        }
        if (zone.pocket().isBlank()) {
            player.displayClientMessage(new TextComponent("This zone's pocket has no template or pool set"), true);
            return null;
        }
        ResourceLocation target = ResourceLocation.tryParse(zone.pocket());
        if (target == null || !PocketTemplates.exists(target)) {
            player.displayClientMessage(new TextComponent("No vault template or pool called \"" + zone.pocket() + "\""), true);
            return null;
        }
        UUID id = vault.get().get(Vault.ID);
        PocketData data = PocketData.get(player.server);
        long room = LoadingZones.group(level, new BlockPos(zone.centreX(), zone.minY(), zone.centreZ()));
        PocketData.Pocket pocket = data.open(id, room + "/" + zone.link(), target);
        LoadingZoneDestination.Landing back = LoadingZones.arrivalFor(level, zone)
                .map(marker -> new LoadingZoneDestination.Landing(Vec3.atBottomCenterOf(marker.getBlockPos()), LoadingZoneArrivalBlock.yaw(marker.getBlockState())))
                .orElseGet(() -> new LoadingZoneDestination.Landing(from, player.getYRot() + 180.0F));
        data.setReturn(id, player.getUUID(), back);
        return new PocketLanding(vault.get(), pocket, zone.link());
    }

    /**
     * A pocket is a room with nothing round it, so walking out of one of its doorways, or falling out of it, leaves the
     * same way a return zone does; standing in pocket territory where no pocket was opened leaves too
     */
    @Nullable
    public static LoadingZoneDestination pastEdge(ServerPlayer player) {
        Optional<Vault> vault = ServerVaults.get(player.getLevel());
        if (vault.isEmpty()) {
            return null;
        }
        PocketData.Pocket pocket = PocketData.get(player.server).pocket(vault.get().get(Vault.ID), PocketTerritory.slotOf(player.getBlockX()));
        if (pocket == null) {
            return leave(player);
        }
        PocketData.Scan scan = pocket.scan();
        if (scan == null || scan.min() == null || scan.max() == null) {
            return null;
        }
        BlockPos min = scan.min();
        BlockPos max = scan.max();
        boolean outside = player.getX() < min.getX() - EDGE_MARGIN || player.getX() > max.getX() + 1 + EDGE_MARGIN
                || player.getZ() < min.getZ() - EDGE_MARGIN || player.getZ() > max.getZ() + 1 + EDGE_MARGIN
                || player.getY() < min.getY() - FALL_MARGIN;
        return outside ? leave(player) : null;
    }

    @Nullable
    public static LoadingZoneDestination leave(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        Optional<Vault> vault = ServerVaults.get(level);
        if (vault.isEmpty()) {
            return null;
        }
        LoadingZoneDestination.Landing back = PocketData.get(player.server).getReturn(vault.get().get(Vault.ID), player.getUUID());
        if (back == null) {
            back = start(level, vault.get());
        }
        return back == null ? null : new LoadedLanding(back);
    }

    /**
     * An arrival marker put down inside a pocket counts for that pocket from then on, even while its chunk is unloaded
     */
    public static void arrivalChanged(LoadingZoneMarkerTileEntity marker) {
        BlockPos pos = marker.getBlockPos();
        if (!(marker.getLevel() instanceof ServerLevel level) || !marker.isArrival() || !PocketTerritory.isVaultWorld(level) || !PocketTerritory.inTerritory(pos.getX())) {
            return;
        }
        ServerVaults.get(level).ifPresent(vault -> {
            PocketData data = PocketData.get(level.getServer());
            PocketData.Pocket pocket = data.pocket(vault.get(Vault.ID), PocketTerritory.slotOf(pos.getX()));
            if (pocket != null) {
                data.setPlaced(pocket, new PocketData.Spot(pos.immutable(), marker.getLink(), LoadingZoneArrivalBlock.yaw(marker.getBlockState())));
            }
        });
    }

    public static void arrivalRemoved(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || !PocketTerritory.isVaultWorld(level) || !PocketTerritory.inTerritory(pos.getX())) {
            return;
        }
        ServerVaults.get(level).ifPresent(vault -> {
            PocketData data = PocketData.get(server.getServer());
            PocketData.Pocket pocket = data.pocket(vault.get(Vault.ID), PocketTerritory.slotOf(pos.getX()));
            if (pocket != null) {
                data.removePlaced(pocket, pos);
            }
        });
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % CHECK_INTERVAL != 0) {
            return;
        }
        PocketData data = PocketData.get(server);
        data.forget(id -> {
            boolean gone = ServerVaults.get(id).isEmpty();
            if (gone) {
                PocketTemplates.forget(id);
            }
            return gone;
        });
        PocketTemplates.evictIdle();
    }

    /**
     * Generation threads read pockets from memory, so they have to be there before anyone can stand in a pocket chunk
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PocketData.get(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PocketData.unload();
        PocketTemplates.clear();
        SCANS.clear();
    }

    /**
     * Whether the vault clock stands still: at least one runner is online and every online runner is in pocket
     * territory of the vault's own world. Read every clock tick by the clock mixin, which only skips the time step and
     * leaves the vault's own pause and visibility flags alone.
     */
    public static boolean holdsClock(Vault vault) {
        ResourceLocation world = vault.get(Vault.WORLD).get(WorldManager.KEY);
        boolean anyone = false;
        for (Listener listener : vault.get(Vault.LISTENERS).getAll()) {
            if (!(listener instanceof Runner) || !listener.isOnline()) {
                continue;
            }
            Optional<ServerPlayer> player = listener.getPlayer();
            if (player.isEmpty()) {
                continue;
            }
            if (!player.get().level.dimension().location().equals(world) || !PocketTerritory.inPocket(player.get())) {
                return false;
            }
            anyone = true;
        }
        return anyone;
    }

    @Nullable
    private static LoadingZoneDestination.Landing start(ServerLevel level, Vault vault) {
        if (vault.get(Vault.WORLD).get(WorldManager.PORTAL_LOGIC) instanceof ClassicPortalLogic logic) {
            return logic.getPlayerStart(level, vault)
                    .map(state -> new LoadingZoneDestination.Landing(new Vec3(state.get(EntityState.POS_X), state.get(EntityState.POS_Y), state.get(EntityState.POS_Z)), state.get(EntityState.YAW)))
                    .orElse(null);
        }
        return null;
    }

    private static void scan(MinecraftServer server, Vault vault, PocketData.Pocket pocket) {
        PocketTemplates.SlotKey key = new PocketTemplates.SlotKey(vault.get(Vault.ID), pocket.slot());
        SCANS.computeIfAbsent(key, slot -> CompletableFuture
                .supplyAsync(() -> PocketTemplates.scan(vault, pocket.slot(), pocket.target()), Util.backgroundExecutor())
                .whenComplete((scan, error) -> server.execute(() -> {
                    SCANS.remove(slot);
                    if (error != null) {
                        WoldLogger.error("Could not read pocket {} for its arrival markers", pocket.target(), error);
                    }
                    PocketData.get(server).setScan(pocket, error == null ? scan : PocketData.Scan.EMPTY);
                })));
    }

    /** Asks for the landing's chunks and its neighbours, and says whether they're generated and loaded yet */
    private static boolean loaded(ServerLevel level, BlockPos pos) {
        ChunkPos centre = new ChunkPos(pos);
        level.getChunkSource().addRegionTicket(TICKET, centre, 2, centre);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getChunkSource().getChunkNow(centre.x + dx, centre.z + dz) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * An arrival marker put down in the pocket, then one built into its structure, the zone's link first; with none, a
     * doorway where vault rooms have them, facing in; and only then the top of the pocket's middle
     */
    private record PocketLanding(Vault vault, PocketData.Pocket pocket, String link) implements LoadingZoneDestination {
        @Override
        public Landing poll(ServerPlayer player) {
            PocketData.Scan scan = this.pocket.scan();
            if (scan == null) {
                scan(player.server, this.vault, this.pocket);
                return null;
            }
            ServerLevel level = player.getLevel();
            List<PocketData.Spot> spots = new ArrayList<>(this.pocket.placed());
            spots.addAll(scan.arrivals());
            Optional<PocketData.Spot> spot = spots.stream().filter(candidate -> candidate.link().equals(this.link)).findFirst().or(() -> spots.stream().findFirst());
            if (spot.isPresent()) {
                return loaded(level, spot.get().pos()) ? new Landing(Vec3.atBottomCenterOf(spot.get().pos()), spot.get().yaw()) : null;
            }
            if (scan.min() == null || scan.max() == null) {
                return null;
            }
            for (Landing door : doorways(PocketTerritory.origin(this.pocket.slot()), scan.max())) {
                BlockPos feet = new BlockPos(door.pos());
                if (!loaded(level, feet)) {
                    return null;
                }
                if (standable(level, feet)) {
                    return door;
                }
            }
            BlockPos centre = scan.centre();
            if (!loaded(level, centre)) {
                return null;
            }
            for (int y = scan.max().getY() + 1; y > scan.min().getY(); y--) {
                BlockPos feet = new BlockPos(centre.getX(), y, centre.getZ());
                if (standable(level, feet)) {
                    return new Landing(Vec3.atBottomCenterOf(feet), 0.0F);
                }
            }
            return null;
        }

        @Override
        public boolean instant() {
            return false;
        }
    }

    /**
     * A vault room's doorways sit mid-face with the walking level 22 up, the way the generator cuts them, one block in
     * from each face
     */
    private static List<LoadingZoneDestination.Landing> doorways(BlockPos origin, BlockPos max) {
        int midX = origin.getX() + (max.getX() - origin.getX()) / 2;
        int midZ = origin.getZ() + (max.getZ() - origin.getZ()) / 2;
        double y = origin.getY() + 22;
        return List.of(
                new LoadingZoneDestination.Landing(new Vec3(midX + 0.5, y, origin.getZ() + 1.5), 0.0F),
                new LoadingZoneDestination.Landing(new Vec3(midX + 0.5, y, max.getZ() - 0.5), 180.0F),
                new LoadingZoneDestination.Landing(new Vec3(origin.getX() + 1.5, y, midZ + 0.5), -90.0F),
                new LoadingZoneDestination.Landing(new Vec3(max.getX() - 0.5, y, midZ + 0.5), 90.0F));
    }

    private static boolean standable(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty();
    }

    private record LoadedLanding(Landing landing) implements LoadingZoneDestination {
        @Override
        public Landing poll(ServerPlayer player) {
            return loaded(player.getLevel(), new BlockPos(this.landing.pos())) ? this.landing : null;
        }

        @Override
        public boolean instant() {
            return false;
        }
    }
}
