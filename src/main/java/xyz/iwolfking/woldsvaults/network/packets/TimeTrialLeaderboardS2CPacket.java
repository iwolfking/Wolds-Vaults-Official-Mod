package xyz.iwolfking.woldsvaults.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.screens.TimeTrialLeaderboardEntry;
import xyz.iwolfking.woldsvaults.client.screens.TimeTrialLeaderboardScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TimeTrialLeaderboardS2CPacket {

    private final String objective;
    private final long timeRemaining;
    private final List<TimeTrialLeaderboardEntry> entries;

    public TimeTrialLeaderboardS2CPacket(
            String objective,
            long timeRemaining,
            List<TimeTrialLeaderboardEntry> entries
    ) {
        this.objective = objective;
        this.timeRemaining = timeRemaining;
        this.entries = entries;
    }

    public static void encode(TimeTrialLeaderboardS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.objective);
        buf.writeLong(msg.timeRemaining);
        buf.writeInt(msg.entries.size());

        for (TimeTrialLeaderboardEntry e : msg.entries) {
            buf.writeUUID(e.uuid());
            buf.writeUtf(e.name());
            buf.writeLong(e.time());
        }
    }

    public static TimeTrialLeaderboardS2CPacket decode(FriendlyByteBuf buf) {
        String objective = buf.readUtf();
        long remaining = buf.readLong();
        int size = buf.readInt();

        List<TimeTrialLeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(new TimeTrialLeaderboardEntry(
                    buf.readUUID(),
                    buf.readUtf(),
                    buf.readLong()
            ));
        }

        return new TimeTrialLeaderboardS2CPacket(objective, remaining, entries);
    }

    public static void handle(TimeTrialLeaderboardS2CPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> openTimeTrialLeaderboardScreen(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openTimeTrialLeaderboardScreen(TimeTrialLeaderboardS2CPacket msg) {
        Minecraft.getInstance().setScreen(
            new TimeTrialLeaderboardScreen(
                msg.objective,
                msg.timeRemaining,
                msg.entries
            )
        );
    }
}
