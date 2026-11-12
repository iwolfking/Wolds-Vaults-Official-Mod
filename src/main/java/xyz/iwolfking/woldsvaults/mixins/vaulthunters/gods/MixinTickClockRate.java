package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.core.vault.time.TickClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRate;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRateHolder;

@Mixin(value = TickClock.class, remap = false)
public abstract class MixinTickClockRate implements VaultClockRateHolder {
    @Unique
    private VaultClockRate.State woldsvaults$clockRate;

    @Shadow
    protected abstract void tickTime();

    @Override
    public VaultClockRate.State woldsvaults$getClockRate() {
        if (this.woldsvaults$clockRate == null) {
            this.woldsvaults$clockRate = new VaultClockRate.State();
        }
        return this.woldsvaults$clockRate;
    }

    /**
     * @author PoorMansPhysicist
     * @reason gate the clock's one unconditional per-server-tick advance behind a rate
     * accumulator, so a vault second can be longer or shorter than twenty game ticks
     */
    @Redirect(method = "tickServer", at = @At(value = "INVOKE",
            target = "Liskallia/vault/core/vault/time/TickClock;tickTime()V"))
    private void woldsvaults$rateGatedTickTime(TickClock clock) {
        VaultClockRate.State rate = this.woldsvaults$getClockRate();
        if (rate.factors.isEmpty()) {
            this.tickTime();
            return;
        }
        int ticks = rate.consume();
        for (int i = 0; i < ticks; i++) {
            this.tickTime();
        }
    }
}
