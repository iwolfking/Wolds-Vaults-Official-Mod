package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.card.Card;
import iskallia.vault.item.BoosterPackItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.IPlayerOutcomeHandler;

import java.util.List;

//TODO: Fix/Remove Gambler expertise
@Mixin(value = BoosterPackItem.class, remap = false)
public class MixinBoosterPackItem {
//    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Liskallia/vault/item/BoosterPackItem;getOutcomes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", remap = false), remap = true)
//    private List<Card> passPlayerToOutcomes(ItemStack stack, @Local(argsOnly = true) Player player) {
//        return ((IPlayerOutcomeHandler)stack.getItem()).woldsVaults$getOutcomes(player);
//    }
}
