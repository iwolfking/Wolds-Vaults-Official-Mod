package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.base.InventoryRetainerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.world.BlockEvent;
import nl.requios.effortlessbuilding.EventHandler;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SurvivalHelper.class, remap = false)
public class MixinEventHandler {
    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private static void preventPlacingRetainerBlocks(Level world, Player player, BlockPos pos, BlockState blockState, ItemStack origstack, Direction facing, Vec3 hitVec, boolean skipPlaceCheck, boolean skipCollisionCheck, boolean playSound, CallbackInfoReturnable<Boolean> cir) {
        if(blockState.getBlock() instanceof InventoryRetainerBlock<?>) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private static void cantPlaceRetainerBlocks(Level world, Player player, BlockPos pos, BlockState newBlockState, ItemStack itemStack, boolean skipCollisionCheck, Direction sidePlacedOn, CallbackInfoReturnable<Boolean> cir) {
        if(newBlockState.getBlock() instanceof InventoryRetainerBlock<?>) {
            cir.setReturnValue(false);
        }
    }
}
