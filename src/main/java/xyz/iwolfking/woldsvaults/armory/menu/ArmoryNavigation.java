package xyz.iwolfking.woldsvaults.armory.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.armory.network.ClientboundArmoryStateMessage;
import xyz.iwolfking.woldsvaults.armory.service.ArmoryService;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.List;

/** Opens armory tabs on the server and pushes the open page's state to the client. */
public final class ArmoryNavigation {
    private ArmoryNavigation() {
    }

    /**
     * Opens {@code tab} of page {@code page} of the armory at {@code pos} (the lower half) for {@code player}.
     * Container tabs open {@link ArmoryMenu}; tree tabs open the read-only tree menus.
     */
    public static void open(ServerPlayer player, BlockPos pos, int page, ArmoryTab tab) {
        if (!(player.getLevel().getBlockEntity(pos) instanceof ArmoryBlockEntity armory)) {
            WoldLogger.warn("Armory: {} asked to open an armory at {} but none is there", player.getGameProfile().getName(), pos);
            return;
        }
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
            return;
        }
        int clamped = Math.max(0, Math.min(ArmoryBlockEntity.PAGES - 1, page));
        ArmoryViewInfo info = info(player, armory, clamped, tab);
        if (!tab.isContainerView()) {
            ArmoryTreeMenus.open(player, armory, info);
            return;
        }
        List<String> keys = ArmoryService.pageKeys(player, armory.page(clamped));
        if (keys.size() > ArmoryMenu.MAX_KEYS) {
            WoldLogger.error("Armory: {} has {} curio slots; the armory shows the first {} and leaves the rest out",
                    player.getGameProfile().getName(), keys.size() - SlotKeys.size(), ArmoryMenu.MAX_KEYS - SlotKeys.size());
            keys = keys.subList(0, ArmoryMenu.MAX_KEYS);
        }
        List<String> shown = keys;
        NetworkHooks.openGui(player, new SimpleMenuProvider((windowId, inventory, p) -> ArmoryMenu.server(windowId, inventory, armory, info, shown),
                new TextComponent("Armory")), buf -> ArmoryMenu.writeOpening(buf, info, shown));
        if (player.containerMenu instanceof ArmoryMenu menu) {
            sendState(player, menu, armory);
        }
    }

    /** The header data for {@code page}, including the Load preview for this player. */
    public static ArmoryViewInfo info(ServerPlayer player, ArmoryBlockEntity armory, int page, ArmoryTab tab) {
        ArmoryService.Preview preview = ArmoryService.preview(player, armory, page);
        return ArmoryViewInfo.of(armory, page, tab, preview.refusals(), preview.notes());
    }

    /** Re-sends the page state and a fresh header (Load preview included) to a player with the menu open. */
    public static void sendState(ServerPlayer player, ArmoryMenu menu, ArmoryBlockEntity armory) {
        ArmoryViewInfo info = info(player, armory, menu.page(), menu.tab());
        ModNetwork.sendToClient(new ClientboundArmoryStateMessage(menu.containerId, info,
                ArmoryClientState.of(armory.page(menu.page()))), player);
    }
}
