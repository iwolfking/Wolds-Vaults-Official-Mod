package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.Map;
import java.util.function.Supplier;

public class ClientboundSyncGamerulesMessage {
    private Map<String, Boolean> gamerules;

    public ClientboundSyncGamerulesMessage(Map<String, Boolean> gamerules) {
        this.gamerules = gamerules;
    }

    public static void encode(ClientboundSyncGamerulesMessage message, FriendlyByteBuf buffer) {
        buffer.writeMap(message.gamerules, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeBoolean);
    }

    public static ClientboundSyncGamerulesMessage decode(FriendlyByteBuf buffer) {
        return new ClientboundSyncGamerulesMessage(buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readBoolean));
    }

    public static void handle(ClientboundSyncGamerulesMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handle(message));
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(ClientboundSyncGamerulesMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null) {
            for(String rule : message.gamerules.keySet()) {
                switch (rule) {
                    case "enablePlacingVaultDolls" ->  ((GameRules.BooleanValue)player.getLevel().getGameRules().getRule(ModGameRules.ENABLE_PLACING_VAULT_DOLLS)).set(message.gamerules.get(rule), null);
                    case "enableVaultDolls" -> ((GameRules.BooleanValue)player.getLevel().getGameRules().getRule(ModGameRules.ENABLE_VAULT_DOLLS)).set(message.gamerules.get(rule), null);
                    case "vaultAllowMentoring" -> ((GameRules.BooleanValue)player.getLevel().getGameRules().getRule(iskallia.vault.init.ModGameRules.ALLOW_MENTOR_BREW)).set(message.gamerules.get(rule), null);
                    case "vaultAllowKnowledgeBrew" -> ((GameRules.BooleanValue)player.getLevel().getGameRules().getRule(iskallia.vault.init.ModGameRules.ALLOW_KNOWLEDGE_BREW)).set(message.gamerules.get(rule), null);
                }
            }
        }
    }
}
