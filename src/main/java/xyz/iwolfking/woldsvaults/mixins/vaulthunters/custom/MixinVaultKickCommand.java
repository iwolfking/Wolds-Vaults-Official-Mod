package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.command.VaultCommand;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.RoyaleObjective;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Listeners;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultCommand.class, remap = false)
public class MixinVaultKickCommand {
    @Inject(method = "lambda$kickFromVault$15", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/player/Listeners;remove(Liskallia/vault/core/world/storage/VirtualWorld;Liskallia/vault/core/vault/Vault;Liskallia/vault/core/vault/player/Listener;)Liskallia/vault/core/vault/player/Listeners;"))
    private static void clearCuriosWhenRoyaleVault(Listener listener, Vault vault, boolean complete, Listeners listeners, VirtualWorld world, CallbackInfo ci) {
        if(listener.getPlayer().isPresent() && VaultUtils.isRoyaleVault(vault)) {
            Player player = listener.getPlayer().get();
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "red_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "red_trinket", 1);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "blue_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "blue_trinket", 1);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "green_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "green_trinket", 1);
        }
    }
}
