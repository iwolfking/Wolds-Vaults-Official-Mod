package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.client.champion.ClientChampionHud;

import java.util.function.Supplier;

/**
 * The Vault Champion's HUD state, pushed to every runner in the vault.
 *
 * <p>The bar draws the damage pool rather than the entity's health, and the pool lives in the
 * Champion's persistent data, which is not synced. A small packet on the manager's cadence is
 * cheaper and simpler than adding a synced data accessor to a base-mod entity through a mixin, and
 * it is also what lets the bar be a vault-wide readout rather than a tracking-range one.
 */
public class ChampionHudMessage {
    private final boolean active;
    private final float dealt;
    private final float pool;
    private final float damageMultiplier;

    public ChampionHudMessage(boolean active, float dealt, float pool, float damageMultiplier) {
        this.active = active;
        this.dealt = dealt;
        this.pool = pool;
        this.damageMultiplier = damageMultiplier;
    }

    public static void encode(ChampionHudMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.active);
        buffer.writeFloat(message.dealt);
        buffer.writeFloat(message.pool);
        buffer.writeFloat(message.damageMultiplier);
    }

    public static ChampionHudMessage decode(FriendlyByteBuf buffer) {
        return new ChampionHudMessage(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat());
    }

    public static void handle(ChampionHudMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientChampionHud.update(message.active, message.dealt, message.pool,
                message.damageMultiplier));
        context.setPacketHandled(true);
    }
}
