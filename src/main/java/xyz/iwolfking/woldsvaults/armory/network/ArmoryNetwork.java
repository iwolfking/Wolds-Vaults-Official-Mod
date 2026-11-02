package xyz.iwolfking.woldsvaults.armory.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** Registers the armory messages on the addon channel. */
public final class ArmoryNetwork {
    private ArmoryNetwork() {
    }

    /** Registers every armory message starting at {@code id}; returns the next free id. */
    public static int register(SimpleChannel channel, int id) {
        channel.registerMessage(id++, ServerboundArmoryNavMessage.class, ServerboundArmoryNavMessage::encode,
                ServerboundArmoryNavMessage::decode, ServerboundArmoryNavMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, ServerboundArmorySaveMessage.class, ServerboundArmorySaveMessage::encode,
                ServerboundArmorySaveMessage::decode, ServerboundArmorySaveMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++, ClientboundArmoryStateMessage.class, ClientboundArmoryStateMessage::encode,
                ClientboundArmoryStateMessage::decode, ClientboundArmoryStateMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        return id;
    }
}
