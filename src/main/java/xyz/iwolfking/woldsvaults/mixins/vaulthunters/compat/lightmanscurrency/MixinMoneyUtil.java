package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.integration.lightmanscurrency.VaultPouchCompatHelper;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = MoneyUtil.class, remap = false)
public abstract class MixinMoneyUtil {

    @Unique
    private static final int[] COMPACTED_SLOTS = {0, 1, 2, 3};
    @Unique
    private static final int[] COMPACTED_MULTIPLIERS = {1, 9, 81, 729};
    @Unique
    private static final int PALLADIUM_SLOT = 8;
    @Unique
    private static final int IRIDIUM_SLOT = 9;

    @WrapOperation(
            method = "ProcessPayment(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/LightmansCurrency;getWalletStack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack woldsvaults$capturePaymentContext(
            Player player,
            Operation<ItemStack> original,
            @Share("player") LocalRef<Player> playerRef,
            @Share("pouchStack") LocalRef<ItemStack> pouchStackRef,
            @Share("pouchStacks") LocalRef<ItemStack[]> pouchStacksRef
    ) {
        playerRef.set(player);
        ItemStack pouch = CoinPouchItem.findFirstPouch(player);
        pouchStackRef.set(pouch);
        if (!pouch.isEmpty()) {
            pouchStacksRef.set(CoinPouchItem.getContainedStacks(pouch));
        }
        return original.call(player);
    }

    @ModifyVariable(
            method = "ProcessPayment(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)Z",
            at = @At(value = "STORE", ordinal = 0),
            name = "rawInventoryValue"
    )
    private static long woldsvaults$includePouchInRawValue(
            long rawInventoryValue,
            @Share("pouchStack") LocalRef<ItemStack> pouchStackRef,
            @Share("pouchStacks") LocalRef<ItemStack[]> pouchStacksRef
    ) {
        ItemStack pouch = pouchStackRef.get();
        ItemStack[] stacks = pouchStacksRef.get();
        if (pouch != null && !pouch.isEmpty() && stacks != null) {
            return rawInventoryValue + woldsvaults$sumStacksValue(stacks);
        }
        return rawInventoryValue;
    }

    @ModifyVariable(
            method = "ProcessPayment(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;getValueNumber()J",
                    shift = At.Shift.BY,
                    by = 2
            ),
            name = "valueToTake"
    )
    private static long woldsvaults$drainPouchInPayment(
            long valueToTake,
            @Share("pouchStack") LocalRef<ItemStack> pouchStackRef,
            @Share("pouchStacks") LocalRef<ItemStack[]> pouchStacksRef,
            @Share("finalValueToTake") LocalLongRef finalValueRef
    ) {
        ItemStack pouch = pouchStackRef.get();
        ItemStack[] originalStacks = pouchStacksRef.get();
        if (valueToTake > 0L && pouch != null && !pouch.isEmpty() && originalStacks != null) {
            NonNullList<ItemStack> workingList = NonNullList.withSize(originalStacks.length, ItemStack.EMPTY);
            for (int i = 0; i < originalStacks.length; i++) {
                workingList.set(i, originalStacks[i] == null ? ItemStack.EMPTY : originalStacks[i].copy());
            }

            long valueBefore = woldsvaults$sumListValue(workingList);
            long remainingAfterTake = MoneyUtil.takeObjectsOfValue(valueToTake, workingList, false);
            long valueAfter = woldsvaults$sumListValue(workingList);
            long actualDeducted = Math.max(0L, valueBefore - valueAfter);

            woldsvaults$syncPouchFromWorkingList(pouch, originalStacks, workingList);

            long netRemaining = valueToTake - actualDeducted;
            if (remainingAfterTake < 0L && remainingAfterTake < netRemaining) {
                netRemaining = remainingAfterTake;
            }
            valueToTake = netRemaining;
        }
        finalValueRef.set(valueToTake);
        return valueToTake;
    }

    @WrapOperation(
            method = "ProcessPayment(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/util/InventoryUtil;TryPutItemStack(Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack woldsvaults$paymentChangeIntoPouch(
            Container inventory,
            ItemStack stack,
            Operation<ItemStack> original,
            @Share("pouchStack") LocalRef<ItemStack> pouchStackRef
    ) {
        ItemStack remainder = woldsvaults$depositIntoPouch(pouchStackRef.get(), stack);
        return remainder.isEmpty() ? ItemStack.EMPTY : original.call(inventory, remainder);
    }

    @WrapOperation(
            method = "ProcessChange(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/LightmansCurrency;getWalletStack(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack woldsvaults$captureChangePouch(
            Player player,
            Operation<ItemStack> original,
            @Share("changePouch") LocalRef<ItemStack> changePouchRef
    ) {
        changePouchRef.set(CoinPouchItem.findFirstPouch(player));
        return original.call(player);
    }

    @WrapOperation(
            method = "ProcessChange(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;Lio/github/lightman314/lightmanscurrency/common/money/CoinValue;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/lightman314/lightmanscurrency/util/InventoryUtil;TryPutItemStack(Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack woldsvaults$processChangeIntoPouch(
            Container inventory,
            ItemStack stack,
            Operation<ItemStack> original,
            @Share("changePouch") LocalRef<ItemStack> changePouchRef
    ) {
        ItemStack remainder = woldsvaults$depositIntoPouch(changePouchRef.get(), stack);
        return remainder.isEmpty() ? ItemStack.EMPTY : original.call(inventory, remainder);
    }

    @Unique
    private static long woldsvaults$sumStacksValue(ItemStack[] stacks) {
        long total = 0L;
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                total += MoneyUtil.getValue(stack);
            }
        }
        return total;
    }

    @Unique
    private static long woldsvaults$sumListValue(NonNullList<ItemStack> list) {
        long total = 0L;
        for (ItemStack stack : list) {
            if (stack != null && !stack.isEmpty()) {
                total += MoneyUtil.getValue(stack);
            }
        }
        return total;
    }

    @Unique
    private static ItemStack woldsvaults$depositIntoPouch(ItemStack pouch, ItemStack stack) {
        if (pouch == null || pouch.isEmpty() || stack == null || stack.isEmpty()) {
            return stack;
        }
        AtomicReference<ItemStack> result = new AtomicReference<>(stack.copy());
        pouch.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            ItemStack current = result.get();
            for (int slot = 0; slot < handler.getSlots() && !current.isEmpty(); slot++) {
                if (handler.isItemValid(slot, current)) {
                    current = handler.insertItem(slot, current, false);
                }
            }
            result.set(current);
        });
        return result.get();
    }

    @Unique
    private static void woldsvaults$syncPouchFromWorkingList(ItemStack pouch, ItemStack[] original, NonNullList<ItemStack> working) {
        CompoundTag invTag = VaultPouchCompatHelper.getContents(pouch);
        if (invTag == null) {
            return;
        }

        boolean compactedChanged = false;
        long newBronzeTotal = 0L;
        for (int i = 0; i < COMPACTED_SLOTS.length; i++) {
            int slot = COMPACTED_SLOTS[i];
            int oldCount = (slot < original.length && original[slot] != null) ? original[slot].getCount() : 0;
            int newCount = slot < working.size() ? working.get(slot).getCount() : 0;
            if (oldCount != newCount) {
                compactedChanged = true;
            }
            newBronzeTotal += (long) newCount * (long) COMPACTED_MULTIPLIERS[i];
        }

        boolean extraChanged = false;
        if (original.length > PALLADIUM_SLOT && working.size() > PALLADIUM_SLOT) {
            int oldPal = original[PALLADIUM_SLOT] == null ? 0 : original[PALLADIUM_SLOT].getCount();
            int newPal = working.get(PALLADIUM_SLOT).getCount();
            if (oldPal != newPal) {
                invTag.putInt("PalladiumStackSize", newPal);
                extraChanged = true;
            }
        }
        if (original.length > IRIDIUM_SLOT && working.size() > IRIDIUM_SLOT) {
            int oldIri = original[IRIDIUM_SLOT] == null ? 0 : original[IRIDIUM_SLOT].getCount();
            int newIri = working.get(IRIDIUM_SLOT).getCount();
            if (oldIri != newIri) {
                invTag.putInt("IridiumStackSize", newIri);
                extraChanged = true;
            }
        }

        if (compactedChanged) {
            int clamped = (int) Math.min(2147483582L, Math.max(0L, newBronzeTotal));
            invTag.putInt("BronzeStackSize", clamped);
            for (int i = 1; i < COMPACTED_MULTIPLIERS.length; i++) {
                String key = switch (i) {
                    case 1 -> "SilverStackSize";
                    case 2 -> "GoldStackSize";
                    case 3 -> "PlatinumStackSize";
                    default -> throw new IllegalStateException("Unexpected compacted index: " + i);
                };
                invTag.putInt(key, Math.floorDiv(clamped, COMPACTED_MULTIPLIERS[i]));
            }
        }

        if (compactedChanged || extraChanged) {
            VaultPouchCompatHelper.markContentsChanged(pouch);
        }
    }
}