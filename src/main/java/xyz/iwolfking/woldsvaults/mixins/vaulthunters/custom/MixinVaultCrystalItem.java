package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.crystal.VaultCrystalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = VaultCrystalItem.class)
public class MixinVaultCrystalItem {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if(!GameruleHelper.isEnabled(ModGameRules.ENABLE_VAULTS, context.getLevel())) {
            if(context.getPlayer() != null){
                context.getPlayer().displayClientMessage(new TextComponent("Vaults are disabled on this server!").withStyle(ChatFormatting.RED), true);
            }
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
