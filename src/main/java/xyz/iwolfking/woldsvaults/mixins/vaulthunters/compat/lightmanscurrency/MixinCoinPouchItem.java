package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.gui.screen.CoinPouchScreen;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

@Mixin(value = CoinPouchItem.class, remap = false)
public class MixinCoinPouchItem {


    @Invoker("contentsOf")
    static CompoundTag woldsvaults$contentsOf(ItemStack pouch) {
        throw new AssertionError();
    }

    @Invoker("contentsChanged")
    static void woldsvaults$contentsChanged(ItemStack pouch) {
        throw new AssertionError();
    }

    @Inject(method = "getPouchSlotIndex", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$getExtraPouchSlotIndex(Item item, CallbackInfoReturnable<Integer> cir) {
        if (item == ModBlocks.VAULT_PALLADIUM_PILE.asItem()) {
            cir.setReturnValue(8);
        } else if (item == ModBlocks.VAULT_IRIDIUM_PILE.asItem()) {
            cir.setReturnValue(9);
        }
    }

    @ModifyReturnValue(method = "getContainedStacks", at = @At("RETURN"))
    private static ItemStack[] woldsvaults$appendExtraContainedStacks(ItemStack[] original, @Local(argsOnly = true) ItemStack pouch) {
        CompoundTag invTag = woldsvaults$contentsOf(pouch);
        int palladium = invTag == null ? 0 : invTag.getInt("PalladiumStackSize");
        int iridium = invTag == null ? 0 : invTag.getInt("IridiumStackSize");

        ItemStack[] expanded = new ItemStack[10];
        System.arraycopy(original, 0, expanded, 0, Math.min(original.length, 8));
        expanded[8] = new ItemStack(ModBlocks.VAULT_PALLADIUM_PILE, palladium);
        expanded[9] = new ItemStack(ModBlocks.VAULT_IRIDIUM_PILE, iridium);
        return expanded;
    }

    @ModifyReturnValue(method = "slotLimit", at = @At("RETURN"))
    private static int woldsvaults$extraSlotLimit(int original, @Local(argsOnly = true) int slot) {
        if (slot == 8 || slot == 9) {
            return 2147483582;
        }
        return original;
    }

    @Inject(method = "writeContainedCount", at = @At("HEAD"), cancellable = true)
    private static void woldsvaults$writeExtraContainedCount(CompoundTag invTag, int slot, int count, CallbackInfo ci) {
        if (slot == 8) {
            invTag.putInt("PalladiumStackSize", count);
            ci.cancel();
        } else if (slot == 9) {
            invTag.putInt("IridiumStackSize", count);
            ci.cancel();
        }
    }

    @Unique
    private static final int PALLADIUM_MULT = 6561;
    @Unique
    private static final int IRIDIUM_MULT = 59049;

    @Inject(method = "setCompactedCount", at = @At("TAIL"))
    private static void woldsvaults$recomputeHigherTierCompaction(
            CompoundTag invTag,
            String[] countKeys,
            int[] multipliers,
            int sizeIndex,
            int count,
            CallbackInfo ci
    ) {
        long bronze = invTag.getInt("BronzeStackSize");
        long palladium = invTag.getInt("PalladiumStackSize");
        long iridium = invTag.getInt("IridiumStackSize");

        if (sizeIndex > 0 && sizeIndex < multipliers.length) {
            for (int i = 1; i < multipliers.length; i++) {
                invTag.putInt(countKeys[i], Math.floorDiv((int) bronze, multipliers[i]));
            }
        }

        invTag.putInt("PalladiumStackSize", (int) Math.floorDiv(palladium * PALLADIUM_MULT + iridium * IRIDIUM_MULT, PALLADIUM_MULT));
        invTag.putInt("IridiumStackSize", (int) Math.floorDiv(iridium * IRIDIUM_MULT, IRIDIUM_MULT));
    }
}