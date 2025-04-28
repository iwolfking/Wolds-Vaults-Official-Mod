package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import iskallia.vault.util.CoinDefinition;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.checkerframework.checker.units.qual.K;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(value = CoinDefinition.class, remap = false)
public class MixinCoinDefinition {
    @Shadow public static Map<Item, CoinDefinition> COIN_DEFINITIONS;

    @Shadow @Final public int coinValue;
    @Shadow @Final public Item coinItem;

    //Add new coin definitions
    @Inject(method = "getCoinDefinition", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 3))
    private static void addNewCurrenciesToCoinDefinition(Item coin, CallbackInfoReturnable<Optional<CoinDefinition>> cir) {
        COIN_DEFINITIONS.put(ModBlocks.VAULT_PALLADIUM, new CoinDefinition(ModBlocks.VAULT_PALLADIUM, (Item)ModBlocks.VAULT_IRIDIUM, iskallia.vault.init.ModBlocks.VAULT_PLATINUM, 6561));
        COIN_DEFINITIONS.put(ModBlocks.VAULT_IRIDIUM, new CoinDefinition(ModBlocks.VAULT_IRIDIUM, (Item)null, ModBlocks.VAULT_PALLADIUM, 59049));
    }

    //Modify platinum definition
    @Redirect(method = "getCoinDefinition", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 3))
    private static Object addPalladiumAsNextCurrencyForPlat(Map instance, Object k, Object v) {
        return instance.put(iskallia.vault.init.ModBlocks.VAULT_PLATINUM, new CoinDefinition(iskallia.vault.init.ModBlocks.VAULT_PLATINUM, ModBlocks.VAULT_PALLADIUM, iskallia.vault.init.ModBlocks.VAULT_GOLD, 729));
    }

    /**
     * @author iwolfking
     * @reason Drop change returned to player on player instead of adding it to inventory
     */
    @Overwrite
    private static void returnChangeToPlayer(Player player, int change) {
        while (change > 0) {
            for (CoinDefinition definition : COIN_DEFINITIONS.values()) {
                if (definition.coinValue <= change && change / definition.coinValue < 9) {
                    ItemEntity entityitem = new ItemEntity(player.getLevel(), player.getX(), player.getY() + 0.5, player.getZ(), new ItemStack(definition.coinItem, change / definition.coinValue));
                    entityitem.setPickUpDelay(0);
                    entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));
                    player.getLevel().addFreshEntity(entityitem);
                    change -= definition.coinValue * (change / definition.coinValue);
                }
            }
        }
    }
}
