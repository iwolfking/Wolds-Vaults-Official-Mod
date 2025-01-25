package xyz.iwolfking.woldsvaults.integration.scannable.filters;

import iskallia.vault.entity.entity.SpiritEntity;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public enum VaultSpiritScanFilter implements Predicate<Entity> {
    INSTANCE;

    private VaultSpiritScanFilter() {
    }

    public boolean test(Entity entity) {
        return entity instanceof SpiritEntity;
    }
}
