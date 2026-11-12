package xyz.iwolfking.woldsvaults.mixins.vaulthunters.block;

import iskallia.vault.block.entity.VaultChestTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.loot.VaultChestSlots;

/**
 * Grows a chest that saved a {@code LootTable} tag to {@link VaultChestSlots#IN_VAULT} slots, ahead
 * of the {@code super.load} that allocates the item list, and never below the highest saved slot.
 */
@Mixin(VaultChestTileEntity.class)
public class MixinVaultChestTileEntitySlots {
    @Shadow(remap = false)
    private int size;

    @Inject(method = "load", at = @At("HEAD"))
    private void woldsvaults$sizeVaultChest(CompoundTag nbt, CallbackInfo ci) {
        if (woldsvaults$isVaultChest(nbt)) {
            this.size = Math.max(this.size, VaultChestSlots.IN_VAULT);
        }
        this.size = Math.max(this.size, woldsvaults$highestSavedSlot(nbt) + 1);
    }

    @Unique
    private static boolean woldsvaults$isVaultChest(CompoundTag nbt) {
        return nbt.contains("LootTable", Tag.TAG_STRING) && !nbt.getString("LootTable").isBlank();
    }

    @Unique
    private static int woldsvaults$highestSavedSlot(CompoundTag nbt) {
        ListTag items = nbt.getList("Items", Tag.TAG_COMPOUND);
        int highest = -1;
        for (int i = 0; i < items.size(); i++) {
            highest = Math.max(highest, items.getCompound(i).getByte("Slot") & 0xFF);
        }
        return highest;
    }
}
