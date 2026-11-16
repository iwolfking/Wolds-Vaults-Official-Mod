package xyz.iwolfking.woldsvaults.integration.sophisticatedstorage;

import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;

import java.util.UUID;

/**
 * Reads and writes the contents of Sophisticated Storage block items (chests, barrels, shulker boxes carried as items)
 * in that mod's world store. Server thread only; call only when the mod is loaded.
 */
public final class StorageItemContentsStore {
    private StorageItemContentsStore() {
    }

    /** A copy of the contents stored under {@code id}; an id the store has never seen reads as empty contents. */
    public static CompoundTag read(UUID id) {
        return ItemContentsStorage.get().getOrCreateStorageContents(id).copy();
    }

    /** Stores {@code contents} under a fresh {@code id}. */
    public static void write(UUID id, CompoundTag contents) {
        ItemContentsStorage.get().setStorageContents(id, contents.copy());
    }
}
