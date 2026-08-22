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
 * Grows chests generated inside a vault to {@link VaultChestSlots#IN_VAULT} slots while leaving the
 * same blocks at their vanilla size everywhere else.
 *
 * <p>The container size is fixed in the tile entity constructor from the block state alone, which
 * cannot tell a vault chest from one a player placed in the overworld - the vault marker only
 * arrives with the NBT. This runs ahead of {@code super.load}, the call that allocates the item list
 * from {@code getContainerSize()}, so a chest carrying a loot table is sized before its items are
 * read back. Chests without one keep 45 or 36 slots and therefore keep their five- and four-row
 * screen, which is what stops overworld storage chests from gaining slots a player cannot reach.
 *
 * <p>The marker survives because the tile entity overrides both loot table hooks to call super and
 * then return false, so the {@code LootTable} tag and the item list are written together rather than
 * one instead of the other. The size is also raised to cover the highest saved slot index, so
 * contents survive intact if the configured capacity is ever lowered.
 *
 * <p>Chests spawned straight into the world without NBT, by the conjuration projectile or a block
 * conversion task, are sized on their first reload rather than at placement, so their loot goes into
 * the 45 slots the block state gives them until then.
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
