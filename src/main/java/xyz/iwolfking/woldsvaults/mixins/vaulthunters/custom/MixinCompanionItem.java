package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.CompanionItem;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.vault.PlayerNoTemporalShardModifier;

@Mixin(value = CompanionItem.class, remap = false)
public class MixinCompanionItem {
    @Inject(method = "activateTemporalModifier", at = @At("HEAD"), cancellable = true)
    private static void preventActivation(ServerPlayer player, ItemStack stack, Vault vault, CallbackInfo ci) {
        if(vault.get(Vault.MODIFIERS).getModifiers().stream().anyMatch(vaultModifier -> vaultModifier instanceof PlayerNoTemporalShardModifier)) {
            player.displayClientMessage(new TextComponent("The Companion does not respond."), true);
            ci.cancel();
        }
    }
}
