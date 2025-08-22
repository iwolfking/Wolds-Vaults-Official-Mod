package xyz.iwolfking.woldsvaults.mixins.blackmarkettweaks;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.BlackMarketRerollsPrestigePowerPower;
import iskallia.vault.skill.prestige.helper.PrestigeHelper;
import iskallia.vault.world.data.PlayerBlackMarketData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(value = ServerboundResetBlackMarketTradesMessage.class, remap = false)
public class MixinServerboundResetBlackMarketTradesMessageMixin {

    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData$BlackMarket;getResetRolls()I"))
    private static int dontConsiderResetRolls(PlayerBlackMarketData.BlackMarket instance) {
        return -1;
    }

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData;get(Lnet/minecraft/server/MinecraftServer;)Liskallia/vault/world/data/PlayerBlackMarketData;"), cancellable = true)
    private static void shrinkItemStack(ServerboundResetBlackMarketTradesMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

            BlockEntity be = serverPlayer.level.getBlockEntity(BlackMarketTweaks.getLastClickedPos(serverPlayer.getUUID()));
            if (!(be instanceof BlackMarketTileEntity)) {
                return;
            }

            double chance = -1;

            PlayerBlackMarketData.BlackMarket playerMarket = PlayerBlackMarketData.get(context.getSender().server).getBlackMarket(context.getSender());
            for(BlackMarketRerollsPrestigePowerPower power : PrestigeHelper.getPrestige(serverPlayer).getAll(BlackMarketRerollsPrestigePowerPower.class, Skill::isUnlocked)) {
                chance = (power.getExtraRerolls() * 0.25);
            }

            Random random = new Random();

            OverSizedInventory container = ((BlackMarketInventory) be).bmt$get();
            if (random.nextFloat() >= chance) {
                ItemStack pearl = container.getItem(0);
                pearl.shrink(dev.attackeight.black_market_tweaks.init.ModConfig.COST.get());
                container.setItem(0, pearl);
            }
            playerMarket.resetTradesWithoutTimer(context.getSender());
        });

        context.setPacketHandled(true);
        ci.cancel();
    }
}
