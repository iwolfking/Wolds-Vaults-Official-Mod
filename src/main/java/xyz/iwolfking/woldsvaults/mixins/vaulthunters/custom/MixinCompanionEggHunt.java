package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.companion.CompanionEggHunt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CompanionEggHunt.class, remap = false, priority = 15000)
public class MixinCompanionEggHunt {

//    @Inject(method = "createHunt", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/companion/HuntInstance;set(Liskallia/vault/core/data/key/FieldKey;Ljava/lang/Object;)Liskallia/vault/core/data/DataObject;", ordinal = 1))
//    private void companionEggHuntNotification(ServerPlayer player, Vault vault, CallbackInfo ci) {
//        player.displayClientMessage(new TextComponent("A companion is watching you!").withStyle(ChatFormatting.GREEN), false);
//    }
}
