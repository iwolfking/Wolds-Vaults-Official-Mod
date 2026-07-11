package xyz.iwolfking.woldsvaults.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * God's Mastery consumables permanently raise a player's MAXIMUM god reputation (base 50)
 * with all four Vault Gods. The count lives in the player's own persistent NBT
 * (ForgeData &gt; PlayerPersisted), NOT in world saved data, so it travels with the player
 * file across deaths, relogs and sub-server transfers.
 */
public final class GodMasteryHelper {
    public static final int BASE_CAP = 50;
    public static final String NBT_KEY = "woldsvaults:god_mastery";
    // The reputation cap for the player whose reputation is currently being read or written.
    // PlayerReputationData's per-player Entry has no player context of its own, so the outer
    // static methods publish the effective cap here before the Entry-level clamps run.
    private static final ThreadLocal<Integer> CURRENT_CAP = ThreadLocal.withInitial(() -> BASE_CAP);

    private GodMasteryHelper() {
    }

    public static int getMastery(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return 0;
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG).getInt(NBT_KEY);
    }

    /** Mastery by uuid; 0 for offline players (their stored reputation is never modified). */
    public static int getMastery(UUID playerUUID) {
        ServerPlayer player = findOnline(playerUUID);
        return player == null ? 0 : getMastery(player);
    }

    public static int capFor(Player player) {
        return BASE_CAP + getMastery(player);
    }

    public static void pushCap(UUID playerUUID) {
        CURRENT_CAP.set(BASE_CAP + getMastery(playerUUID));
    }

    public static int currentCap() {
        return CURRENT_CAP.get();
    }

    /** Consumes one mastery: bumps the persistent counter and returns the NEW cap. */
    public static int increaseMastery(ServerPlayer player) {
        CompoundTag persisted = persistedTag(player);
        int mastery = persisted.getInt(NBT_KEY) + 1;
        persisted.putInt(NBT_KEY, mastery);
        WoldsVaults.LOGGER.info("{} consumed God's Mastery #{}: god reputation cap is now {}.",
                player.getGameProfile().getName(), mastery, BASE_CAP + mastery);
        return BASE_CAP + mastery;
    }

    /**
     * Death/respawn clone. Forge carries the PlayerPersisted subtree across death itself,
     * but copying explicitly costs nothing and removes any dependence on that behavior.
     */
    public static void copyOnClone(Player original, Player clone) {
        CompoundTag oldRoot = original.getPersistentData();
        if (!oldRoot.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        int mastery = oldRoot.getCompound(Player.PERSISTED_NBT_TAG).getInt(NBT_KEY);
        if (mastery <= 0) {
            return;
        }
        CompoundTag persisted = persistedTag(clone);
        if (persisted.getInt(NBT_KEY) < mastery) {
            persisted.putInt(NBT_KEY, mastery);
        }
    }

    /** The live (attached) PlayerPersisted compound, created on first use. */
    private static CompoundTag persistedTag(Player player) {
        CompoundTag root = player.getPersistentData();
        if (root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            return root.getCompound(Player.PERSISTED_NBT_TAG);
        }
        CompoundTag persisted = new CompoundTag();
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return persisted;
    }

    @Nullable
    private static ServerPlayer findOnline(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(playerUUID);
    }
}
