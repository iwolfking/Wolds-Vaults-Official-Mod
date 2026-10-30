package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.network.message.ServerboundRoyaleSelectTrinketMessage;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ServerboundRoyaleSelectTrinketMessageAccessor;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRoyale;

@Mixin(value = ServerboundRoyaleSelectTrinketMessage.class, remap = false)
public class MixinServerboundRoyaleSelectTrinketMessage {
    /**
     * @author iwolfking
     * @reason Insert the chosen trinket into the Royale pouch once
     */
    @Overwrite
    public static void handle(ServerboundRoyaleSelectTrinketMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerboundRoyaleSelectTrinketMessageAccessor reward = (ServerboundRoyaleSelectTrinketMessageAccessor) message;
                PouchRoyale.select(player, reward.getTrinket(), reward.getEntityId(), null);
            }
        });
        context.setPacketHandled(true);
    }
}
