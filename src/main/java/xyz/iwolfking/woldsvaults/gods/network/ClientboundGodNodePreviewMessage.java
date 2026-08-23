package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.gods.ClientGodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The server's answer to a god node preview request: either the effect has no live formula, or
 * the formula text the description carries, the multiplier it resolves to for this player right
 * now and the worked math to show on hover.
 */
public class ClientboundGodNodePreviewMessage extends Message<ClientboundGodNodePreviewMessage> {
    private final String effectId;
    private final String formulaText;
    private final GodNodePreviews.Preview preview;

    public ClientboundGodNodePreviewMessage() {
        this("", null, null);
    }

    public ClientboundGodNodePreviewMessage(String effectId, @Nullable String formulaText, @Nullable GodNodePreviews.Preview preview) {
        this.effectId = effectId;
        this.formulaText = formulaText;
        this.preview = preview;
    }

    @Override
    public ClientboundGodNodePreviewMessage read(FriendlyByteBuf buffer) {
        String effectId = buffer.readUtf();
        if (!buffer.readBoolean()) {
            return new ClientboundGodNodePreviewMessage(effectId, null, null);
        }
        String formulaText = buffer.readUtf();
        double multiplier = buffer.readDouble();
        int count = buffer.readVarInt();
        List<Component> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            lines.add(buffer.readComponent());
        }
        return new ClientboundGodNodePreviewMessage(effectId, formulaText, new GodNodePreviews.Preview(multiplier, lines));
    }

    @Override
    public void write(ClientboundGodNodePreviewMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.effectId);
        boolean dynamic = message.preview != null && message.formulaText != null;
        buffer.writeBoolean(dynamic);
        if (!dynamic) {
            return;
        }
        buffer.writeUtf(message.formulaText);
        buffer.writeDouble(message.preview.multiplier());
        buffer.writeVarInt(message.preview.lines().size());
        for (Component line : message.preview.lines()) {
            buffer.writeComponent(line);
        }
    }

    @Override
    public void onMessage(ClientboundGodNodePreviewMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientGodNodePreviews.set(message.effectId,
                message.preview == null || message.formulaText == null ? null
                        : new ClientGodNodePreviews.Preview(message.formulaText, message.preview.multiplier(), message.preview.lines())));
        context.get().setPacketHandled(true);
    }
}
