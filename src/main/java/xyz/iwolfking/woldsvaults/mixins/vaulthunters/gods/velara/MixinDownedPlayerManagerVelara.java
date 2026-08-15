package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.velara;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.trees.velara.Perserverence;
import xyz.iwolfking.woldsvaults.gods.trees.velara.SacrificeFlocks;

/**
 * Perserverence and Sacrifice's integration with the downed system. The bleed-out timer is
 * computed once and then stored in a final field, so the only place to lengthen it is the
 * computation itself; a Sacrifice user going down must hand their flock over in the same tick,
 * which is what the tail injection does.
 */
@Mixin(value = DownedPlayerManager.class, remap = false)
public class MixinDownedPlayerManagerVelara {

    @ModifyVariable(method = "enterDownedState",
            at = @At(value = "INVOKE_ASSIGN", target = "Liskallia/vault/config/DownedConfig;getBleedOutTicks(Ljava/lang/String;I)I"))
    private static int velaraPerserverenceTimer(int bleedOutTicks, ServerPlayer player, Vault vault) {
        return Perserverence.adjustBleedOutTicks(player, bleedOutTicks);
    }

    @Inject(method = "enterDownedState", at = @At("TAIL"))
    private static void velaraRedistributeFlock(ServerPlayer player, Vault vault, CallbackInfo ci) {
        SacrificeFlocks.rebuildFor(player);
    }
}
