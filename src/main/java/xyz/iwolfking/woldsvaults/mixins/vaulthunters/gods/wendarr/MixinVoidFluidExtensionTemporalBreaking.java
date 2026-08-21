package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.wendarr;

import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.VoidFluidExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.gods.trees.wendarr.WendarrNodes;

import java.util.UUID;

/**
 * Temporal Breaking (r77): halves how fast time acceleration eats the vault clock.
 *
 * <p>The extension is attached once per player and its {@code PLAYER} field says whose
 * acceleration this instance is draining, so scoping the reduction per player is exact -  two
 * players carrying the node each reduce only their own drain, which is what "cannot stack" means
 * here. A {@code CLOCK_MODIFIER} listener could only cancel the whole extension, never halve it,
 * which is why this is a mixin on the drain amount instead.
 */
@Mixin(value = VoidFluidExtension.class, remap = false)
public abstract class MixinVoidFluidExtensionTemporalBreaking {
    @ModifyVariable(method = "apply", at = @At("STORE"), ordinal = 0)
    private int woldsvaults$halveTimeAcceleration(int decrement, ServerLevel world, TickClock clock) {
        if (decrement <= 0 || world.getServer() == null) {
            return decrement;
        }
        UUID playerId = ((VoidFluidExtension) (Object) this).get(VoidFluidExtension.PLAYER);
        if (playerId == null) {
            return decrement;
        }
        ServerPlayer player = world.getServer().getPlayerList().getPlayer(playerId);
        if (player == null || !WendarrNodes.isActive(player, WendarrNodes.TEMPORAL_BREAKING)) {
            return decrement;
        }
        return Math.max(1, decrement / 2);
    }
}
