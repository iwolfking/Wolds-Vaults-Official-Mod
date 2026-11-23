package xyz.iwolfking.woldsvaults.loadingzone;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

// Which pockets each vault has opened, the arrival markers in them, where each player came into one from, and the vault clock
// as it was before pockets paused it. It lives on the overworld, not in the vault's world, so chunk generation threads can read the pockets straight
// from memory without going near world storage.
public class PocketData extends SavedData {
    private static final String NAME = "woldsvaults_pockets";
    private static volatile PocketData loaded;

    private final Map<UUID, VaultPockets> vaults = new ConcurrentHashMap<>();

    public record Spot(BlockPos pos, String link, float yaw) {
    }

    // where the structure's own arrival markers ended up, and the box its blocks fill
    public record Scan(@Nullable BlockPos min, @Nullable BlockPos max, List<Spot> arrivals) {
        public static final Scan EMPTY = new Scan(null, null, List.of());

        @Nullable
        public BlockPos centre() {
            return this.min == null || this.max == null ? null : new BlockPos((this.min.getX() + this.max.getX()) / 2, this.max.getY(), (this.min.getZ() + this.max.getZ()) / 2);
        }
    }

    public record ClockHold(boolean paused, boolean visible) {
    }

    public static final class Pocket {
        private final int slot;
        private final ResourceLocation target;
        private volatile Scan scan;
        // arrival markers put down in the world inside this pocket, which only this vault's copy has
        private final Map<BlockPos, Spot> placed = new ConcurrentHashMap<>();

        private Pocket(int slot, ResourceLocation target, @Nullable Scan scan) {
            this.slot = slot;
            this.target = target;
            this.scan = scan;
        }

        public int slot() {
            return this.slot;
        }

        public ResourceLocation target() {
            return this.target;
        }

        @Nullable
        public Scan scan() {
            return this.scan;
        }

        public Collection<Spot> placed() {
            return this.placed.values();
        }
    }

    public static final class VaultPockets {
        private final Map<String, Integer> slots = new ConcurrentHashMap<>();
        private final Map<Integer, Pocket> pockets = new ConcurrentHashMap<>();
        private final Map<UUID, LoadingZoneDestination.Landing> returns = new HashMap<>();
        private ClockHold clock;

        @Nullable
        public ClockHold clock() {
            return this.clock;
        }

        public Collection<Pocket> pockets() {
            return this.pockets.values();
        }

        public int returns() {
            return this.returns.size();
        }
    }

    public static PocketData get(MinecraftServer server) {
        PocketData data = server.overworld().getDataStorage().computeIfAbsent(PocketData::load, PocketData::new, NAME);
        loaded = data;
        return data;
    }

    // for generation threads: nothing until the server has loaded the data
    @Nullable
    public static Pocket lookup(UUID vault, int slot) {
        PocketData data = loaded;
        VaultPockets pockets = data == null ? null : data.vaults.get(vault);
        return pockets == null ? null : pockets.pockets.get(slot);
    }

    public static void unload() {
        loaded = null;
    }

    // a zone keeps the pocket it opened first, so walking back in lands in the same place
    public Pocket open(UUID vault, String zone, ResourceLocation target) {
        VaultPockets pockets = this.vaults.computeIfAbsent(vault, id -> new VaultPockets());
        Integer slot = pockets.slots.get(zone);
        if (slot == null) {
            slot = pockets.pockets.size();
            pockets.pockets.put(slot, new Pocket(slot, target, null));
            pockets.slots.put(zone, slot);
            setDirty();
        }
        return pockets.pockets.get(slot);
    }

    @Nullable
    public Pocket pocket(UUID vault, int slot) {
        VaultPockets pockets = this.vaults.get(vault);
        return pockets == null ? null : pockets.pockets.get(slot);
    }

    public void setPlaced(Pocket pocket, Spot spot) {
        if (!spot.equals(pocket.placed.put(spot.pos(), spot))) {
            setDirty();
        }
    }

    public void removePlaced(Pocket pocket, BlockPos pos) {
        if (pocket.placed.remove(pos) != null) {
            setDirty();
        }
    }

    public void setScan(Pocket pocket, Scan scan) {
        pocket.scan = scan;
        setDirty();
    }

    public void setReturn(UUID vault, UUID player, LoadingZoneDestination.Landing landing) {
        this.vaults.computeIfAbsent(vault, id -> new VaultPockets()).returns.put(player, landing);
        setDirty();
    }

    @Nullable
    public LoadingZoneDestination.Landing getReturn(UUID vault, UUID player) {
        VaultPockets pockets = this.vaults.get(vault);
        return pockets == null ? null : pockets.returns.get(player);
    }

    public void setClock(VaultPockets pockets, @Nullable ClockHold hold) {
        pockets.clock = hold;
        setDirty();
    }

    public Map<UUID, VaultPockets> vaults() {
        return this.vaults;
    }

    public void forget(Predicate<UUID> gone) {
        if (this.vaults.keySet().removeIf(gone)) {
            setDirty();
        }
    }

    private static PocketData load(CompoundTag tag) {
        PocketData data = new PocketData();
        for (Tag vaultTag : tag.getList("Vaults", Tag.TAG_COMPOUND)) {
            CompoundTag vault = (CompoundTag) vaultTag;
            VaultPockets pockets = new VaultPockets();
            for (Tag pocketTag : vault.getList("Pockets", Tag.TAG_COMPOUND)) {
                CompoundTag pocket = (CompoundTag) pocketTag;
                ResourceLocation target = ResourceLocation.tryParse(pocket.getString("Target"));
                if (target != null) {
                    int slot = pocket.getInt("Slot");
                    Pocket entry = new Pocket(slot, target, pocket.contains("Scan") ? readScan(pocket.getCompound("Scan")) : null);
                    for (Tag spotTag : pocket.getList("Placed", Tag.TAG_COMPOUND)) {
                        Spot spot = readSpot((CompoundTag) spotTag);
                        entry.placed.put(spot.pos(), spot);
                    }
                    pockets.pockets.put(slot, entry);
                    pockets.slots.put(pocket.getString("Zone"), slot);
                }
            }
            for (Tag returnTag : vault.getList("Returns", Tag.TAG_COMPOUND)) {
                CompoundTag landing = (CompoundTag) returnTag;
                pockets.returns.put(landing.getUUID("Player"), new LoadingZoneDestination.Landing(new Vec3(landing.getDouble("X"), landing.getDouble("Y"), landing.getDouble("Z")), landing.getFloat("Yaw")));
            }
            if (vault.contains("Clock")) {
                CompoundTag clock = vault.getCompound("Clock");
                pockets.clock = new ClockHold(clock.getBoolean("Paused"), clock.getBoolean("Visible"));
            }
            data.vaults.put(vault.getUUID("Id"), pockets);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag vaultList = new ListTag();
        this.vaults.forEach((id, pockets) -> {
            CompoundTag vault = new CompoundTag();
            vault.putUUID("Id", id);
            ListTag pocketList = new ListTag();
            pockets.slots.forEach((zone, slot) -> {
                Pocket pocket = pockets.pockets.get(slot);
                CompoundTag entry = new CompoundTag();
                entry.putString("Zone", zone);
                entry.putInt("Slot", slot);
                entry.putString("Target", pocket.target.toString());
                if (pocket.scan != null) {
                    entry.put("Scan", writeScan(pocket.scan));
                }
                ListTag placed = new ListTag();
                pocket.placed.values().forEach(spot -> placed.add(writeSpot(spot)));
                entry.put("Placed", placed);
                pocketList.add(entry);
            });
            vault.put("Pockets", pocketList);
            ListTag returnList = new ListTag();
            pockets.returns.forEach((player, landing) -> {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("Player", player);
                entry.putDouble("X", landing.pos().x);
                entry.putDouble("Y", landing.pos().y);
                entry.putDouble("Z", landing.pos().z);
                entry.putFloat("Yaw", landing.yaw());
                returnList.add(entry);
            });
            vault.put("Returns", returnList);
            if (pockets.clock != null) {
                CompoundTag clock = new CompoundTag();
                clock.putBoolean("Paused", pockets.clock.paused());
                clock.putBoolean("Visible", pockets.clock.visible());
                vault.put("Clock", clock);
            }
            vaultList.add(vault);
        });
        tag.put("Vaults", vaultList);
        return tag;
    }

    private static Scan readScan(CompoundTag tag) {
        List<Spot> arrivals = new ArrayList<>();
        for (Tag spotTag : tag.getList("Arrivals", Tag.TAG_COMPOUND)) {
            arrivals.add(readSpot((CompoundTag) spotTag));
        }
        return new Scan(tag.contains("Min") ? NbtUtils.readBlockPos(tag.getCompound("Min")) : null, tag.contains("Max") ? NbtUtils.readBlockPos(tag.getCompound("Max")) : null, arrivals);
    }

    private static CompoundTag writeScan(Scan scan) {
        CompoundTag tag = new CompoundTag();
        if (scan.min() != null && scan.max() != null) {
            tag.put("Min", NbtUtils.writeBlockPos(scan.min()));
            tag.put("Max", NbtUtils.writeBlockPos(scan.max()));
        }
        ListTag arrivals = new ListTag();
        for (Spot spot : scan.arrivals()) {
            arrivals.add(writeSpot(spot));
        }
        tag.put("Arrivals", arrivals);
        return tag;
    }

    private static Spot readSpot(CompoundTag tag) {
        return new Spot(NbtUtils.readBlockPos(tag.getCompound("Pos")), tag.getString("Link"), tag.getFloat("Yaw"));
    }

    private static CompoundTag writeSpot(Spot spot) {
        CompoundTag tag = new CompoundTag();
        tag.put("Pos", NbtUtils.writeBlockPos(spot.pos()));
        tag.putString("Link", spot.link());
        tag.putFloat("Yaw", spot.yaw());
        return tag;
    }
}
