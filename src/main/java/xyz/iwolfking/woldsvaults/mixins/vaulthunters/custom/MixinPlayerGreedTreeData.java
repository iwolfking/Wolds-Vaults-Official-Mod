package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.world.data.PlayerGreedTreeData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerGreedTreeData.class, remap = false)
public abstract class MixinPlayerGreedTreeData {
    /**
     * Blocks the two quest writes that are reachable without a network message: the greed command's
     * {@code complete_quest}, which paid reputation, and the lazy population that ran whenever the
     * trader's quest tab was opened. The quest system is retired, and the addon's old auto-refresh
     * inject on {@code completeQuest} went with it.
     */
    @Inject(method = "completeQuest", at = @At("HEAD"), cancellable = true)
    private void cancelQuestCompletion(ServerPlayer player, int slotIndex, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "populateQuestsIfNeeded", at = @At("HEAD"), cancellable = true)
    private void cancelQuestPopulation(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
