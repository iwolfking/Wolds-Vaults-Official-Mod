package xyz.iwolfking.woldsvaults.mixins.openpac;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.VaultPortalBlock;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.pac.common.event.CommonEvents;

import java.util.stream.Stream;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "openpartiesandclaims")
        }
)
@Mixin(value = CommonEvents.class, remap = false)
public class MixinCommonEvents {
    @Inject(method = "onEntityPlaceBlock", at = @At(value = "HEAD"), cancellable = true)
    private void allowPlacingVaultPortalsSingle(LevelAccessor levelAccessor, BlockPos pos, Entity entity, BlockState placedBlock, BlockState replacedBlock, CallbackInfoReturnable<Boolean> cir) {
        if(placedBlock.getBlock() instanceof VaultPortalBlock) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onEntityMultiPlaceBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"), cancellable = true, remap = true)
    private void allowPlacingVaultPortalsMulti(LevelAccessor levelAccessor, Stream<Pair<BlockPos, BlockState>> blocks, Entity entity, CallbackInfoReturnable<Boolean> cir, @Local BlockState state) {
        if(state.getBlock() instanceof VaultPortalBlock) {
            cir.setReturnValue(false);
        }
    }
}
