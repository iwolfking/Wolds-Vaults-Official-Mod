package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobSpawnTickModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(value = MobSpawnTickModifier.class, remap = false)
public class MixinMobSpawnTickModifier {
    @WrapOperation(
            method = "lambda$initServer$1",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/core/vault/modifier/modifier/MobSpawnTickModifier$SpawnTracker;tick(Liskallia/vault/core/world/storage/VirtualWorld;Liskallia/vault/core/vault/Vault;Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/core/vault/modifier/spi/ModifierContext;)V"
            )
    )
    private static void dontTickWhilePaused(
            @Coerce Object instance,
            VirtualWorld world,
            Vault vault,
            ServerPlayer player,
            ModifierContext context,
            Operation<Void> original
    ) {
        if(vault.get(Vault.CLOCK).has(TickClock.PAUSED)) {
            return;
        }

        original.call(instance, world, vault, player, context);
    }
}
