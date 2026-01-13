package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.block.entity.VaultCrateTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

@Mixin(value = VaultCrateBlock.class, remap = false)
public abstract class MixinVaultCrateBlock {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"), cancellable = true, remap = true)
    private void preventDestroyingCrateWhenEmpty(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if(world.getBlockEntity(pos) instanceof VaultCrateTileEntity vaultCrateTileEntity) {
            if(vaultCrateTileEntity.getItems().isEmpty()) {
                cir.setReturnValue(InteractionResult.CONSUME);
            }
        }
    }

    @Inject(method = "getCrateBlock", at = @At("HEAD"), cancellable = true)
    private static void handleAdditionalCrates(VaultCrateBlock.Type type, CallbackInfoReturnable<Block> cir) {
        if(ModBlocks.CUSTOM_VAULT_CRATES.containsKey(type.name().toLowerCase())) {
            cir.setReturnValue(ModBlocks.getCrateFor(type.name()));
        }
        else if(type.equals(VaultCrateBlock.Type.valueOf("CORRUPTED"))) {
            cir.setReturnValue(ModBlocks.getCrateFor("corrupt"));
        }
    }

}