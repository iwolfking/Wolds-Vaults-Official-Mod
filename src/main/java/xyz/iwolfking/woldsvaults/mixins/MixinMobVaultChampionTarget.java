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
 * Stops anything from taking a Vault Champion's quarry away from it.
 *
 * <p>The base mod retargets every vault mob roughly every five ticks, to the nearest player within
 * forty-eight blocks - and when there is no player inside that radius, to null. For an ordinary mob
 * that is exactly right. For a boss whose entire premise is that it does not lose you, it means aggro
 * evaporates the moment you put a couple of rooms between you, and the goal that was chasing you stops
 * and clears its path.
 *
 * <p>So a Champion refuses both halves: it will not be handed null while the player it is hunting is
 * still alive in the same level, and it will not be handed somebody else. Re-binding to a new victim
 * goes through the manager, which rewrites the stamp this reads, so the lock never fights a deliberate
 * hand-off.
 *
 * <p>Targeted on {@code Mob} rather than the Vessel because the Vessel does not override
 * {@code setTarget} at all. The cost to every other mob in the game is a single class check.</p>
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
