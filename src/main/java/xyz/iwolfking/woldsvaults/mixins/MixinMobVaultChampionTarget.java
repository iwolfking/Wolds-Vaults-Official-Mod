package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.entity.boss.TheVesselEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.medallions.champion.VaultChampion;

/**
 * Stops anything taking a Vault Champion's quarry: while the hunted player is alive, {@code setTarget}
 * is refused for null and for anyone else. Re-binding rewrites the hunt stamp this reads.
 */
@Mixin(Mob.class)
public abstract class MixinMobVaultChampionTarget {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$championKeepsItsQuarry(LivingEntity target, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof TheVesselEntity champion) || !VaultChampion.isChampion(champion)) {
            return;
        }
        java.util.UUID hunted = VaultChampion.getHuntTarget(champion);
        if (hunted == null) {
            return;
        }
        if (target instanceof Player player && player.getUUID().equals(hunted)) {
            return;
        }
        LivingEntity current = champion.getTarget();
        if (current instanceof Player currentPlayer && currentPlayer.isAlive()
                && currentPlayer.getUUID().equals(hunted)) {
            ci.cancel();
        }
    }
}
