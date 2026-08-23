package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.greed.GreedQuestVaultHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GreedQuestVaultHandler.class, remap = false)
public class MixinGreedQuestVaultHandler {
    /** Cancels the greed quest system at its three entry points: attach, tick and completion check. */
    @Inject(method = "attachQuestTask", at = @At("HEAD"), cancellable = true)
    private static void cancelQuestAttach(ServerPlayer player, Vault vault, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "checkAndCompleteQuest", at = @At("HEAD"), cancellable = true)
    private static void cancelQuestCompletion(ServerPlayer player, boolean notifyPlayer, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true)
    private static void cancelQuestTick(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
