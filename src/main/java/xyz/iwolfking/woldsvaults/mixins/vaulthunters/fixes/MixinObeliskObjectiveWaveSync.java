package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ObeliskObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * the_vault 3.21.6's dirty-listener sync cannot see obelisk wave mutations: WAVES holds a
 * plain Wave[] array, and only ICompound field values get dirty listeners attached, so
 * activating a wave, spawning its mobs or counting a kill never re-syncs to clients — every
 * obelisk counter (including the brutal-pillar counters of the subclassing
 * BrutalBossesObjective) freezes at its initial state while the server plays on normally.
 * Re-hash the wave state each server tick and mark WAVES dirty on change, mirroring what VH
 * itself added to RuneBossObjective for its equally sync-invisible FIGHTS field.
 */
@Mixin(value = ObeliskObjective.class, remap = false)
public abstract class MixinObeliskObjectiveWaveSync {
    @Unique
    private boolean woldsVaults$waveSyncInitialized;
    @Unique
    private long woldsVaults$lastWaveSyncState;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void woldsVaults$markWavesDirtyIfChanged(VirtualWorld world, Vault vault, CallbackInfo ci) {
        ObeliskObjective self = (ObeliskObjective) (Object) this;
        ObeliskObjective.Wave[] waves = self.get(ObeliskObjective.WAVES);
        if (waves == null) {
            return;
        }
        long state = -3750763034362895579L;
        for (ObeliskObjective.Wave wave : waves) {
            state = woldsVaults$mix(state, wave.has(ObeliskObjective.Wave.ACTIVE) ? 1L : 0L);
            state = woldsVaults$mix(state, wave.getOr(ObeliskObjective.Wave.COUNT, 0));
            state = woldsVaults$mix(state, wave.getOr(ObeliskObjective.Wave.TARGET, 0));
            state = woldsVaults$mix(state, wave.get(ObeliskObjective.Wave.MOBS) == null ? 0L : wave.get(ObeliskObjective.Wave.MOBS).size());
        }
        if (this.woldsVaults$waveSyncInitialized && this.woldsVaults$lastWaveSyncState == state) {
            return;
        }
        this.woldsVaults$waveSyncInitialized = true;
        this.woldsVaults$lastWaveSyncState = state;
        self.markDirty(ObeliskObjective.WAVES);
    }

    @Unique
    private static long woldsVaults$mix(long hash, long value) {
        return (hash ^ value) * 1099511628211L;
    }
}
