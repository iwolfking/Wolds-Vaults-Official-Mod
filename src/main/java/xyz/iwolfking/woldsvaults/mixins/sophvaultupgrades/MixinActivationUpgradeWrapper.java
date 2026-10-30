package xyz.iwolfking.woldsvaults.mixins.sophvaultupgrades;

import iskallia.vault.block.TreasureDoorBlock;
import iskallia.vault.block.entity.TreasureContainerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.sophisticatedvaultupgrades.upgrades.activation.ActivationUpgradeItem;
import xyz.iwolfking.sophisticatedvaultupgrades.upgrades.activation.ActivationUpgradeWrapper;
import xyz.iwolfking.woldsvaults.blocks.LockedTreasureContainerBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static xyz.iwolfking.woldsvaults.blocks.LockedTreasureContainerBlock.UNLOCKED;

@Mixin(value = ActivationUpgradeWrapper.class, remap = false)
public class MixinActivationUpgradeWrapper extends UpgradeWrapperBase<ActivationUpgradeWrapper, ActivationUpgradeItem> {
    protected MixinActivationUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void lockedTreasureChest(Level world, BlockPos blockPos, Player player, CallbackInfoReturnable<Boolean> cir){
        BlockState blockState = world.getBlockState(blockPos);
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if(blockState.getBlock() instanceof LockedTreasureContainerBlock && blockEntity instanceof TreasureContainerTileEntity treasureContainer) {
            List<ItemStack> itemsToRemove = new ArrayList<>();
            boolean unlocked = blockState.getOptionalValue(UNLOCKED).orElse(false);
            if (!unlocked) {
                storageWrapper.getInventoryForUpgradeProcessing().getTrackedStacks().forEach(itemStackKey -> {
                    if (itemStackKey.getStack().getItem() == blockState.getValue(TreasureDoorBlock.TYPE).getKey()) {
                        itemsToRemove.add(new ItemStack(itemStackKey.getStack().getItem(), 1));
                        world.setBlock(blockPos, blockState.setValue(UNLOCKED, true), Block.UPDATE_ALL);
                    }
                });
                for (ItemStack stack : itemsToRemove) {
                    InventoryHelper.extractFromInventory(stack, storageWrapper.getInventoryForUpgradeProcessing(), false);
                }
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
