package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.gear.trinket.TrinketEffectRegistry;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.network.message.ServerboundRoyaleSelectTrinketMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ServerboundRoyaleSelectTrinketMessageAccessor;

import java.util.UUID;
import java.util.function.Supplier;

@Mixin(value = ServerboundRoyaleSelectTrinketMessage.class, remap = false)
public class MixinServerboundRoyaleSelectTrinketMessage {
    @Shadow
    @Final
    private ResourceLocation trinket;

    /**
     * @author iwolfking
     * @reason Fix only getting 1 trinket if you pick multiple of the same color.
     */
    @Overwrite
    public static void handle(ServerboundRoyaleSelectTrinketMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = (NetworkEvent.Context) contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = TrinketItem.createBaseTrinket(TrinketEffectRegistry.getEffect(((ServerboundRoyaleSelectTrinketMessageAccessor)message).getTrinket()));
                TrinketItem.setUses(stack, 2);
                VaultUtils.getVault(player.level).ifPresent((vault) -> TrinketItem.addUsedVault(stack, (UUID) vault.get(Vault.ID)));
                TrinketItem.getSlotIdentifier(stack).ifPresent((slot) -> {
                    if(IntegrationCurios.getCurioItemStack(player, slot, 0).isEmpty()) {
                        IntegrationCurios.setCurioItemStack(player, stack, slot, 0);
                    }
                    else {
                        IntegrationCurios.setCurioItemStack(player, stack, slot, 1);
                    }
                });
            }

        });
        context.setPacketHandled(true);
    }
}
