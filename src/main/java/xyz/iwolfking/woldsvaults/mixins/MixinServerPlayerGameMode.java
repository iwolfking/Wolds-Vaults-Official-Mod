package xyz.iwolfking.woldsvaults.mixins;

import com.bawnorton.mixinsquared.TargetHandler;
import iskallia.vault.block.RunePillarBlock;
import iskallia.vault.block.ShopPedestalBlock;
import iskallia.vault.block.VaultCrateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayerGameMode.class)
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
        cir.setReturnValue(state.getBlock() instanceof VaultCrateBlock || state.getBlock() instanceof ShopPedestalBlock || state.getBlock() instanceof RunePillarBlock);
    }
}
