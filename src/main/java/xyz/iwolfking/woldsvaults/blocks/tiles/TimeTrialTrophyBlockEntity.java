package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.UUID;

public class TimeTrialTrophyBlockEntity extends BlockEntity {

    private TrophyData data;

    public TimeTrialTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TIME_TRIAL_TROPHY_BLOCK_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public void readFromItem(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("TrophyData")) {
            this.data = TrophyData.fromNbt(stack.getTag().getCompound("TrophyData"));
            setChanged();
        }
    }

    public TrophyData getData() {
        return data;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (data != null) {
            tag.put("TrophyData", data.toNbt());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TrophyData")) {
            this.data = TrophyData.fromNbt(tag.getCompound("TrophyData"));
        }
    }

    public record TrophyData(
            String owner,
            UUID ownerUuid,
            String objective,
            String endDate,
            long bestTimeTicks
    ) {

        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Owner", owner);
            tag.putUUID("OwnerUUID", ownerUuid);
            tag.putString("Objective", objective);
            tag.putString("EndDate", endDate);
            tag.putLong("BestTimeTicks", bestTimeTicks);
            return tag;
        }

        public static TrophyData fromNbt(CompoundTag tag) {
            return new TrophyData(
                    tag.getString("Owner"),
                    tag.getUUID("OwnerUUID"),
                    tag.getString("Objective"),
                    tag.getString("EndDate"),
                    tag.getLong("BestTimeTicks")
            );
        }
    }

}
