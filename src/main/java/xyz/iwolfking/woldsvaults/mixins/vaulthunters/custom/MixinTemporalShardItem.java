package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.gear.TemporalShardItem;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.modifiers.vault.PlayerNoTemporalShardModifier;

@Mixin(value = TemporalShardItem.class, remap = false)
public class MixinTemporalShardItem {
    @Inject(method = "lambda$use$2", at = @At("HEAD"), cancellable = true)
    private static void preventUse(ItemStack stack, Player player, Level level, Vault vault, CallbackInfo ci) {
        if(vault.get(Vault.MODIFIERS).getModifiers().stream().anyMatch(vaultModifier -> vaultModifier instanceof PlayerNoTemporalShardModifier)) {
            player.displayClientMessage(new TextComponent("The relic does not respond."), true);
            ci.cancel();
        }
    }
}
