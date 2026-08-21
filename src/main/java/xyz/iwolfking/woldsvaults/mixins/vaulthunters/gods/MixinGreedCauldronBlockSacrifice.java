package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.block.GreedCauldronBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;

@Mixin(value = GreedCauldronBlock.class, remap = false)
public abstract class MixinGreedCauldronBlockSacrifice {

    /**
     * @author PoorMansPhysicist
     * @reason the Greed Cauldron is now the god sacrificial altar: right-click opens the
     * sacrifice menu instead of the retired hand-in demand flow. Cancelling at HEAD replaces the
     * old interaction wholesale.
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsVaults$openSacrificeMenu(BlockState state, Level level, BlockPos pos, Player player,
                                               InteractionHand hand, BlockHitResult hit,
                                               CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            SacrificeAltarLogic.openMenu(serverPlayer);
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
