package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.TreasureDoorTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.VaultRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos.TreasureDoorPalettesAccessor;

import java.util.List;

/**
 * Barter Expert: every standard vendoor room this player opens rolls the richer pedestal table,
 * which can yield god, rare and omega pedestals. The door's own palette identifies the room family,
 * so only the doors carrying the stock vendoor table are enriched - blacksmith, card and etching
 * vendoors keep theirs untouched, and mapped vaults get their own table. Inserted at index 0, since
 * the processor is first-wins.
 */
public final class TenosVendoors {
    private static final ResourceLocation STOCK_PALETTE = VaultMod.id("vendor_rooms/vendor_rooms");
    private static final ResourceLocation STOCK_PALETTE_MAP = VaultMod.id("vendor_rooms/vendor_rooms_map");
    private static final ResourceLocation RICH_PALETTE = WoldsVaults.id("vendor_rooms/barter_expert");
    private static final ResourceLocation RICH_PALETTE_MAP = WoldsVaults.id("vendor_rooms/barter_expert_map");

    private static final Object OWNER = new Object();
    private static boolean accessorWarned;
    private static boolean paletteWarned;

    private TenosVendoors() {
    }

    static void register() {
        CommonEvents.VENDOOR_ROOM_OPEN.register(OWNER, data -> {
            if (!(data.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            if (!TenosNodes.isActive(player, TenosNodes.BARTER_EXPERT)) {
                return;
            }
            BlockEntity tile = data.getLevel().getBlockEntity(data.getPos());
            if (!(tile instanceof TreasureDoorTileEntity door)) {
                return;
            }
            enrichPalettes(door);
        });
    }

    private static void enrichPalettes(TreasureDoorTileEntity door) {
        if (!(door instanceof TreasureDoorPalettesAccessor accessor)) {
            if (!accessorWarned) {
                accessorWarned = true;
                WoldsVaults.LOGGER.error("Barter Expert is inert: the TreasureDoorTileEntity palette accessor mixin is "
                        + "not applied, so vendoors keep their vanilla pedestal table.");
            }
            return;
        }
        List<ResourceLocation> palettes = accessor.woldsvaults$getPalettes();
        ResourceLocation rich = richPaletteFor(palettes);
        if (rich == null || palettes.contains(rich)) {
            return;
        }
        if (VaultRegistry.PALETTE.getKey(rich) == null) {
            if (!paletteWarned) {
                paletteWarned = true;
                WoldsVaults.LOGGER.error("Barter Expert is inert: palette {} is not registered, so vendoors keep "
                        + "their stock pedestal table.", rich);
            }
            return;
        }
        palettes.add(0, rich);
        door.setChanged();
    }

    /** The table for this door, or null when it opens onto a room family the node leaves alone. */
    private static ResourceLocation richPaletteFor(List<ResourceLocation> palettes) {
        if (palettes.contains(STOCK_PALETTE_MAP)) {
            return RICH_PALETTE_MAP;
        }
        if (palettes.contains(STOCK_PALETTE)) {
            return RICH_PALETTE;
        }
        return null;
    }
}
