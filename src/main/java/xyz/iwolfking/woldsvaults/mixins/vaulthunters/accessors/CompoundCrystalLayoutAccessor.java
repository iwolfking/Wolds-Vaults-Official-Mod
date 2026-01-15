package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.core.world.generator.layout.ArchitectRoomEntry;
import iskallia.vault.item.crystal.layout.ArchitectCrystalLayout;
import iskallia.vault.item.crystal.layout.CompoundCrystalLayout;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = CompoundCrystalLayout.class, remap = false)
public interface CompoundCrystalLayoutAccessor {
    @Accessor("children")
    void setChildren(List<CrystalLayout> children);
}
