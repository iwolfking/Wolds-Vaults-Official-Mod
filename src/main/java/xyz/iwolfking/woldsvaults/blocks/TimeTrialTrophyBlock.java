package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import xyz.iwolfking.woldsvaults.blocks.tiles.TimeTrialTrophyBlockEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TimeTrialTrophyBlock extends Block implements EntityBlock {

    public TimeTrialTrophyBlock(Properties props) {
        super(props.noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimeTrialTrophyBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TimeTrialTrophyBlockEntity trophy) {
                trophy.readFromItem(stack);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            BlockGetter level,
            BlockPos pos,
            Player player
    ) {
        ItemStack stack = new ItemStack(this);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TimeTrialTrophyBlockEntity trophy && trophy.getData() != null) {
            stack.getOrCreateTag().put("TrophyData", trophy.getData().toNbt());
        }

        return stack;
    }

    @Override
    public void playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        BlockEntity be = level.getBlockEntity(pos);

        if (!level.isClientSide && be instanceof TimeTrialTrophyBlockEntity trophy) {
            ItemStack drop = new ItemStack(this);

            if (trophy.getData() != null) {
                drop.getOrCreateTag().put("TrophyData", trophy.getData().toNbt());
            }

            popResource(level, pos, drop);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(
            BlockState state,
            LootContext.Builder builder
    ) {
        return Collections.emptyList();
    }
}
