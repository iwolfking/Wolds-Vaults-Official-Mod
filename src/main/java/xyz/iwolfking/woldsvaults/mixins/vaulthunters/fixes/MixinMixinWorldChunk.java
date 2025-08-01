package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.*;
import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.WorldZone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = LevelChunk.class, priority = 1500)
public abstract class MixinMixinWorldChunk {
    @Shadow public abstract Level getLevel();

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> ci) {
        if (!this.getLevel().isClientSide()) {
            Block block = getLevel().getBlockState(pos).getBlock();
            if(block instanceof TotemBlock || block instanceof VaultChestBlock || block instanceof CoinPileBlock || block instanceof WildSpawnerBlock || block instanceof CustomEntitySpawnerBlock || block instanceof LandmineBlock) {
                return;
            }
            IZonedWorld proxy = IZonedWorld.of(this.getLevel()).orElse(null);
            if (proxy != null) {
                List<WorldZone> zones = proxy.getZones().get(pos);
                if (!zones.isEmpty()) {
                    for (WorldZone zone : zones) {
                        if (zone.canModify() == Boolean.FALSE) {
                            ci.setReturnValue(null);
                            return;
                        }
                    }
                }
            }
        }
    }

}
