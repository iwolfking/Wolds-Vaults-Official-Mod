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
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.VaultDifficultyAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DifficultyLockModifier extends VaultModifier<DifficultyLockModifier.Properties> {

    private final Map<UUID, VaultDifficulty> RETURN_DIFFICULTY_MAP = new HashMap<>();

    public DifficultyLockModifier(ResourceLocation id, DifficultyLockModifier.Properties properties, VaultModifier.Display display) {
        super(id, properties, display);
    }

    @Override
    public void initServer(VirtualWorld world, Vault vault, ModifierContext context) {
        if(WoldsVaultsConfig.COMMON.normalizedModifierEnabled.get()) {
            CommonEvents.PLAYER_TICK.register(context.getUUID(), EventPriority.HIGHEST, (event) -> {
                if(event.side.isServer()) {
                    if((event.player.tickCount % 20) != 0) {
                        return;
                    }

                    if(!(event.player.getLevel().dimension().equals(world.dimension()))) {
                        return;
                    }

                    if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
                        return;
                    }

                    if(event.player.getUUID() != vault.get(Vault.OWNER)) {
                        return;
                    }
                    if(WorldSettings.get(world).getPlayerDifficulty(vault.get(Vault.OWNER)).equals(properties.getDifficulty())) {
                        return;
                    }

                    if(!properties().shouldLockHigher() && isDifficultyHigher(world, vault)) {
                        return;
                    }
                    RETURN_DIFFICULTY_MAP.put(vault.get(Vault.OWNER), WorldSettings.get(world).getPlayerDifficulty(vault.get(Vault.OWNER)));
                    WorldSettings.get(world).setPlayerDifficulty(vault.get(Vault.OWNER), properties.getDifficulty());
                }
            });
        }
    }

    @Override
    public void onListenerRemove(VirtualWorld world, Vault vault, ModifierContext context, Listener listener) {
        if(WoldsVaultsConfig.COMMON.normalizedModifierEnabled.get()) {
            vault.ifPresent(Vault.OWNER, owner -> {
                if(listener.getPlayer().isPresent()) {
                    if(listener.getPlayer().get().getUUID().equals(owner)) {
                        if(RETURN_DIFFICULTY_MAP.containsKey(vault.get(Vault.OWNER))) {
                            WorldSettings.get(world).setPlayerDifficulty(vault.get(Vault.OWNER), RETURN_DIFFICULTY_MAP.get(vault.get(Vault.OWNER)));
                            return;
                        }

                        WorldSettings.get(world).setPlayerDifficulty(vault.get(Vault.OWNER), VaultDifficulty.NORMAL);
                        RETURN_DIFFICULTY_MAP.clear();
                    }
                }
            });
        }
    }

    private boolean isDifficultyHigher(VirtualWorld world, Vault vault) {
        return ((VaultDifficultyAccessor)(Object)WorldSettings.get(world).getPlayerDifficulty(vault.get(Vault.OWNER))).getDisplayOrder() >= ((VaultDifficultyAccessor)(Object)properties.getDifficulty()).getDisplayOrder();
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
