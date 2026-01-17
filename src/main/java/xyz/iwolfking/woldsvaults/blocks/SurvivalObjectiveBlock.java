package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.util.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventCooldowns;
import xyz.iwolfking.woldsvaults.blocks.tiles.HellishSandTileEntity;
import xyz.iwolfking.woldsvaults.blocks.tiles.VaultEventActivatorTileEntity;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

public class SurvivalObjectiveBlock extends Block implements EntityBlock {
    public SurvivalObjectiveBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return ModBlocks.VAULT_EVENT_ACTIVATOR_TILE_ENTITY_BLOCK_ENTITY_TYPE.create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level world, BlockState state, BlockEntityType<A> type) {
        return world.isClientSide() ? null : BlockHelper.getTicker(type, ModBlocks.VAULT_EVENT_ACTIVATOR_TILE_ENTITY_BLOCK_ENTITY_TYPE, VaultEventActivatorTileEntity::tick);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof VaultEventActivatorTileEntity tile)) return InteractionResult.FAIL;

        if (tile.isOnCooldown()) return InteractionResult.FAIL;

        Vault vault = VaultUtils.getVault(level).orElse(null);
        if (vault == null) return InteractionResult.FAIL;

        VaultEvent event = tile.getEvent();
        if (event == null) return InteractionResult.FAIL;

        event.triggerEvent(() -> pos, serverPlayer, vault);

        tile.startCooldown(VaultEventCooldowns.getCooldown(event));
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {

        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof VaultEventActivatorTileEntity tile)) return;

        if (stack.hasTag() && stack.getTag().contains("EventId")) {
            tile.setEventId(ResourceLocation.parse(
                    stack.getTag().getString("EventId")
            ));
        }
    }


}
