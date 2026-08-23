package xyz.iwolfking.woldsvaults.maps;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persisted god binding of every running mapped vault, dropped when its vault ends. */
public final class MapGodVaultState {
    private MapGodVaultState() {
    }

    public static void set(UUID vaultId, VaultGod god, int bonusPercent, double difficultyMultiplier) {
        if (vaultId == null || god == null) {
            return;
        }
        Store store = store();
        if (store != null) {
            store.bindings.put(vaultId, new Binding(god, bonusPercent, difficultyMultiplier));
            store.setDirty();
        }
    }

    public static Optional<Binding> get(UUID vaultId) {
        if (vaultId == null) {
            return Optional.empty();
        }
        Store store = store();
        return store == null ? Optional.empty() : Optional.ofNullable(store.bindings.get(vaultId));
    }

    public static Optional<Binding> get(Vault vault) {
        if (vault == null || !vault.has(Vault.ID)) {
            return Optional.empty();
        }
        return get(vault.get(Vault.ID));
    }

    public static void release(UUID vaultId) {
        if (vaultId == null) {
            return;
        }
        Store store = store();
        if (store != null && store.bindings.remove(vaultId) != null) {
            store.setDirty();
        }
    }

    private static Store store() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            WoldsVaults.LOGGER.error("Map god binding touched with no server running; the binding is lost.");
            return null;
        }
        return Store.get(server);
    }

    public record Binding(VaultGod god, int bonusPercent, double difficultyMultiplier) {
    }

    private static final class Store extends SavedData {
        private static final String DATA_NAME = "woldsvaults_MapGodBindings";

        private final Map<UUID, Binding> bindings = new HashMap<>();

        private Store() {
        }

        private Store(CompoundTag tag) {
            ListTag list = tag.getList("vaults", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                VaultGod god = VaultGod.fromName(entry.getString("god"));
                if (god == null) {
                    WoldsVaults.LOGGER.error("Saved map god binding names unknown god '{}'; dropping it.", entry.getString("god"));
                    continue;
                }
                this.bindings.put(entry.getUUID("vault"),
                        new Binding(god, entry.getInt("bonus"), entry.getDouble("difficulty")));
            }
        }

        private static Store get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(Store::new, Store::new, DATA_NAME);
        }

        @Override
        @Nonnull
        public CompoundTag save(@Nonnull CompoundTag tag) {
            ListTag list = new ListTag();
            this.bindings.forEach((vaultId, binding) -> {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("vault", vaultId);
                entry.putString("god", binding.god().getName());
                entry.putInt("bonus", binding.bonusPercent());
                entry.putDouble("difficulty", binding.difficultyMultiplier());
                list.add(entry);
            });
            tag.put("vaults", list);
            return tag;
        }
    }
}
