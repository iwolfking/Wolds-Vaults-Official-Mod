package xyz.iwolfking.woldsvaults.mixins.blackmarkettweaks;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.BlackMarketRerollsPrestigePowerPower;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.world.data.PlayerBlackMarketData;
import iskallia.vault.world.data.PlayerPrestigePowersData;
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

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/PlayerBlackMarketData$BlackMarket;resetTradesWithoutTimer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private static void useRerollItem(ServerboundResetBlackMarketTradesMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer serverPlayer = context.getSender();
        PrestigeTree prestige = PlayerPrestigePowersData.get(serverPlayer.server).getPowers(serverPlayer);
        for(BlackMarketRerollsPrestigePowerPower power : prestige.getAll(BlackMarketRerollsPrestigePowerPower.class, Skill::isUnlocked)) {
            Random random = new Random();
            if(random.nextBoolean()) {
                return;
            }
        }

        context.enqueueWork(() -> {
            BlockEntity be = serverPlayer.level.getBlockEntity(BlackMarketTweaks.getLastClickedPos(serverPlayer.getUUID()));
            if (be instanceof BlackMarketTileEntity) {
                try {
                    OverSizedInventory container = (OverSizedInventory)be.getClass().getDeclaredField("inventory").get(be);
                    ItemStack pearl = container.getItem(0);
                    pearl.shrink(1);
                    container.setItem(0, pearl);
                } catch (Exception e) {
                    BlackMarketTweaks.LOGGER.error(e.toString());
                }
            }
        });
    }
}
