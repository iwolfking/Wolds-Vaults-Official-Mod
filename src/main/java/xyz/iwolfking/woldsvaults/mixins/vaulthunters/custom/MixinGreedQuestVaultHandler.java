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
    /**
     * Kills the greed quest system at its three entry points. The handler's vault listener hooks
     * (LISTENER_JOIN, LISTENER_LEAVE) and its per-player tick are the whole of it: with attach
     * cancelled no quest task is ever bound to a vault, with the tick cancelled nothing progresses
     * or re-attaches, and with the completion check cancelled no quest is ever completed or paid
     * out. Milestones replace quests as the reputation source.
     */
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
