package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.container.VaultArtisanStationContainer;
import iskallia.vault.gear.modification.GearModificationAction;
import iskallia.vault.util.CoinDefinition;
import iskallia.vault.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.items.gear.VaultMapItem;
import xyz.iwolfking.woldsvaults.maps.MapGodXp;

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

    /**
     * Slashes a vault map's Bonus XP implicit whenever the map is modified at the artisan station.
     * TAIL is the single RETURN that follows {@code modification().apply(...)}; every early return in
     * the method sits at a lower bytecode offset, so a refused modification never reaches this.
     */
    @Inject(method = "apply", at = @At("TAIL"))
    private void woldsvaults$dropMapBonusXp(VaultArtisanStationContainer container, ServerPlayer player, CallbackInfo ci) {
        ItemStack gear = container.getGearInputSlot().getItem();
        if (gear.getItem() instanceof VaultMapItem) {
            MapGodXp.onMapModified(gear);
        }
    }
}
