package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeModel;

import java.util.function.Supplier;

/**
 * Serverbound request to buy one node of a god's constellation tree. The server re-validates
 * everything the client checked: the tree and node exist, the node is enabled and not yet owned,
 * a root or an owned neighbour grants access, and the player has an unspent god point. On success
 * the ledger sync rides along from {@code purchaseTreeNode} and the attribute snapshot is
 * refreshed so stat nodes apply without a relog.
 */
public class ServerboundUnlockGodNodeMessage extends Message<ServerboundUnlockGodNodeMessage> {
    private final VaultGod god;
    private final String nodeId;

    public ServerboundUnlockGodNodeMessage() {
        this(VaultGod.IDONA, "");
    }

    public ServerboundUnlockGodNodeMessage(VaultGod god, String nodeId) {
        this.god = god;
        this.nodeId = nodeId;
    }

    @Override
    public ServerboundUnlockGodNodeMessage read(FriendlyByteBuf buffer) {
        return new ServerboundUnlockGodNodeMessage(buffer.readEnum(VaultGod.class), buffer.readUtf());
    }

    @Override
    public void write(ServerboundUnlockGodNodeMessage message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.god);
        buffer.writeUtf(message.nodeId);
    }

    @Override
    public void onMessage(ServerboundUnlockGodNodeMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.getServer() == null) {
                return;
            }
            GodTreeModel tree = GodNodeRegistry.tree(message.god).orElse(null);
            if (tree == null) {
                WoldsVaults.LOGGER.warn("{} tried to unlock god node {} but the {} tree is not defined.",
                        player.getGameProfile().getName(), message.nodeId, message.god.getName());
                return;
            }
            GodNode node = tree.getNode(message.nodeId);
            if (node == null) {
                WoldsVaults.LOGGER.warn("{} tried to unlock unknown god node {} in the {} tree.",
                        player.getGameProfile().getName(), message.nodeId, message.god.getName());
                return;
            }
            GodAlignmentData data = GodAlignmentData.get(player.getServer());
            if (!tree.isPurchasable(message.nodeId,
                    id -> data.isTreeNodePurchased(player.getUUID(), message.god, id))) {
                WoldsVaults.LOGGER.debug("Refused {} the god node {}: it is disabled, already owned, or touches "
                        + "no node they own.", player.getGameProfile().getName(), message.nodeId);
                return;
            }
            if (!data.purchaseTreeNode(player, message.god, node.id(), node.ledgerKey(), node.cost())) {
                WoldsVaults.LOGGER.debug("Refused {} the god node {}: the ledger would not take it, so they hold "
                        + "fewer than its {} unspent points.", player.getGameProfile().getName(), message.nodeId,
                        node.cost());
                return;
            }
            AttributeSnapshotHelper.getInstance().refreshSnapshotDelayed(player);
        });
        ctx.setPacketHandled(true);
    }
}
