package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.MinorTransferSlots;

import java.util.function.Supplier;

/**
 * Serverbound request to put a learned minor star into a minor-transfer slot, or clear one (empty effect
 * id). {@link MinorTransferSlots#assign} re-validates; a refusal logs at WARN.
 */
public class ServerboundSetMinorTransferMessage extends Message<ServerboundSetMinorTransferMessage> {
    private final VaultGod god;
    private final int slot;
    private final String effectId;

    public ServerboundSetMinorTransferMessage() {
        this(VaultGod.IDONA, 0, "");
    }

    public ServerboundSetMinorTransferMessage(VaultGod god, int slot, String effectId) {
        this.god = god;
        this.slot = slot;
        this.effectId = effectId == null ? "" : effectId;
    }

    @Override
    public ServerboundSetMinorTransferMessage read(FriendlyByteBuf buffer) {
        return new ServerboundSetMinorTransferMessage(buffer.readEnum(VaultGod.class), buffer.readVarInt(), buffer.readUtf());
    }

    @Override
    public void write(ServerboundSetMinorTransferMessage message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.god);
        buffer.writeVarInt(message.slot);
        buffer.writeUtf(message.effectId);
    }

    @Override
    public void onMessage(ServerboundSetMinorTransferMessage message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            MinorTransferSlots.Result result = MinorTransferSlots.assign(player, message.god, message.slot, message.effectId);
            if (result != MinorTransferSlots.Result.OK && result != MinorTransferSlots.Result.UNCHANGED) {
                WoldsVaults.LOGGER.warn("Refused {}'s transfer slot request ({} slot {} <- '{}'): {}.",
                        player.getGameProfile().getName(), message.god.getName(), message.slot + 1,
                        message.effectId, result);
            }
        });
        ctx.setPacketHandled(true);
    }
}
