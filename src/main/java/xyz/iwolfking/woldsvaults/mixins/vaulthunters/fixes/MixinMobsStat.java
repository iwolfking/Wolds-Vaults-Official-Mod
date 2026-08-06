package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.stat.MobsStat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mixin(value = MobsStat.class, remap = false)
public class MixinMobsStat {

    @Unique
    private static long woldsVaults$lastOverflowWarn = 0L;

    @Inject(method = "lambda$onDamageDealt$5", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$saturateDamageDealt(float amount, Float total, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(woldsVaults$saturatingAdd(total, amount, "damage_dealt"));
    }

    @Inject(method = "lambda$onDamageReceived$7", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$saturateDamageReceived(float amount, Float total, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(woldsVaults$saturatingAdd(total, amount, "damage_received"));
    }

    /**
     * Accumulates a damage event into a per-mob stat total in double precision and clamps the
     * result to Float.MAX_VALUE, so hyper-scaled damage can never saturate the stored 32-bit
     * float to Infinity (which previously rendered as "Infinity" on the vault end screen and
     * poisoned every later aggregation of the snapshot).
     */
    @Unique
    private static Float woldsVaults$saturatingAdd(Float total, float amount, String stat) {
        double current = Float.isFinite(total) ? total.doubleValue() : Float.MAX_VALUE;
        double addend = Float.isFinite(amount) ? amount : Float.MAX_VALUE;
        double sum = current + addend;
        if (!Float.isFinite(total) || !Float.isFinite(amount) || sum > Float.MAX_VALUE) {
            long now = System.currentTimeMillis();
            if (now - woldsVaults$lastOverflowWarn > 60000L) {
                woldsVaults$lastOverflowWarn = now;
                WoldsVaults.LOGGER.warn("Vault stat {} exceeded 32-bit float range (total {}, event amount {}); clamping to Float.MAX_VALUE", stat, total, amount);
            }
        }
        return (float) Math.min(sum, (double) Float.MAX_VALUE);
    }
}
