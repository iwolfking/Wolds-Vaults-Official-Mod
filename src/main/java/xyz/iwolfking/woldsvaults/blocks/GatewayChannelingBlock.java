package xyz.iwolfking.woldsvaults.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import shadows.gateways.entity.GatewayEntity;
import shadows.gateways.item.GatePearlItem;
import xyz.iwolfking.woldsvaults.api.helper.GTEHelper;

import static shadows.gateways.item.GatePearlItem.getGate;

public class GatewayChannelingBlock extends Block {

    public static final BooleanProperty USED = BooleanProperty.create("used");

    public GatewayChannelingBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(USED, false));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(pState.getValue(USED)) {
            return InteractionResult.FAIL;
        }

        if(!(pPlayer.getItemInHand(pHand).getItem() instanceof GatePearlItem)) {
            return attemptSpawnGateway(pLevel, pPlayer.getItemInHand(pHand), pPos, pPlayer, true);
        }

        return attemptSpawnGateway(pLevel, pPlayer.getItemInHand(pHand), pPos, pPlayer);
    }

    private InteractionResult attemptSpawnGateway(Level world, ItemStack stack, BlockPos pos, Player player) {
        return attemptSpawnGateway(world, stack, pos, player, false);
    }

    private InteractionResult attemptSpawnGateway(Level world, ItemStack stack, BlockPos pos, Player player, boolean randomGateway) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (!world.getEntitiesOfClass(GatewayEntity.class, (new AABB(pos)).inflate(25.0, 25.0, 25.0)).isEmpty()) {
            return InteractionResult.FAIL;
        } else {
            GatewayEntity entity = null;

            if(randomGateway) {
                player.sendMessage(new TextComponent("Opening random gateway...").withStyle(ChatFormatting.LIGHT_PURPLE), Util.NIL_UUID);
                entity = new GatewayEntity(world, player, GTEHelper.getRandomGate());
            }
            else {
                entity = new GatewayEntity(world, player, getGate(stack));
            }

            if(entity == null) {
                return InteractionResult.FAIL;
            }

            BlockState state = world.getBlockState(pos);
            entity.setPos((double)pos.getX() + 0.5, (double)pos.getY() + state.getShape(world, pos).max(Direction.Axis.Y), (double)pos.getZ() + 0.5);
            int y = 0;

            while(y++ < 4 && !world.noCollision(entity)) {
                entity.setPos(entity.getX(), entity.getY() + 1.0, entity.getZ());
            }

            if (!world.noCollision(entity)) {
                player.sendMessage((new TranslatableComponent("error.gateways.no_space")).withStyle(ChatFormatting.RED), Util.NIL_UUID);
                return InteractionResult.FAIL;
            } else {
                world.addFreshEntity(entity);
                entity.onGateCreated();
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                world.setBlockAndUpdate(pos, state.setValue(USED, true));
                return InteractionResult.CONSUME;
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USED);
    }
}
