package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.util.PlayerBerserkHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PlayerBerserkHelper.BerserkEffect.class, remap = false)
public interface BerserkEffectAccessor {
    @Accessor("MULTIPLIER_PER_STACK")
    static float woldsvaults$getMultiplierPerStack() {
        throw new AssertionError();
    }
}
