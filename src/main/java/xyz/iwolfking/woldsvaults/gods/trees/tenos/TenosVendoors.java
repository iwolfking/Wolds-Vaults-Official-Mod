package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.TreasureDoorTileEntity;
import iskallia.vault.core.event.CommonEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos.TreasureDoorPalettesAccessor;

import java.util.List;

/**
 * Barter Expert: vendoors this player opens have a chance to use the rich pedestal table, which can
 * roll god, rare and omega pedestals. Inserted at index 0, since the processor is first-wins.
 */
public final class TenosVendoors {
    public static final ResourceLocation RICH_PALETTE = VaultMod.id("vendor_rooms/vendor_rooms_map");

    private static final Object OWNER = new Object();
    private static boolean accessorWarned;

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
            float chance = TenosNodeHandlers.params(TenosNodes.BARTER_EXPERT,
                    TenosNodeHandlers.BarterExpertParams.class).rich_palette_chance();
            if (player.getRandom().nextFloat() >= chance) {
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
        if (palettes.isEmpty() || palettes.contains(RICH_PALETTE)) {
            return;
        }
        palettes.add(0, RICH_PALETTE);
        door.setChanged();
    }
}
