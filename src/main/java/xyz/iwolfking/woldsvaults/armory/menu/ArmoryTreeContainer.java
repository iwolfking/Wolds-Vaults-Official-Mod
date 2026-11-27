package xyz.iwolfking.woldsvaults.armory.menu;

import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.skill.tree.SkillTree;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;

import java.util.function.Supplier;

/**
 * The menu behind a read-only armory tree tab: the saved page's tree (built on a fresh config copy), the armory
 * header and the saved point count. It has no slots and accepts no actions.
 */
public class ArmoryTreeContainer<T extends SkillTree> extends NBTElementContainer<T> {
    private final ArmoryViewInfo info;
    private final int savedPoints;

    public ArmoryTreeContainer(Supplier<MenuType<?>> type, int windowId, Player player, T tree, ArmoryViewInfo info, int savedPoints) {
        super(type, windowId, player, tree);
        this.info = info;
        this.savedPoints = savedPoints;
    }

    /** Writes the open packet: header, saved points, then the tree as VH's open-tab packets send it (bit buffer). */
    public static void write(FriendlyByteBuf buf, ArmoryViewInfo info, int savedPoints, SkillTree tree) {
        info.write(buf);
        buf.writeVarInt(savedPoints);
        ArrayBitBuffer bits = ArrayBitBuffer.empty();
        tree.writeBits(bits);
        buf.writeLongArray(bits.toLongArray());
    }

    /** Client side: reads what {@link #write} sent into {@code empty}, a new tree instance. */
    public static <T extends SkillTree> ArmoryTreeContainer<T> client(Supplier<MenuType<?>> type, int windowId, Inventory inventory,
                                                                      FriendlyByteBuf buf, T empty) {
        ArmoryViewInfo info = ArmoryViewInfo.read(buf);
        int points = buf.readVarInt();
        empty.readBits(ArrayBitBuffer.backing(buf.readLongArray(), 0));
        return new ArmoryTreeContainer<>(type, windowId, inventory.player, empty, info, points);
    }

    public ArmoryViewInfo info() {
        return this.info;
    }

    public int savedPoints() {
        return this.savedPoints;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = this.info.pos();
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
