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
 * The player's alignment with all four gods; sent on login and on every mutation of {@link GodAlignmentData}.
 * Raw reputation rides along because god points and the charting threshold both read it client side.
 */
public class GodAlignmentSyncMessage extends Message<GodAlignmentSyncMessage> {
    private final EnumMap<VaultGod, GodAlignmentData.GodState> states;
    private final EnumMap<VaultGod, Integer> piety;
    private final EnumMap<VaultGod, Integer> reputation;

    public GodAlignmentSyncMessage() {
        this.states = new EnumMap<>(VaultGod.class);
        this.piety = new EnumMap<>(VaultGod.class);
        this.reputation = new EnumMap<>(VaultGod.class);
    }

    public GodAlignmentSyncMessage(Map<VaultGod, GodAlignmentData.GodState> states, Map<VaultGod, Integer> piety,
                                   Map<VaultGod, Integer> reputation) {
        this.states = new EnumMap<>(VaultGod.class);
        this.states.putAll(states);
        this.piety = new EnumMap<>(VaultGod.class);
        this.piety.putAll(piety);
        this.reputation = new EnumMap<>(VaultGod.class);
        this.reputation.putAll(reputation);
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
        EnumMap<VaultGod, Integer> readPiety = new EnumMap<>(VaultGod.class);
        int pietyCount = buffer.readVarInt();
        for (int i = 0; i < pietyCount; i++) {
            readPiety.put(buffer.readEnum(VaultGod.class), buffer.readVarInt());
        }
        EnumMap<VaultGod, Integer> readReputation = new EnumMap<>(VaultGod.class);
        int reputationCount = buffer.readVarInt();
        for (int i = 0; i < reputationCount; i++) {
            readReputation.put(buffer.readEnum(VaultGod.class), buffer.readVarInt());
        }
        return new GodAlignmentSyncMessage(read, readPiety, readReputation);
    }

    @Override
    public void write(GodAlignmentSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.states.size());
        message.states.forEach((god, state) -> {
            buffer.writeEnum(god);
            buffer.writeNbt(state.toNbt());
        });
        buffer.writeVarInt(message.piety.size());
        message.piety.forEach((god, value) -> {
            buffer.writeEnum(god);
            buffer.writeVarInt(value);
        });
        buffer.writeVarInt(message.reputation.size());
        message.reputation.forEach((god, value) -> {
            buffer.writeEnum(god);
            buffer.writeVarInt(value);
        });
    }

    @Override
    public void onMessage(GodAlignmentSyncMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientGodAlignmentData.accept(message.states, message.piety, message.reputation));
        context.get().setPacketHandled(true);
    }
}
