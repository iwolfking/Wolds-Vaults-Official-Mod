package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.velara;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.DownedPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraPerserverence;
import xyz.iwolfking.woldsvaults.gods.trees.velara.VelaraSacrificeFlocks;

/** Perserverence's bleed-out adjustment and Sacrifice's flock handover, both on entering the downed state. */
@Mixin(value = DownedPlayerManager.class, remap = false)
public class MixinDownedPlayerManagerVelara {

    /**
     * {@code ordinal = 1} is load-bearing: ordinal 0 is the vault down count fed into {@code getBleedOutTicks},
     * ordinal 1 its return.
     */
    @ModifyVariable(method = "enterDownedState", ordinal = 1,
            at = @At(value = "INVOKE_ASSIGN", target = "Liskallia/vault/config/DownedConfig;getBleedOutTicks(Ljava/lang/String;I)I"))
    private static int velaraPerserverenceTimer(int bleedOutTicks, ServerPlayer player, Vault vault) {
        return VelaraPerserverence.adjustBleedOutTicks(player, bleedOutTicks);
    }

    @Inject(method = "enterDownedState", at = @At("TAIL"))
    private static void velaraRedistributeFlock(ServerPlayer player, Vault vault, CallbackInfo ci) {
        VelaraSacrificeFlocks.rebuildFor(player);
    }
}
