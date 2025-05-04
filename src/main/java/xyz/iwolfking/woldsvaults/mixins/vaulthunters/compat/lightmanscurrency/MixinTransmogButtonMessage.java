package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.network.message.transmog.TransmogButtonMessage;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = TransmogButtonMessage.class, remap = false)
public class MixinTransmogButtonMessage {
    @Redirect(method = "lambda$handle$0", at = @At(value = "INVOKE", target = "Liskallia/vault/util/CoinDefinition;deductCoins(Ljava/util/List;ILiskallia/vault/util/CoinDefinition;)I"))
    private static int extractCurrencyInsteadOfDeductCoins(List<InventoryUtil.ItemAccess> countToRemove, int stack, CoinDefinition itemAccess, @Local ServerPlayer sender) {
        boolean extracted = CoinDefinition.extractCurrency(sender, countToRemove, new ItemStack(itemAccess.coinItem, stack));
        if(extracted) {
            return 0;
        }
        return stack;
    }
}
