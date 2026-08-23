package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * The Vault Champion's HUD state, pushed clientbound to every runner in the vault. The bar draws the
 * damage pool, which lives in the Champion's unsynced persistent data.
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
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> xyz.iwolfking.woldsvaults.client.champion.ClientChampionHud.update(message.active,
                        message.dealt, message.pool, message.damageMultiplier)));
        context.setPacketHandled(true);
    }
}
