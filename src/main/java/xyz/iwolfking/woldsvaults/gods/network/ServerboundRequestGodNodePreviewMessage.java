package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;

import java.util.function.Supplier;

/**
 * Serverbound request for one god node effect's live preview. An effect with no formula, or a resolver
 * that throws, is answered as static so the client stops asking.
 */
public class ServerboundRequestGodNodePreviewMessage extends Message<ServerboundRequestGodNodePreviewMessage> {
    private final String effectId;

    public ServerboundRequestGodNodePreviewMessage() {
        this("");
    }

    public ServerboundRequestGodNodePreviewMessage(String effectId) {
        this.effectId = effectId == null ? "" : effectId;
    }

    @Override
    public ServerboundRequestGodNodePreviewMessage read(FriendlyByteBuf buffer) {
        return new ServerboundRequestGodNodePreviewMessage(buffer.readUtf());
    }

    @Override
    public void write(ServerboundRequestGodNodePreviewMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.effectId);
    }

    @Override
    public void onMessage(ServerboundRequestGodNodePreviewMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || message.effectId.isEmpty()) {
                return;
            }
            String formulaText = GodNodePreviews.formulaText(message.effectId);
            GodNodePreviews.Preview preview = null;
            if (formulaText != null) {
                try {
                    preview = GodNodePreviews.resolve(player, message.effectId).orElse(null);
                } catch (RuntimeException e) {
                    WoldsVaults.LOGGER.error("God node preview for '{}' failed for {}; answering it as static.",
                            message.effectId, player.getGameProfile().getName(), e);
                }
            }
            NetworkHandler.INSTANCE.reply(new ClientboundGodNodePreviewMessage(message.effectId,
                    preview == null ? null : formulaText, preview), ctx);
        });
        ctx.setPacketHandled(true);
    }
}
