package xyz.iwolfking.woldsvaults.mixins.cast.deferred;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.effect.DeferredReach;

/** A damage-over-time entry created under a spell step's cast keeps that step's glyphs for its ticks. */
@Mixin(targets = "iskallia.vault.util.damage.DamageOverTimeHelper$DamageOverTimeEntry", remap = false)
public class MixinDamageOverTimeEntryGlyphs {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void woldsvaults$keepCastGlyphs(int ticks, DamageSource source, int entityId, float damagePerSecond, ActiveFlags flag,
                                            CallbackInfo ci) {
        DeferredReach.noteDamageOverTime(source);
    }
}
