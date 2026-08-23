package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.VaultArtisanStationContainer;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.gear.modification.GearModificationAction;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;
import xyz.iwolfking.woldsvaults.maps.MapGodXp;

import java.util.List;
import java.util.Random;

@Mixin(value = GearModificationAction.class, remap = false)
public class MixinGearModificationAction {
    @Redirect(method = "apply", at = @At(value = "INVOKE", target = "Liskallia/vault/util/CoinDefinition;deductCoins(Ljava/util/List;ILiskallia/vault/util/CoinDefinition;)I"))
    private int extractInsteadOfDeduct(List<InventoryUtil.ItemAccess> coinAccesses, int amount, CoinDefinition coinDefinition, @Local(argsOnly = true) ServerPlayer player) {
        boolean extracted = CoinDefinition.extractCurrency(player, coinAccesses, new ItemStack(coinDefinition.coinItem, amount));
        if(extracted) {
            return 0;
        }
        return amount;
    }

    /**
     * Slashes a vault map's Bonus XP implicit whenever the map is actually modified at the artisan
     * station. The modification call is wrapped rather than the method's tail injected: the base
     * method pops the call's boolean and falls through to its single trailing return either way, so
     * a tail inject also fired for a modification that ran and returned {@code false}, charging the
     * map's bonus for a reroll that changed nothing.
     */
    @WrapOperation(method = "apply", at = @At(value = "INVOKE",
            target = "Liskallia/vault/gear/modification/GearModification;apply(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/Random;)Z"))
    private boolean woldsvaults$dropMapBonusXpOnSuccess(GearModification modification, ItemStack gear, ItemStack material,
                                                         Player player, Random random, Operation<Boolean> original,
                                                         @Local(argsOnly = true) VaultArtisanStationContainer container) {
        boolean applied = original.call(modification, gear, material, player, random);
        if (applied) {
            ItemStack input = container.getGearInputSlot().getItem();
            if (input.getItem() instanceof VaultMapItem) {
                MapGodXp.onMapModified(input);
            }
        }
        return applied;
    }
}
