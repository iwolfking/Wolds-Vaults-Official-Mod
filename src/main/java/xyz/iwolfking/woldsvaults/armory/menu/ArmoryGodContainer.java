package xyz.iwolfking.woldsvaults.armory.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.armory.ArmoryGodSnapshot;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;

/** Slotless menu behind the armory's read-only Gods view; carries the page header and the saved god standing. */
public class ArmoryGodContainer extends GodTreeContainer {
    private final ArmoryViewInfo info;
    private final ArmoryGodSnapshot snapshot;

    public ArmoryGodContainer(int windowId, Player player, ArmoryViewInfo info, ArmoryGodSnapshot snapshot) {
        super(ArmoryGodMenus.ARMORY_GODS, windowId, player);
        this.info = info;
        this.snapshot = snapshot;
    }

    public static ArmoryGodContainer client(int windowId, Inventory inventory, FriendlyByteBuf buf) {
        ArmoryViewInfo info = ArmoryViewInfo.read(buf);
        return new ArmoryGodContainer(windowId, inventory.player, info, ArmoryGodSnapshot.read(buf));
    }

    public ArmoryViewInfo info() {
        return this.info;
    }

    public ArmoryGodSnapshot snapshot() {
        return this.snapshot;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.info.pos();
        return player.level.getBlockEntity(pos) instanceof ArmoryBlockEntity armory && !armory.isRemoved()
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
