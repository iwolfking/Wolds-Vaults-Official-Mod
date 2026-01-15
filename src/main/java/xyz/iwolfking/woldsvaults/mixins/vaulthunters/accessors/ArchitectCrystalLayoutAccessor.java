package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.world.generator.layout.ArchitectRoomEntry;
import iskallia.vault.item.crystal.layout.ArchitectCrystalLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ArchitectCrystalLayout.class, remap = false)
public interface ArchitectCrystalLayoutAccessor {
    @Accessor("entries")
    ArchitectRoomEntry.List getEntries();
}
