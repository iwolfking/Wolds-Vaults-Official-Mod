package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.rune.RuneBossAnimation;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckRuneBossFightFreeze;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.List;
import java.util.UUID;

@Mixin(value = RuneBossFight.class, remap = false)
public class MixinRuneBossFight implements DuckRuneBossFightFreeze {
    @Shadow
    private BlockPos origin;
    @Shadow
    private UUID bossId;
    @Shadow
    private boolean completed;
    @Shadow
    @Final
    private RuneBossAnimation animation;

    @Unique
    private Entity woldsVaults$boss;
    @Unique
    private boolean woldsVaults$bossUnloaded;

    /**
     * Vanilla treats a boss it cannot look up as gone and completes the fight, so a hyperboss
     * whose chunk unloads (every fighter disconnected) or whose id was lost on reload (bossId
     * is not saved) would count as a kill. In hyper vaults the whole fight tick is skipped
     * until the boss is loaded again; only a killed or discarded boss lets vanilla complete it.
     */
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$freezeWhileBossUnloaded(VirtualWorld world, Vault vault, CallbackInfo ci) {
        if (this.completed || this.animation.getState() != RuneBossAnimation.State.FIGHT) {
            return;
        }
        List<HyperVaultObjective> hyper = vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class);
        if (hyper.isEmpty()) {
            return;
        }
        if (this.bossId == null && this.woldsVaults$boss == null) {
            UUID saved = hyper.get(0).getOr(HyperVaultObjective.BOSS_ID, null);
            if (saved != null) {
                this.bossId = saved;
                WoldsVaults.LOGGER.warn("Restored hyperboss id {} into the rune fight at {} after a reload.", saved, this.origin);
            }
        }
        if (this.bossId == null) {
            return;
        }
        Entity boss = world.getEntity(this.bossId);
        if (boss != null) {
            this.woldsVaults$boss = boss;
            if (this.woldsVaults$bossUnloaded) {
                this.woldsVaults$bossUnloaded = false;
                WoldsVaults.LOGGER.info("Hyperboss at {} is loaded again; resuming the fight.", this.origin);
            }
            return;
        }
        Entity.RemovalReason reason = this.woldsVaults$boss == null ? null : this.woldsVaults$boss.getRemovalReason();
        if (reason != null && reason != Entity.RemovalReason.UNLOADED_TO_CHUNK && reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            return;
        }
        if (!this.woldsVaults$bossUnloaded) {
            this.woldsVaults$bossUnloaded = true;
            WoldsVaults.LOGGER.info("Hyperboss at {} is not loaded (removal reason: {}); freezing the fight until it loads again.", this.origin, reason);
        }
        ci.cancel();
    }

    @Inject(method = "onTick", at = @At("TAIL"))
    private void woldsVaults$captureSummonedBoss(VirtualWorld world, Vault vault, CallbackInfo ci) {
        if (this.woldsVaults$boss == null && this.bossId != null) {
            this.woldsVaults$boss = world.getEntity(this.bossId);
        }
    }

    @Override
    public boolean isBossUnloaded() {
        return this.woldsVaults$bossUnloaded;
    }
}
