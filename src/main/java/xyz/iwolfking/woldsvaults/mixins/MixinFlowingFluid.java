package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.fluid.VoidFluid;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

/**
 * Hazard pools in Hyper vaults (Volcanic lava, Void Pools void liquid) are bare source blocks
 * with nothing keeping them in place: any neighbor update — an explosion hole, a broken or
 * pushed block — schedules a fluid tick and the pool pours out. Cancelling the scheduled tick
 * for those two fluids inside a Hyper vault means the pools can never flow, while every other
 * fluid property (pass-through, damage, fog, bucket pickup) stays untouched. Other fluids,
 * vaults and dimensions are unaffected.
 */
@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid {
    @Unique
    private static int woldsVaults$hyperPoolFreezeCount = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$freezeHyperPoolFluids(Level level, BlockPos pos, FluidState state, CallbackInfo ci) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (!state.is(FluidTags.LAVA) && !((Object) this instanceof VoidFluid)) {
            return;
        }
        if (ServerVaults.get(level).flatMap(HyperVaultObjective::get).isEmpty()) {
            return;
        }
        ci.cancel();
        woldsVaults$hyperPoolFreezeCount++;
        if (woldsVaults$hyperPoolFreezeCount == 1 || woldsVaults$hyperPoolFreezeCount % 200 == 0) {
            WoldsVaults.LOGGER.info("Froze a hazard-pool fluid tick at {} in a Hyper vault ({} frozen so far).",
                    pos, woldsVaults$hyperPoolFreezeCount);
        }
    }
}
