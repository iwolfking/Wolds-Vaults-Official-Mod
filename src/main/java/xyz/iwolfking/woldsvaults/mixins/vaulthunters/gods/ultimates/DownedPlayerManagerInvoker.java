package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.ultimates;

import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Access to the private revive completion, which is otherwise reachable only by holding a revive
 * channel open next to the downed player for its full duration. Savior needs the completion
 * without the channel, and reimplementing it would mean duplicating the state removal, pose reset,
 * effect clear, heal and both clientbound packets -  a copy that would silently rot the first time
 * the base mod changes any of them.
 */
@Mixin(value = DownedPlayerManager.class, remap = false)
public interface DownedPlayerManagerInvoker {
    @Invoker("completeRevive")
    static void woldsvaults$completeRevive(ServerPlayer rescuer, ServerPlayer downedPlayer) {
        throw new UnsupportedOperationException();
    }
}
