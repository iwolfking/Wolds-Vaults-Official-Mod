package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.block.MobBarrier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionHelper {

    public static BlockHitResult specialClip(Level cLevel, ClipContext pContext) {
        return BlockGetter.traverseBlocks(pContext.getFrom(), pContext.getTo(), pContext, (clipContext, blockPos) -> {
            BlockState blockstate = cLevel.getBlockState(blockPos);


            FluidState fluidstate = cLevel.getFluidState(blockPos);
            Vec3 vec3 = clipContext.getFrom();

            //TODO: Make this more extensible
            if(blockstate.getBlock() instanceof MobBarrier) {
                return new BlockHitResult(vec3, Direction.getNearest(vec3.x, vec3.y, vec3.z), blockPos, false);
            }

            Vec3 vec31 = clipContext.getTo();
            VoxelShape voxelshape = clipContext.getBlockShape(blockstate, cLevel, blockPos);
            BlockHitResult blockhitresult = cLevel.clipWithInteractionOverride(vec3, vec31, blockPos, voxelshape, blockstate);
            VoxelShape voxelshape1 = clipContext.getFluidShape(fluidstate, cLevel, blockPos);
            BlockHitResult blockhitresult1 = voxelshape1.clip(vec3, vec31, blockPos);
            double d0 = blockhitresult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockhitresult.getLocation());
            double d1 = blockhitresult1 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockhitresult1.getLocation());
            return d0 <= d1 ? blockhitresult : blockhitresult1;
        }, (clipContext) -> {
            Vec3 vec3 = clipContext.getFrom().subtract(clipContext.getTo());
            return BlockHitResult.miss(clipContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos(clipContext.getTo()));
        });
    }
}
