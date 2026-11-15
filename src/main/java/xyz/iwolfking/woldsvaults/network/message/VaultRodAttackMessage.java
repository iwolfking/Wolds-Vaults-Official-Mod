package xyz.iwolfking.woldsvaults.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModItems;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VaultRodAttackMessage {
    public static void encode(VaultRodAttackMessage message, FriendlyByteBuf buffer) {}
    public static VaultRodAttackMessage decode(FriendlyByteBuf buffer) { return new VaultRodAttackMessage(); }

    public static void handle(VaultRodAttackMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                FishingHook hook = player.fishing;
                if (hook != null) {
                    if (hook.getHookedIn() instanceof LivingEntity target) {
                        ItemStack mainHandItem = player.getMainHandItem();
                        if (mainHandItem.is(ModItems.VAULT_ROD)) {
                            WoldLogger.debug("Triggering vault rod attack on server for target: " + target.getName().getString());
                            player.attack(target);
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void clientHook(InputEvent.ClickInputEvent event) {
        WoldLogger.debug("FIRING HOOK EVENT");
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        WoldLogger.debug(String.valueOf(player));
        WoldLogger.debug(String.valueOf(event.isAttack()));

        if (player == null || !event.isAttack()) return;
        WoldLogger.debug(String.valueOf(player.getMainHandItem().is(ModItems.VAULT_ROD)));

        if (!player.getMainHandItem().is(ModItems.VAULT_ROD)) return;

        FishingHook hook = player.fishing;
        WoldLogger.info(String.valueOf(hook));
        if (hook == null) return;

        WoldLogger.debug("Client clicked with Vault Rod while hook is out! Sending packet.");
        ModNetwork.sendToServer(new VaultRodAttackMessage());
        event.setCanceled(true);
    }
}