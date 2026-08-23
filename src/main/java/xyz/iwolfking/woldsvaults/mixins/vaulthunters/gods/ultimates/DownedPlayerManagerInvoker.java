package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.ultimates;

import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Access to the private revive completion, so Savior can revive without a revive channel. */
@Mixin(value = DownedPlayerManager.class, remap = false)
public interface DownedPlayerManagerInvoker {
    @Invoker("completeRevive")
    static void woldsvaults$completeRevive(ServerPlayer rescuer, ServerPlayer downedPlayer) {
        throw new UnsupportedOperationException();
    }
}
