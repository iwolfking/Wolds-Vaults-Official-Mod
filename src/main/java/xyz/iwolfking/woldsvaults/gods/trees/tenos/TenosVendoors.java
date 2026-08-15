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
 * Barter Expert (r102): vendoors this player opens have a 50% chance to use the rich pedestal
 * table, which is the only one that can roll god, rare and omega shop pedestals.
 *
 * <p>A normal vault's vendoor room is 100% plain shop pedestals - the door <em>rate</em> is not
 * modifiable and the god/rare/omega pedestals live in a different palette entirely, so the node
 * has to swap the table rather than change any chance. The swap is an insert at index 0 because
 * the loot tile processor is first-processor-wins; appending would always lose to the plain table.
 *
 * <p>Degrades to vanilla on its own: if the accessor mixin is not applied the cast fails, the
 * failure is logged once, and every vendoor keeps its stock behaviour.
 */
public final class TenosVendoors {
    public static final float RICH_PALETTE_CHANCE = 0.50F;
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
            if (!TenosNodes.hasMinor(player, TenosNodes.BARTER_EXPERT)) {
                return;
            }
            if (player.getRandom().nextFloat() >= RICH_PALETTE_CHANCE) {
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
