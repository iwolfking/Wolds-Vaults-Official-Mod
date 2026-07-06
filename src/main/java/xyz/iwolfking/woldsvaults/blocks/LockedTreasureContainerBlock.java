package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.block.TreasureContainerBlock;
import iskallia.vault.block.TreasureDoorBlock;
import iskallia.vault.block.entity.TreasureContainerTileEntity;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.ItemVaultKeyring;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LockedTreasureContainerBlock extends TreasureContainerBlock {
    public static final EnumProperty<TreasureDoorBlock.Type> TYPE = EnumProperty.create("type", TreasureDoorBlock.Type.class);
    public static final BooleanProperty UNLOCKED = BooleanProperty.create("unlocked");

    public LockedTreasureContainerBlock() {
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(UNLOCKED, false));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
        builder.add(UNLOCKED);
    }

    // TODO: colored particles
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TreasureContainerTileEntity treasureContainer) {
            if (!treasureContainer.isGenerated() && this.consumeKey(player, hand, state.getValue(TYPE))) {
                treasureContainer.generateLoot(player);
                level.setBlock(pos, state.setValue(UNLOCKED, true), Block.UPDATE_ALL);

            }
            if (!treasureContainer.isGenerated()) {
                player.displayClientMessage(new TextComponent("This treasure chest requires ").append(state.getValue(TYPE).getKey().getDescription()).append(" to open"), true);
                return InteractionResult.FAIL;
            }

            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openGui(serverPlayer, treasureContainer, pos);
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public boolean consumeKey(Player player, InteractionHand hand, TreasureDoorBlock.Type doorType) {
        ItemStack heldStack = player.getItemInHand(hand);
        Item requiredKey = doorType.getKey();
        if (heldStack.is(requiredKey)) {
            if (!player.isCreative()) {
                heldStack.shrink(1);
            }

            return true;
        }
        if (heldStack.is(ModItems.TREAUSURE_KEYRING)) {
            List<OverSizedItemStack> stored = ItemVaultKeyring.getStoredStacks(heldStack);
            OverSizedItemStack keyStack = stored.stream().filter((stack) -> stack.stack().is(requiredKey)).findFirst().orElse(OverSizedItemStack.EMPTY);
            if (!keyStack.isEmpty()) {
                if (!player.isCreative()) {
                    int newAmount = keyStack.amount() - 1;
                    stored.remove(keyStack);
                    if (newAmount > 0) {
                        stored.add(OverSizedItemStack.of(new ItemStack(requiredKey, newAmount)));
                    }

                    ItemVaultKeyring.setStoredStacks(heldStack, stored);
                }

                return true;
            }
        }

        return false;
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        CompoundTag nbt = context.getItemInHand().getTag();
        if (nbt != null) {
            TreasureDoorBlock.Type type = TreasureDoorBlock.Type.fromString(nbt.getString("type"));
            if (type != null) {
                state = state.setValue(TYPE, type);
            }
        }

        return state;
    }

}
