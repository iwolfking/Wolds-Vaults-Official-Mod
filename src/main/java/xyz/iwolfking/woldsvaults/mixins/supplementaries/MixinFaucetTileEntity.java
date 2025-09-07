package xyz.iwolfking.woldsvaults.mixins.supplementaries;

import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "supplementaries"),
                @Condition(type = Condition.Type.MOD, value = "modularrouters")
        }
)
@Mixin(value = FaucetBlockTile.class, remap = false)
public abstract class MixinFaucetTileEntity {
    private static final List<Item> ROUTER_ALLOWED_FLUIDS = List.of(Items.WATER_BUCKET, Items.LAVA_BUCKET);
    /**
     * @author iwolfking
     * @reason Patch Supplementaries bug with Modular Routers (excluding water and lava)
     */
    @Inject(method = "tryExtract", at = @At("HEAD"), cancellable = true)
    private void tryExtract(Level level, BlockPos pos, BlockState state, boolean doTransfer, CallbackInfoReturnable<Boolean> cir) {
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        Block backBlock = backState.getBlock();
        if(backBlock instanceof ModularRouterBlock) {
            if(level.getBlockEntity(behind) instanceof ModularRouterBlockEntity blockEntity) {
                if(!ROUTER_ALLOWED_FLUIDS.contains(blockEntity.getBuffer().getStackInSlot(0).getItem())) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
