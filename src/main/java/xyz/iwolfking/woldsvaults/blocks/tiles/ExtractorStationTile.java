package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.world.item.ItemStack;

/** What the station renderer needs from a controller tile: whether the rig is cycling and what to show in the vitrine. */
public interface ExtractorStationTile {
    boolean isWorking();

    /** The extractor to display inside the machine, or {@link ItemStack#EMPTY} to draw nothing. */
    ItemStack getDisplayExtractor();
}
