package xyz.iwolfking.woldsvaults.integration.cctweaked;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.key.ThemeKey;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VaultPortalPeripheral implements IPeripheral {

    private final VaultPortalTileEntity portal;

    public VaultPortalPeripheral(VaultPortalTileEntity portal) {
        this.portal = portal;
    }

    @Nonnull
    @Override
    public String getType() {
        return "vault_portal";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof VaultPortalPeripheral vp && vp.portal == this.portal;
    }

    @LuaFunction
    public final String getVaultId() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> {
            UUID vaultId = cd.getProperties().getVaultId();
            return vaultId != null ? vaultId.toString() : null;
        }).orElse(null);
    }

    @LuaFunction
    public final String getVaultLayout() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> {
            LayoutDefinition definition = LayoutDefinitionRegistry.getForLayout(cd.getLayout()).orElse(null);
            if(definition == null) {
                return "unsupported";
            }

            return definition.id();
        }).orElse("unsupported");
    }

    @LuaFunction
    public final int getVaultLevel() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> cd.getProperties().getLevel().orElse(-1)).orElse(-1);
    }

    @LuaFunction
    public final boolean vaultExists() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> {
            UUID vaultId = cd.getProperties().getVaultId();
            return vaultId != null && ServerVaults.get(vaultId).isPresent();
        }).orElse(false);
    }

    //Vault methods
    public final Optional<Vault> getVault() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> {
            UUID vaultId = cd.getProperties().getVaultId();
            return ServerVaults.get(vaultId);
        }).orElse(Optional.empty());
    }

    @LuaFunction
    public final Long getVaultSeed() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.SEED);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final Integer getVaultGlobalTime() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.CLOCK).get(TickClock.GLOBAL_TIME);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final Integer getVaultLogicalTime() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.CLOCK).get(TickClock.LOGICAL_TIME);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final Integer getVaultDisplayTime() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.CLOCK).get(TickClock.DISPLAY_TIME);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final Boolean isVaultPaused() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.CLOCK).has(TickClock.PAUSED);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final String getVaultTheme() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            ResourceLocation theme = vault.get(Vault.WORLD).get(WorldManager.THEME);
            ThemeKey themeKey =VaultRegistry.THEME.getKey(theme);
            return themeKey.getName();
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final ResourceLocation getVaultThemeId() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.WORLD).get(WorldManager.THEME);
        }
        else {
            return null;
        }
    }

    @LuaFunction
    public final List<String> getPlayers() {
        List<String> names = new ArrayList<>();
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();

            if (!vault.has(Vault.LISTENERS)) return names;

            Listeners listeners = vault.get(Vault.LISTENERS);

            for (Listener listener : listeners.getAll()) {
                UUID uuid = listener.getId();

                ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    names.add(player.getGameProfile().getName());
                } else {
                    var profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
                    profile.ifPresent(gameProfile -> names.add(gameProfile.getName()));
                }
            }
        }
        else {
            return null;
        }


        return names;
    }

    @LuaFunction
    public final List<UUID> getPlayerUUIDs() {
        List<UUID> names = new ArrayList<>();
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();

            if (!vault.has(Vault.LISTENERS)) return names;

            Listeners listeners = vault.get(Vault.LISTENERS);

            for (Listener listener : listeners.getAll()) {
                UUID uuid = listener.getId();

                ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    names.add(player.getUUID());
                } else {
                    var profile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
                    profile.ifPresent(gameProfile -> names.add(gameProfile.getId()));
                }
            }
        }
        else {
            return null;
        }


        return names;
    }

    @LuaFunction
    public final List<String> getObjectives() {
        Optional<Vault> vaultOpt = getVault();
        if(vaultOpt.isPresent()) {
            Vault vault = vaultOpt.get();
            return vault.get(Vault.OBJECTIVES).get(Objectives.LIST).stream().map(objective -> objective.getKey().getId().toString()).toList();
        }
        else {
            return null;
        }
    }



}
