package xyz.iwolfking.woldsvaults.integration.lightmanscurrency;

import iskallia.vault.client.data.ClientVaultPouchData;
import iskallia.vault.world.data.VaultPouchData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public final class VaultPouchCompatHelper {

    private VaultPouchCompatHelper() {}

    @Nullable
    public static UUID getOwner(@Nullable ItemStack pouch) {
        if (pouch == null || pouch.isEmpty()) {
            return null;
        }
        CompoundTag tag = pouch.getTag();
        return tag != null && tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Nullable
    public static CompoundTag getContents(@Nullable ItemStack pouch) {
        UUID owner = getOwner(pouch);
        if (owner == null) {
            return null;
        }
        if (EffectiveSide.get().isServer()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server == null ? null : VaultPouchData.get(server).contentsOf(owner);
        }
        return ClientVaultPouchData.contentsFor(owner);
    }

    public static void markContentsChanged(@Nullable ItemStack pouch) {
        UUID owner = getOwner(pouch);
        if (owner != null && EffectiveSide.get().isServer()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                VaultPouchData.get(server).contentsChanged(owner);
            }
        }
    }
}