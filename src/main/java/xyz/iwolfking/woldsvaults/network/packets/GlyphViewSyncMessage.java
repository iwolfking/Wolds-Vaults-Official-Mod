package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.spellcraft.state.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server to client: the glyph candidates the player's equipped sources offer, so the client builds the same glyph views
 * as the server. {@code shared} holds every transferable line. The client does not trust its own copy of curios and
 * gear for this.
 */
public record GlyphViewSyncMessage(List<GlyphCandidate> shared) {
    private static final int MAX_LINES = 256;

    /** The candidates as one string; the server only resends when it changes. */
    public String signature() {
        return this.shared.toString();
    }

    public static void encode(GlyphViewSyncMessage message, FriendlyByteBuf buffer) {
        write(buffer, message.shared);
    }

    public static GlyphViewSyncMessage decode(FriendlyByteBuf buffer) {
        return new GlyphViewSyncMessage(read(buffer));
    }

    private static void write(FriendlyByteBuf buffer, List<GlyphCandidate> candidates) {
        buffer.writeVarInt(candidates.size());
        for (GlyphCandidate candidate : candidates) {
            GlyphLine line = candidate.line();
            buffer.writeUtf(line.type().id());
            buffer.writeFloat(line.potency());
            buffer.writeFloat(line.quality());
            buffer.writeVarInt(line.cost());
            buffer.writeVarInt(line.uses());
            buffer.writeBoolean(line.transferable());
            buffer.writeVarInt(line.cap() == null ? 0 : line.cap() + 1);
            buffer.writeUtf(candidate.sourceKey());
            buffer.writeUtf(candidate.sourceName());
        }
    }

    private static List<GlyphCandidate> read(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_LINES, buffer.readVarInt());
        List<GlyphCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String id = buffer.readUtf();
            float potency = buffer.readFloat();
            float quality = buffer.readFloat();
            int cost = buffer.readVarInt();
            int uses = buffer.readVarInt();
            boolean transferable = buffer.readBoolean();
            int cap = buffer.readVarInt();
            String key = buffer.readUtf();
            String name = buffer.readUtf();
            GlyphType type = GlyphType.byId(id);
            if (type != null) {
                candidates.add(new GlyphCandidate(new GlyphLine(type, potency, quality, cost, uses, transferable,
                        cap == 0 ? null : cap - 1), key, name));
            }
        }
        return candidates;
    }

    public static void handle(GlyphViewSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientGlyphViews.update(message)));
        context.get().setPacketHandled(true);
    }
}
