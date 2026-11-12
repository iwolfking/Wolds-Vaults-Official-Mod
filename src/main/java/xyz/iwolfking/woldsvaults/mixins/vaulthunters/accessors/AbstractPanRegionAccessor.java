package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractPanRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The skill tree pan's zoom, so overlays drawn inside it can keep a fixed on-screen size. */
@Mixin(value = AbstractPanRegion.class, remap = false)
public interface AbstractPanRegionAccessor {
    @Accessor("viewportScale")
    float woldsvaults$getViewportScale();
}
