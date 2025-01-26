package xyz.iwolfking.woldsvaults.integration.scannable.scanning;

import li.cil.scannable.api.scanning.EntityScannerModule;
import li.cil.scannable.api.scanning.ScanResultProvider;
import li.cil.scannable.client.scanning.ScanResultProviders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.integration.scannable.filters.VaultSpiritScanFilter;

import java.util.function.Predicate;

public enum VaultSpiritEntityScannerModule implements EntityScannerModule {
    INSTANCE;


    @Override
    public Predicate<Entity> getFilter(ItemStack itemStack) {
        return VaultSpiritScanFilter.INSTANCE;
    }

    @Override
    public int getEnergyCost(ItemStack itemStack) {
        return 500;
    }

    @Nullable
    @Override
    public ScanResultProvider getResultProvider() {
        return ScanResultProviders.ENTITIES.get();
    }
}
