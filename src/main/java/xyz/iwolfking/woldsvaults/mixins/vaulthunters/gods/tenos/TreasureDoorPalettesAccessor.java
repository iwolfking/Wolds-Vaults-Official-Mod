package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos;

import iskallia.vault.block.entity.TreasureDoorTileEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Exposes the palette list a treasure/vendoor door builds its room from (Barter Expert, r102). */
@Mixin(value = TreasureDoorTileEntity.class, remap = false)
public interface TreasureDoorPalettesAccessor {
    @Accessor("palettes")
    List<ResourceLocation> woldsvaults$getPalettes();
}
