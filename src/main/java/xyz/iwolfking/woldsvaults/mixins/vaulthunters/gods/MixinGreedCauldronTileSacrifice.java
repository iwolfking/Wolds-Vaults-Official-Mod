package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.block.entity.GreedCauldronTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.sacrifice.SacrificeAltarLogic;

@Mixin(value = GreedCauldronTileEntity.class, remap = false)
public abstract class MixinGreedCauldronTileSacrifice {

    /**
     * @author PoorMansPhysicist
     * @reason the sacrificial-altar tick replaces the retired demand tick entirely: it vacuums
     * item entities the owner's current sacrifice gate still needs and fires the sacrifice on a
     * rising redstone edge.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$sacrificeTick(Level level, BlockPos pos, BlockState state,
                                                  GreedCauldronTileEntity tile, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            SacrificeAltarLogic.tickCauldron(serverLevel, pos, tile);
        }
        ci.cancel();
    }
}
