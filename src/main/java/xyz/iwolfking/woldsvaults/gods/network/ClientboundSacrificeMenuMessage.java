package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Clientbound snapshot of the receiving player's sacrificial-altar state: the selected god and,
 * per god, completed sacrifices, whether god XP has filled the pending level, and the current
 * gate's item requirements with progress. Opens the altar screen, or refreshes it when already
 * open.
 */
public class ClientboundSacrificeMenuMessage extends Message<ClientboundSacrificeMenuMessage> {
    @Nullable
    public VaultGod selectedGod;
    /**
     * True for a snapshot that only updates an altar screen the player already has open; only
     * the block's own right-click opens the screen, so a sacrifice fired by redstone while the
     * owner is elsewhere cannot pop a menu on them.
     */
    public boolean refreshOnly;
    public final EnumMap<VaultGod, GodSnapshot> gods = new EnumMap<>(VaultGod.class);

    public static class GodSnapshot {
        public int completed;
        public boolean xpReady;
        @Nullable
        public String gateLabel;
        public final List<EntrySnapshot> entries = new ArrayList<>();
    }

    public record EntrySnapshot(ResourceLocation item, int required, int have) {
    }

    @Override
    public ClientboundSacrificeMenuMessage read(FriendlyByteBuf buffer) {
        ClientboundSacrificeMenuMessage message = new ClientboundSacrificeMenuMessage();
        message.refreshOnly = buffer.readBoolean();
        message.selectedGod = buffer.readBoolean() ? buffer.readEnum(VaultGod.class) : null;
        int godCount = buffer.readVarInt();
        for (int i = 0; i < godCount; i++) {
            VaultGod god = buffer.readEnum(VaultGod.class);
            GodSnapshot snapshot = new GodSnapshot();
            snapshot.completed = buffer.readVarInt();
            snapshot.xpReady = buffer.readBoolean();
            if (buffer.readBoolean()) {
                snapshot.gateLabel = buffer.readUtf();
                int entryCount = buffer.readVarInt();
                for (int j = 0; j < entryCount; j++) {
                    snapshot.entries.add(new EntrySnapshot(buffer.readResourceLocation(),
                            buffer.readVarInt(), buffer.readVarInt()));
                }
            }
            message.gods.put(god, snapshot);
        }
        return message;
    }

    @Override
    public void write(ClientboundSacrificeMenuMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.refreshOnly);
        buffer.writeBoolean(message.selectedGod != null);
        if (message.selectedGod != null) {
            buffer.writeEnum(message.selectedGod);
        }
        buffer.writeVarInt(message.gods.size());
        message.gods.forEach((god, snapshot) -> {
            buffer.writeEnum(god);
            buffer.writeVarInt(snapshot.completed);
            buffer.writeBoolean(snapshot.xpReady);
            buffer.writeBoolean(snapshot.gateLabel != null);
            if (snapshot.gateLabel != null) {
                buffer.writeUtf(snapshot.gateLabel);
                buffer.writeVarInt(snapshot.entries.size());
                for (EntrySnapshot entry : snapshot.entries) {
                    buffer.writeResourceLocation(entry.item());
                    buffer.writeVarInt(entry.required());
                    buffer.writeVarInt(entry.have());
                }
            }
        });
    }

    @Override
    public void onMessage(ClientboundSacrificeMenuMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.screens.gods.SacrificeAltarScreen.openOrRefresh(message)));
        context.get().setPacketHandled(true);
    }
}
