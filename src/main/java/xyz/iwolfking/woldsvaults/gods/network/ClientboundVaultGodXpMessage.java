package xyz.iwolfking.woldsvaults.gods.network;

import com.blakebr0.cucumber.network.message.Message;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.gods.ClientVaultGodXp;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Tells the leaving player how much god experience their mapped vault just paid, and for which
 * god, so the vault end screen can print it beside the vault experience. Sent before the award
 * is banked so it always lands ahead of the exit screen opening.
 */
public class ClientboundVaultGodXpMessage extends Message<ClientboundVaultGodXpMessage> {
    private final UUID vaultId;
    private final VaultGod god;
    private final long amount;

    public ClientboundVaultGodXpMessage() {
        this(new UUID(0L, 0L), VaultGod.IDONA, 0L);
    }

    public ClientboundVaultGodXpMessage(UUID vaultId, VaultGod god, long amount) {
        this.vaultId = vaultId;
        this.god = god;
        this.amount = amount;
    }

    @Override
    public ClientboundVaultGodXpMessage read(FriendlyByteBuf buffer) {
        return new ClientboundVaultGodXpMessage(buffer.readUUID(), buffer.readEnum(VaultGod.class), buffer.readVarLong());
    }

    @Override
    public void write(ClientboundVaultGodXpMessage message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.vaultId);
        buffer.writeEnum(message.god);
        buffer.writeVarLong(message.amount);
    }

    @Override
    public void onMessage(ClientboundVaultGodXpMessage message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientVaultGodXp.set(message.vaultId, message.god, message.amount));
        context.get().setPacketHandled(true);
    }
}
