package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.event.Event;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.world.data.ServerVaults;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.vault.RemoveBlacklistModifier;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = Runner.class, remap = false)
public class MixinRunner {
    @Inject(method = "lambda$initServer$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER), cancellable = true, remap = true)
    private void preventCancelingInteraction(VirtualWorld world, PlayerInteractEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToItems()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "lambda$initServer$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER), cancellable = true)
    private void preventCancelingInteraction(VirtualWorld world, BlockEvent.EntityPlaceEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToBlocks()) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
