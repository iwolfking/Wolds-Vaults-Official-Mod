package xyz.iwolfking.woldsvaults.mixins;

import com.bawnorton.mixinsquared.TargetHandler;
import iskallia.vault.block.*;
import iskallia.vault.block.entity.CompanionHomeTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.blocks.BrewingAltar;
import xyz.iwolfking.woldsvaults.items.CompanionRerollingItem;

@Mixin(value = ServerPlayerGameMode.class, priority = 1500)
public class MixinServerPlayerGameMode {

    @TargetHandler(
            mixin = "iskallia.vault.mixin.MixinServerPlayerGameMode",
            name = "doesSneakBypassUse"
    )
    @Inject(
            method = {"@MixinSquared:Handler"},
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void doesSneakBypassUse(ItemStack instance, LevelReader levelReader, BlockPos blockPos, Player player, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = levelReader.getBlockState(blockPos);
        if(player.getMainHandItem().getItem() instanceof CompanionRerollingItem) {
            cir.setReturnValue(false);
        }

        if(state.getBlock() instanceof RunePillarBlock && player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            cir.setReturnValue(true);
        }
        else if(state.getBlock() instanceof CompanionHomeBlock && player.isShiftKeyDown() && (levelReader.getBlockEntity(blockPos) instanceof CompanionHomeTileEntity entity && !entity.getCompanion().isEmpty() && !(player.getMainHandItem().getItem() instanceof CompanionRerollingItem))) {
            cir.setReturnValue(true);
        }
        else {
            cir.setReturnValue(state.getBlock() instanceof VaultCrateBlock || state.getBlock() instanceof ShopPedestalBlock || state.getBlock() instanceof RoyaleCrateBlock || state.getBlock() instanceof BrewingAltar);
        }
    }
}
