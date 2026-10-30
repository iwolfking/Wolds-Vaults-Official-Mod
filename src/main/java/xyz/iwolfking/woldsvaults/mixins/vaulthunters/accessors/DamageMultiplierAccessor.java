package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.util.damage.PlayerDamageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(value = PlayerDamageHelper.DamageMultiplier.class, remap = false)
public interface DamageMultiplierAccessor {
    @Accessor("id")
    UUID getId();

    @Accessor("operation")
    PlayerDamageHelper.Operation getOperation();

    @Accessor("tickTimeout")
    int getTickTimeout();
}
