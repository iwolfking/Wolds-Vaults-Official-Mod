package xyz.iwolfking.woldsvaults.stats;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Sent while the Statistics tab is open: asks the server for the player's stat values with every listener applied. */
public record ServerboundStatSyncRequestMessage() {
    private static final long MIN_INTERVAL_TICKS = 10L;
    private static final Map<UUID, Long> LAST_REPLY = new ConcurrentHashMap<>();

    public static void encode(ServerboundStatSyncRequestMessage message, FriendlyByteBuf buf) {
    }

    public static ServerboundStatSyncRequestMessage decode(FriendlyByteBuf buf) {
        return new ServerboundStatSyncRequestMessage();
    }

    public static void handle(ServerboundStatSyncRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            long now = player.getLevel().getGameTime();
            Long last = LAST_REPLY.get(player.getUUID());
            if (last != null && now - last < MIN_INTERVAL_TICKS && now >= last) {
                return;
            }
            LAST_REPLY.put(player.getUUID(), now);
            ModNetwork.sendToClient(new ClientboundStatSyncMessage(StatSources.evaluate(player)), player);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Forgets a player's request timer when they log out. */
    public static void forget(UUID player) {
        LAST_REPLY.remove(player);
    }
}
