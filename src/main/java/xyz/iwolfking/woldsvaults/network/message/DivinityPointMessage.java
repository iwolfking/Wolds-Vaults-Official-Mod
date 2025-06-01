package xyz.iwolfking.woldsvaults.network.message;

import iskallia.vault.client.gui.screen.player.legacy.ILegacySkillTreeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.util.DivinityUtils;

import java.util.function.Supplier;

public class DivinityPointMessage {

    public final int unspentDivinityPoints;

    public DivinityPointMessage(int unspentDivinityPoints) {
        this.unspentDivinityPoints = unspentDivinityPoints;
    }

    public static void encode(DivinityPointMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.unspentDivinityPoints);
    }

    public static DivinityPointMessage decode(FriendlyByteBuf buffer) {
        int unspentDivinityPoints = buffer.readInt();
        return new DivinityPointMessage(unspentDivinityPoints);
    }

    public static void handle(DivinityPointMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DivinityUtils.unspentDivinityPoints = message.unspentDivinityPoints;
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof ILegacySkillTreeScreen skillTreeScreen) {
                skillTreeScreen.update();
            }

        });
        context.setPacketHandled(true);
    }
}

