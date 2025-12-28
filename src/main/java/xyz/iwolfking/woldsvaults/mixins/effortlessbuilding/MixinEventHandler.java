package xyz.iwolfking.woldsvaults.mixins.effortlessbuilding;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.base.InventoryRetainerBlock;
import iskallia.vault.block.entity.base.InventoryRetainerTileEntity;
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
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.helper.SurvivalHelper;
import nl.requios.effortlessbuilding.network.BlockPlacedMessage;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EventHandler.class, remap = false)
public abstract class MixinEventHandler {

    @Inject(method = "onBlockPlaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void preventPlacingRetainerBlocks(BlockEvent.EntityPlaceEvent event, CallbackInfo ci) {
        if(event.getPlacedBlock().getBlock() instanceof InventoryRetainerBlock<?>) {
            ci.cancel();
            return;
        }
    }
}
