package xyz.iwolfking.woldsvaults.integration.sophisticatedbackpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackAccessLogger;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackStorage;

import java.util.UUID;

/** Reads and writes backpack contents in Sophisticated Backpacks' world store. Server thread only. */
public final class BackpackContentsStore {
    private BackpackContentsStore() {
    }

    /** A copy of the contents stored under {@code id}; an id the store has never seen reads as empty contents. */
    public static CompoundTag read(UUID id) {
        return BackpackStorage.get().getOrCreateBackpackContents(id).copy();
    }

    /** Stores {@code contents} under a fresh {@code id}. */
    public static void write(UUID id, CompoundTag contents) {
        BackpackStorage.get().setBackpackContents(id, contents.copy());
    }

    /**
     * Records {@code player} as the last to open the backpack stored under {@code id}, so the store's cleanup command
     * keeps it before anyone has opened it.
     */
    public static void logAccess(ServerPlayer player, ItemStack backpack, UUID id) {
        backpack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).ifPresent(wrapper ->
                BackpackAccessLogger.logPlayerAccess(player, backpack.getItem(), id, backpack.getHoverName().getString(),
                        wrapper.getMainColor(), wrapper.getAccentColor(), wrapper.getColumnsTaken()));
    }
}
