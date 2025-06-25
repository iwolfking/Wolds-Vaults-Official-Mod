package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = CoinPouchItem.class, remap = false)
public abstract class MixinCoinPouchItem {
    @Shadow
    public static int getTotalBronzeValue(Inventory playerInventory) {
        return 0;
    }

    /**
     * @author iwolfking
     * @reason Properly get total currency count.
     */
    @Overwrite
    public static int getTotalBronzeValue(Player player) {
        long value = 0;
        value += MoneyUtil.getValue(player.getInventory());
        ItemStack wallet = LightmansCurrency.getWalletStack(player);
        if(!wallet.isEmpty()) {
            value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
        }
        return Math.toIntExact(value);
    }

    /**
     * @author iwolfking
     * @reason Properly get gold amount
     */
    @Overwrite
    public static int getGoldAmount(Inventory playerInventory) {
        long value = 0;
        value += MoneyUtil.getValue(playerInventory);
        ItemStack wallet = LightmansCurrency.getWalletStack(playerInventory.player);
        if(!wallet.isEmpty()) {
            value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
        }
        value = value / 81L;
        return Math.toIntExact(value);
    }
}
