package xyz.iwolfking.woldsvaults.mixins.lightmanscurrency;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.integration.lightmanscurrency.VaultPouchCompatHelper;

import java.util.List;

@Mixin(value = TradeContext.class, remap = false)
public abstract class MixinTradeContext {

    @Shadow
    public abstract boolean hasPlayer();

    @Shadow
    public abstract Player getPlayer();

    @Unique
    private static long woldsvaults$getPouchValue(ItemStack pouch) {
        if (pouch == null || pouch.isEmpty()) {
            return 0L;
        }
        CompoundTag invTag = VaultPouchCompatHelper.getContents(pouch);
        if (invTag == null) {
            return 0L;
        }

        int bronzeCount = invTag.getInt("BronzeStackSize");
        int palladiumCount = invTag.getInt("PalladiumStackSize");
        int iridiumCount = invTag.getInt("IridiumStackSize");

        long singleBronzeValue = MoneyUtil.getValue(new ItemStack(ModBlocks.BRONZE_COIN_PILE.asItem(), 1));

        long totalValue = 0L;
        if (bronzeCount > 0) {
            totalValue += (long) bronzeCount * singleBronzeValue;
        }
        if (palladiumCount > 0) {
            totalValue += MoneyUtil.getValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_PALLADIUM_PILE.asItem(), palladiumCount));
        }
        if (iridiumCount > 0) {
            totalValue += MoneyUtil.getValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_IRIDIUM_PILE.asItem(), iridiumCount));
        }

        return totalValue;
    }

    @ModifyReturnValue(method = "getAvailableFunds", at = @At("RETURN"))
    private long woldsvaults$addPouchFunds(long funds) {
        if (this.hasPlayer()) {
            ItemStack pouch = CoinPouchItem.findFirstPouch(this.getPlayer());
            long pouchVal = woldsvaults$getPouchValue(pouch);
            funds += pouchVal;
        }
        return funds;
    }

    @Inject(method = "getAvailableFundsDescription", at = @At("RETURN"))
    private void woldsvaults$addPouchFundsTooltip(CallbackInfoReturnable<List<Component>> cir) {
        if (this.hasPlayer()) {
            ItemStack pouch = CoinPouchItem.findFirstPouch(this.getPlayer());
            long pouchValue = woldsvaults$getPouchValue(pouch);
            if (pouchValue > 0L) {
                cir.getReturnValue().add(
                        EasyText.translatable("tooltip.lightmanscurrency.trader.info.money.pouch", CoinValue.fromNumber(pouchValue).getString())
                );
            }
        }
    }

    @WrapOperation(
            method = "getPayment(Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/common/traders/TradeContext;hasPlayer()Z",
                    ordinal = 1
            )
    )
    private boolean woldsvaults$drainPouchInGetPayment(
            TradeContext instance,
            Operation<Boolean> original,
            @Local(name = "amountToWithdraw") LocalLongRef amountToWithdrawRef,
            @Share("pouchStack") LocalRef<ItemStack> pouchRef
    ) {
        boolean hasPlayer = original.call(instance);
        long remaining = amountToWithdrawRef.get();
        if (hasPlayer && remaining > 0L) {
            ItemStack pouch = CoinPouchItem.findFirstPouch(instance.getPlayer());
            pouchRef.set(pouch);
            if (!pouch.isEmpty()) {
                long actualDeducted = woldsvaults$deductValueFromPouch(pouch, remaining);
                long newRemaining = Math.max(0L, remaining - actualDeducted);

                amountToWithdrawRef.set(newRemaining);
            }
        }
        return hasPlayer;
    }

    @Unique
    private static long woldsvaults$deductValueFromPouch(ItemStack pouch, long valueToDeduct) {
        if (pouch == null || pouch.isEmpty() || valueToDeduct <= 0L) {
            return 0L;
        }
        CompoundTag invTag = VaultPouchCompatHelper.getContents(pouch);
        if (invTag == null) {
            return 0L;
        }

        long singleBronzeValue = Math.max(1L, MoneyUtil.getValue(new ItemStack(ModBlocks.BRONZE_COIN_PILE.asItem(), 1)));
        long currentBronze = Math.max(0L, invTag.getInt("BronzeStackSize"));
        long currentPal = Math.max(0L, invTag.getInt("PalladiumStackSize"));
        long currentIri = Math.max(0L, invTag.getInt("IridiumStackSize"));

        long palSingleVal = Math.max(0L, MoneyUtil.getValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_PALLADIUM_PILE.asItem(), 1)));
        long iriSingleVal = Math.max(0L, MoneyUtil.getValue(new ItemStack(xyz.iwolfking.woldsvaults.init.ModBlocks.VAULT_IRIDIUM_PILE.asItem(), 1)));

        long totalAvailableValue = currentBronze * singleBronzeValue
                + currentPal * palSingleVal
                + currentIri * iriSingleVal;

        long actualDeductedValue = Math.min(valueToDeduct, totalAvailableValue);
        if (actualDeductedValue <= 0L) {
            return 0L;
        }

        long remainingToDeduct = actualDeductedValue;

        long bronzeToRemove = Math.min(currentBronze, remainingToDeduct / singleBronzeValue);
        if (bronzeToRemove > 0L) {
            currentBronze -= bronzeToRemove;
            remainingToDeduct -= bronzeToRemove * singleBronzeValue;
        }

        if (remainingToDeduct > 0L && palSingleVal > 0L && currentPal > 0L) {
            long palToRemove = Math.min(currentPal, (remainingToDeduct + palSingleVal - 1L) / palSingleVal);
            if (palToRemove > 0L) {
                currentPal -= palToRemove;
                remainingToDeduct -= palToRemove * palSingleVal;
            }
        }
        if (remainingToDeduct > 0L && iriSingleVal > 0L && currentIri > 0L) {
            long iriToRemove = Math.min(currentIri, (remainingToDeduct + iriSingleVal - 1L) / iriSingleVal);
            if (iriToRemove > 0L) {
                currentIri -= iriToRemove;
                remainingToDeduct -= iriToRemove * iriSingleVal;
            }
        }

        if (remainingToDeduct < 0L) {
            long refundBronze = (-remainingToDeduct) / singleBronzeValue;
            currentBronze += refundBronze;
        }

        int clampedBronze = (int) Math.min(2147483582L, Math.max(0L, currentBronze));
        invTag.putInt("BronzeStackSize", clampedBronze);
        invTag.putInt("SilverStackSize", Math.floorDiv(clampedBronze, 9));
        invTag.putInt("GoldStackSize", Math.floorDiv(clampedBronze, 81));
        invTag.putInt("PlatinumStackSize", Math.floorDiv(clampedBronze, 729));
        invTag.putInt("PalladiumStackSize", (int) Math.max(0L, currentPal));
        invTag.putInt("IridiumStackSize", (int) Math.max(0L, currentIri));

        VaultPouchCompatHelper.markContentsChanged(pouch);
        return actualDeductedValue;
    }
}