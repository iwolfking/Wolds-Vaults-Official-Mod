package xyz.iwolfking.woldsvaults.modifiers.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import xyz.iwolfking.woldsvaults.api.data.modifiers.DifficultyLockData;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultDifficultyAccessor;

import java.util.UUID;

public class DifficultyLockModifier extends VaultModifier<DifficultyLockModifier.Properties> {

    public DifficultyLockModifier(ResourceLocation id, DifficultyLockModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        if (world.getGameRules().getBoolean(ModGameRules.NORMALIZED_ENABLED)) {
            CommonEvents.PLAYER_TICK.register(context.getUUID(), EventPriority.HIGHEST, (event) -> {
                if (event.side.isServer()) {
                    if ((event.player.tickCount % 20) != 0) {
                        return;
                    }

                    if (!event.player.getLevel().dimension().equals(world.dimension())) {
                        return;
                    }

                    if (vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
                        return;
                    }

                    UUID ownerId = vault.get(Vault.OWNER);
                    if (ownerId == null) {
                        return;
                    }

                    if (!event.player.getUUID().equals(ownerId)) {
                        return;
                    }

                    VaultDifficulty currentDifficulty = WorldSettings.get(world).getPlayerDifficulty(ownerId);
                    if (currentDifficulty.equals(properties.getDifficulty())) {
                        return;
                    }

                    if (!properties().shouldLockHigher() && isDifficultyHigher(world, vault)) {
                        return;
                    }

                    DifficultyLockData lockData = DifficultyLockData.get(world);
                    if (!lockData.hasDifficulty(ownerId)) {
                        lockData.setDifficulty(ownerId, currentDifficulty);
                    }

                    WorldSettings.get(world).setPlayerDifficulty(ownerId, properties.getDifficulty());
                }
            });
        }
    }

    @Override
    public void onListenerRemove(VirtualWorld world, Vault vault, ModifierContext context, Listener listener) {
        UUID ownerId = vault.get(Vault.OWNER);
        if (ownerId != null) {
            DifficultyLockData lockData = DifficultyLockData.get(world);
            VaultDifficulty originalDifficulty = lockData.getDifficulty(ownerId);
            if (originalDifficulty != null) {
                WorldSettings.get(world).setPlayerDifficulty(ownerId, originalDifficulty);
                lockData.removeDifficulty(ownerId);
            }
        }
    }

    private boolean isDifficultyHigher(VirtualWorld world, Vault vault) {
        UUID ownerId = vault.get(Vault.OWNER);
        if (ownerId == null) return false;
        VaultDifficulty ownerDifficulty = WorldSettings.get(world).getPlayerDifficulty(ownerId);
        if (ownerDifficulty == null) return false;
        return ((VaultDifficultyAccessor)(Object)ownerDifficulty).getDisplayOrder() 
                >= ((VaultDifficultyAccessor)(Object)properties.getDifficulty()).getDisplayOrder();
    }

    public static class Properties {
        @Expose
        private final String difficulty;

        @Expose
        private final boolean lockHigher;

        public Properties(String difficulty, boolean lockHigher) {
            this.difficulty = difficulty;
            this.lockHigher = lockHigher;
        }

        public VaultDifficulty getDifficulty() {
            return VaultDifficulty.valueOf(this.difficulty);
        }

        public boolean shouldLockHigher() {
            return lockHigher;
        }
    }
}