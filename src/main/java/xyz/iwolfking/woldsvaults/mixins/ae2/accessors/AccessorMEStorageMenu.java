package xyz.iwolfking.woldsvaults.mixins.ae2.accessors;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.MEStorage;
import appeng.menu.me.common.MEStorageMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MEStorageMenu.class, remap = false)
public interface AccessorMEStorageMenu {
    @Accessor("storage")
    MEStorage getStorage();

    @Accessor("powerSource")
    IEnergySource getPowerSource();

    @Invoker("canInteractWithGrid")
    boolean invokeCanInteractWithGrid();
}