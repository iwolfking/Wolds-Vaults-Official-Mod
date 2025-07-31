package xyz.iwolfking.woldsvaults.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.lib.network.packets.StopFlightMessage;
import xyz.iwolfking.woldsvaults.network.message.BrewingAltarParticleMessage;
import xyz.iwolfking.woldsvaults.network.message.ClientboundSyncGamerulesMessage;

public class ModNetwork {
    private static int id = 0;
    private static final String PROTOCOL_VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(WoldsVaults.MOD_ID, "woldsvaults"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void init() {
        CHANNEL.registerMessage(id++, StopFlightMessage.class, StopFlightMessage::encode, StopFlightMessage::decode, StopFlightMessage::handle);
        CHANNEL.registerMessage(id++, BrewingAltarParticleMessage.class, BrewingAltarParticleMessage::encode, BrewingAltarParticleMessage::decode, BrewingAltarParticleMessage::handle);
        CHANNEL.registerMessage(id++, ClientboundSyncGamerulesMessage.class, ClientboundSyncGamerulesMessage::encode, ClientboundSyncGamerulesMessage::decode, ClientboundSyncGamerulesMessage::handle);
    }

    public static <T> void sendToServer(T message) {
        CHANNEL.sendToServer(message);
    }

    public static <T> void sendToClient(T message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <T> void sendToAllClients(T message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <T> void sendToLevel(T message, Level level) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }
}
