package xyz.iwolfking.woldsvaults.mixins.blackmarkettweaks;

import com.bawnorton.mixinsquared.TargetHandler;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.BlackMarketRerollsPrestigePowerPower;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.world.data.PlayerPrestigePowersData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;
import java.util.function.Supplier;

@Mixin(value = ServerboundResetBlackMarketTradesMessage.class, remap = false)
public class MixinServerboundResetBlackMarketTradesMessageMixin {
    @TargetHandler(
            mixin = "dev.attackeight.black_market_tweaks.mixin.ServerboundResetBlackMarketTradesMessageMixin",
            name = "shrinkItemStack"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "HEAD"), cancellable = true)
    private static void addChanceToCostNothing(ServerboundResetBlackMarketTradesMessage message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci, CallbackInfo ci2) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer serverPlayer = context.getSender();
        PrestigeTree prestige = PlayerPrestigePowersData.get(serverPlayer.server).getPowers(serverPlayer);
        for(BlackMarketRerollsPrestigePowerPower power : prestige.getAll(BlackMarketRerollsPrestigePowerPower.class, Skill::isUnlocked)) {
            Random random = new Random();
            if(random.nextBoolean()) {
                ci2.cancel();
            }
        }
    }
}
