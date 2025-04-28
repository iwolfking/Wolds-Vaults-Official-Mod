package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.modification.GearModificationAction;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = GearModificationAction.class, remap = false)
public class MixinGearModificationAction {
    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Liskallia/vault/util/CoinDefinition;deductCoins(Ljava/util/List;ILiskallia/vault/util/CoinDefinition;)I"))
    private int extractInsteadOfDeduct(List<InventoryUtil.ItemAccess> countToRemove, int stack, CoinDefinition itemAccess, @Local(argsOnly = true) ServerPlayer player) {
        boolean extracted = CoinDefinition.extractCurrency(player, countToRemove, new ItemStack(itemAccess.coinItem, stack));
        if(extracted) {
            return 0;
        }
        return stack;
    }
}
