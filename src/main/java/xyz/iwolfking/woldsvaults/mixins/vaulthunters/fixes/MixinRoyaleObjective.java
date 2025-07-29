package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.common.ListenerLeaveEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.objective.RoyaleObjective;
import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RoyaleObjective.class, remap = false)
public class MixinRoyaleObjective {
    @Inject(method = "lambda$initServer$7", at = @At(value = "INVOKE", target = "Liskallia/vault/util/MiscUtils;clearPlayerInventory(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE))
    private void clearTrinketSlotsOnCompletion(Vault vault, ListenerLeaveEvent.Data data, CallbackInfo ci, @Local ServerPlayer player) {
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "red_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "red_trinket", 1);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "blue_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "blue_trinket", 1);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "green_trinket", 0);
            IntegrationCurios.setCurioItemStack(player, ItemStack.EMPTY, "green_trinket", 1);
    }
}
