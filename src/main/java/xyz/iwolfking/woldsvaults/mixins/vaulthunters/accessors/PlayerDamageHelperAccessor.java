package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.util.damage.PlayerDamageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerDamageHelper.class, remap = false)
public interface PlayerDamageHelperAccessor {
    /** player uuid -> (multiplier id -> live multiplier); the registry behind getDamageMultiplier. */
    @Accessor("multipliers")
    static Map<UUID, Map<UUID, PlayerDamageHelper.DamageMultiplier>> getMultipliers() {
        throw new UnsupportedOperationException();
    }
}
