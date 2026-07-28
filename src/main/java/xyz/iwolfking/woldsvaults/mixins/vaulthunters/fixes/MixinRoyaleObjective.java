package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.ListenerLeaveEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.VaultRoyaleObjective;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.TrinketHelper;

@Mixin(value = VaultRoyaleObjective.class, remap = false)
public class MixinRoyaleObjective {

    @Inject(method = "lambda$initServer$9", at = @At(value = "INVOKE", target = "Liskallia/vault/util/MiscUtils;clearPlayerInventory(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE))
    private static void clearTrinketSlotsOnCompletion(Vault vault, ListenerLeaveEvent.Data data, CallbackInfo ci, @Local ServerPlayer player) {
            TrinketHelper.clearCurios(player);
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/boss/SkeletonGladiatorEntity;getHealth()F", shift = At.Shift.AFTER))
    private void pauseTimer(VirtualWorld world, Vault vault, CallbackInfo ci) {
        WoldsVaults.LOGGER.info("Ticking Boss pause");
        if (!vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            vault.get(Vault.CLOCK).set(TickClock.PAUSED);
        }

        if (vault.get(Vault.CLOCK).has(TickClock.VISIBLE)) {
            vault.get(Vault.CLOCK).remove(TickClock.VISIBLE);
        }
    }
}
