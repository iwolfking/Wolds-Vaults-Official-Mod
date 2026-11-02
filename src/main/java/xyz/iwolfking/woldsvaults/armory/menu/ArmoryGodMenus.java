package xyz.iwolfking.woldsvaults.armory.menu;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.ArmoryGodSnapshot;
import xyz.iwolfking.woldsvaults.armory.ArmoryPage;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;

/** The read-only Gods view menu for a saved armory page. */
public final class ArmoryGodMenus {
    private static final int OPEN_DATA_LIMIT = 32600;
    private static final String CHARM_KEY = SlotKeys.curio("charm", 0);

    public static MenuType<ArmoryGodContainer> ARMORY_GODS;

    private ArmoryGodMenus() {
    }

    public static void register(RegistryEvent.Register<MenuType<?>> event) {
        ARMORY_GODS = IForgeMenuType.create(ArmoryGodContainer::client);
        event.getRegistry().register(ARMORY_GODS.setRegistryName(WoldsVaults.id("armory_gods")));
    }

    /**
     * Opens the saved page's god constellations; an empty page opens an empty snapshot. The viewer's live god data
     * is synced after, since the view compares against it.
     */
    public static void open(ServerPlayer player, ArmoryBlockEntity armory, ArmoryViewInfo info) {
        ArmoryPage page = armory.page(info.page());
        ArmoryGodSnapshot snapshot = ArmoryGodSnapshot.of(page.build(), savedCharm(page));
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        info.write(data);
        snapshot.write(data);
        if (data.readableBytes() > OPEN_DATA_LIMIT) {
            WoldsVaults.LOGGER.error("Armory: the Gods view of page {} needs {} bytes, over the {} byte menu limit; not opened",
                    info.page() + 1, data.readableBytes(), OPEN_DATA_LIMIT);
            player.displayClientMessage(new TextComponent("This build is too large to show."), true);
            return;
        }
        NetworkHooks.openGui(player, new SimpleMenuProvider(
                (windowId, inventory, p) -> new ArmoryGodContainer(windowId, p, info, snapshot),
                new TextComponent("Armory")), buf -> buf.writeBytes(data));
        GodAlignmentData.get(player.getServer()).sync(player);
    }

    /** The charm the page holds, else the charm its saved layout records, else empty. */
    private static ItemStack savedCharm(ArmoryPage page) {
        ItemStack stored = page.stored(CHARM_KEY);
        if (!stored.isEmpty()) {
            return stored;
        }
        ItemStack expected = page.expected(CHARM_KEY);
        return expected == null ? ItemStack.EMPTY : expected;
    }
}
