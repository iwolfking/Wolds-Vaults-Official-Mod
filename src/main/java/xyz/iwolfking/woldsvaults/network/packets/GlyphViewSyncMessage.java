package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.client.spellcraft.ClientGlyphViews;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server to client: the glyph candidates the player's equipped sources offer, so the client builds the same views as
 * the server for any arcana item it shows (its own lines come from the item; everything else from here). {@code shared}
 * holds every transferable line, {@code auto} the auto lines. Curios and gear on the client are not trusted for this.
 */
public record GlyphViewSyncMessage(List<GlyphCandidate> shared, List<GlyphCandidate> auto) {
    private static final int MAX_LINES = 256;

    /** The candidates as one string; the server only resends when it changes. */
    public String signature() {
        return this.shared + "|" + this.auto;
    }

    public static void encode(GlyphViewSyncMessage message, FriendlyByteBuf buffer) {
        write(buffer, message.shared);
        write(buffer, message.auto);
    }

    public static GlyphViewSyncMessage decode(FriendlyByteBuf buffer) {
        return new GlyphViewSyncMessage(read(buffer), read(buffer));
    }

    private static void write(FriendlyByteBuf buffer, List<GlyphCandidate> candidates) {
        buffer.writeVarInt(candidates.size());
        for (GlyphCandidate candidate : candidates) {
            SpellcraftItemData.Line line = candidate.line();
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
            AugmentType type = AugmentType.byId(id);
            if (type != null) {
                candidates.add(new GlyphCandidate(new SpellcraftItemData.Line(type, potency, quality, cost, uses, transferable,
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
