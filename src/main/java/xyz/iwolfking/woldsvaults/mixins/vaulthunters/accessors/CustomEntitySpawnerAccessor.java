package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.block.entity.CustomEntitySpawnerTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CustomEntitySpawnerTileEntity.class, remap = false)
public interface CustomEntitySpawnerAccessor {
    @Accessor("spawnerGroupName")
    String getSpawnerGroupName();
}
