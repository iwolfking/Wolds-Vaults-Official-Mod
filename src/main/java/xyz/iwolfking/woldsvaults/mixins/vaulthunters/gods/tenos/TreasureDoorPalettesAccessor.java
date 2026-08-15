package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos;

import iskallia.vault.block.entity.TreasureDoorTileEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the palette list a treasure/vendoor door will build its room from. The field has no
 * getter and is filled only from NBT, and room generation happens two ticks after the player
 * opened the door with no player in scope, so reaching the list at open time is the only way to
 * make a per-player pedestal table possible (Barter Expert, r102).
 */
@Mixin(value = TreasureDoorTileEntity.class, remap = false)
public interface TreasureDoorPalettesAccessor {
    @Accessor("palettes")
    List<ResourceLocation> woldsvaults$getPalettes();
}
