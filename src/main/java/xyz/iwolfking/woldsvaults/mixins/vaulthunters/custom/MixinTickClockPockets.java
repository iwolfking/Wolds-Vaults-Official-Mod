package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.loadingzone.Pockets;

@Mixin(value = TickClock.class, remap = false)
public abstract class MixinTickClockPockets {
    /** Skips the clock's time step while every online runner is in a pocket; the vault's own pause flags are untouched. */
    @WrapWithCondition(method = "tickServer", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/time/TickClock;tickTime()V"))
    private boolean woldsvaults$holdForPockets(TickClock clock, ServerLevel world) {
        return ServerVaults.get(world).map(vault -> !Pockets.holdsClock(vault)).orElse(true);
    }
}
