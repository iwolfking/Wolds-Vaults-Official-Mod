package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.VaultMod;
import iskallia.vault.block.entity.TreasureDoorTileEntity;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultRegistry;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.medallions.stages.VaultStages;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos.TreasureDoorPalettesAccessor;

import java.util.List;

/**
 * Barter Expert: every stock vendoor room this player opens rolls the richer pedestal table, which can yield god,
 * rare and omega pedestals. A door is stock when it carries the stock vendoor palette, or the palette that id
 * resolves to in this vault's greed stage; blacksmith, card and etching vendoors keep their tables. The rich palette
 * is resolved the same way and goes first in the door's palette list.
 */
public final class TenosVendoors {
    private static final ResourceLocation STOCK_PALETTE = VaultMod.id("vendor_rooms/vendor_rooms");
    private static final ResourceLocation RICH_PALETTE = WoldsVaults.id("vendor_rooms/barter_expert");

    private static final Object OWNER = new Object();

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
            enrichPalettes(door, ServerVaults.get(data.getLevel()).orElse(null));
        });
    }

    private static void enrichPalettes(TreasureDoorTileEntity door, Vault vault) {
        if (!(door instanceof TreasureDoorPalettesAccessor accessor)) {
            WoldLogger.errorOnce("tenos-barter-expert-accessor", "Barter Expert is inert: the TreasureDoorTileEntity "
                    + "palette accessor mixin is not applied, so vendoors keep their stock pedestal table.");
            return;
        }
        List<ResourceLocation> palettes = accessor.woldsvaults$getPalettes();
        if (!palettes.contains(STOCK_PALETTE) && !palettes.contains(VaultStages.resolvePalette(vault, STOCK_PALETTE))) {
            return;
        }
        ResourceLocation rich = VaultStages.resolvePalette(vault, RICH_PALETTE);
        if (palettes.contains(rich)) {
            return;
        }
        if (VaultRegistry.PALETTE.getKey(rich) == null) {
            WoldLogger.errorOnce("tenos-barter-expert-palette-" + rich, "Barter Expert is inert: palette {} is not "
                    + "registered, so vendoors keep their stock pedestal table.", rich);
            return;
        }
        palettes.add(0, rich);
        door.setChanged();
    }
}
