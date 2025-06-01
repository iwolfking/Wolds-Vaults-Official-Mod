package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.core.net.BitBuffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.data.ClientDivinityData;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;

import java.util.function.Supplier;

public class KnownDivinityTreeMessage {
    private DivinityTree tree;

    public KnownDivinityTreeMessage(DivinityTree tree) {
        this.tree = tree;
    }

    public DivinityTree getTree() {
        return this.tree;
    }

    public static void encode(KnownDivinityTreeMessage message, FriendlyByteBuf buffer) {
        ArrayBitBuffer bits = ArrayBitBuffer.empty();
        message.tree.writeBits(bits);
        buffer.writeLongArray(bits.toLongArray());
    }

    public static KnownDivinityTreeMessage decode(FriendlyByteBuf buffer) {
        BitBuffer bits = ArrayBitBuffer.backing(buffer.readLongArray(), 0);
        DivinityTree tree = new DivinityTree();
        tree.readBits(bits);
        return new KnownDivinityTreeMessage(tree);
    }

    public static void handle(KnownDivinityTreeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientDivinityData.update(message);
        });
        context.setPacketHandled(true);
    }
}
