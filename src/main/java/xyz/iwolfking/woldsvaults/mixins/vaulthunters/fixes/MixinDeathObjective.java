package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = DeathObjective.class, remap = false)
public class MixinDeathObjective {
    @Inject(method = "lambda$tickListener$1", at = @At("HEAD"), cancellable = true)
    private static void test(Listener listener, StatsCollector collector, CallbackInfo ci) {
        if(collector == null) {
            ci.cancel();
        }
    }

    @Inject(method = "initServer", at = @At("TAIL"))
    private void wolds$enableVaultFailOnPlayerDeath(VirtualWorld world, Vault vault, CallbackInfo ci) {
        if (GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULT_FAIL_ON_PLAYER_DEATH, world)) {
            ((DeathObjective) (Object) this).set(DeathObjective.KILL_ALL_STACK, 1);
        }
    }
}