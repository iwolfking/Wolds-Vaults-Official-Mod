package xyz.iwolfking.woldsvaults.integration.cctweaked;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.ServerVaults;
import xyz.iwolfking.woldsvaults.api.core.layout.LayoutDefinitionRegistry;
import xyz.iwolfking.woldsvaults.api.core.layout.lib.LayoutDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public final String getVaultSeed() {
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
    @LuaFunction
    public final DigitalVault getVault() {
        Optional<CrystalData> data = portal.getData();
        return data.map(cd -> {
            UUID vaultId = cd.getProperties().getVaultId();
            Vault vault = ServerVaults.get(vaultId).orElse(null);
            if(vault == null) {
                return null;
            }

            return new DigitalVault(vault);
        }).orElse(null);
    }

    @LuaFunction
    public static class DigitalVault {
        private final Vault vault;

        public DigitalVault(Vault vault) {
            this.vault = vault;
        }

        @LuaFunction
        public UUID getVaultOwner() {
            return vault.get(Vault.OWNER);
        }
    }
}
