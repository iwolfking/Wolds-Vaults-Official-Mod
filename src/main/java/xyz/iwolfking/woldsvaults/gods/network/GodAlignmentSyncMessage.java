package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Clientbound snapshot of the receiving player's own alignment with all four gods. Sent on login
 * and on every server-side mutation of {@link GodAlignmentData}.
 */
public class GodAlignmentSyncMessage extends Message<GodAlignmentSyncMessage> {
    private final EnumMap<VaultGod, GodAlignmentData.GodState> states;

    public GodAlignmentSyncMessage() {
        this.states = new EnumMap<>(VaultGod.class);
    }

    public GodAlignmentSyncMessage(Map<VaultGod, GodAlignmentData.GodState> states) {
        this.states = new EnumMap<>(VaultGod.class);
        this.states.putAll(states);
    }

    @Override
    public GodAlignmentSyncMessage read(FriendlyByteBuf buffer) {
        EnumMap<VaultGod, GodAlignmentData.GodState> read = new EnumMap<>(VaultGod.class);
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            VaultGod god = buffer.readEnum(VaultGod.class);
            CompoundTag tag = buffer.readNbt();
            if (tag == null) {
                WoldsVaults.LOGGER.error("God alignment sync carried a null payload for {}; skipping that god.", god.getName());
                continue;
            }
            read.put(god, GodAlignmentData.GodState.fromNbt(tag));
        }
        return new GodAlignmentSyncMessage(read);
    }

    @Override
    public void write(GodAlignmentSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.states.size());
        message.states.forEach((god, state) -> {
            buffer.writeEnum(god);
            buffer.writeNbt(state.toNbt());
        });
    }

    @Override
    public void onMessage(GodAlignmentSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientGodAlignmentData.accept(message.states));
        context.get().setPacketHandled(true);
    }
}
