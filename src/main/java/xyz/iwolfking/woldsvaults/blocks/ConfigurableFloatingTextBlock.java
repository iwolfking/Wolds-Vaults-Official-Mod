package xyz.iwolfking.woldsvaults.blocks;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import java.util.Random;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.blocks.containers.FloatingTextEditScreen;
import xyz.iwolfking.woldsvaults.blocks.tiles.ConfigurableFloatingTextTileEntity;

public class ConfigurableFloatingTextBlock extends BarrierBlock implements EntityBlock {

    public ConfigurableFloatingTextBlock() {
        super(Properties.of(Material.BARRIER).strength(10.0F, 3.6E8F).noDrops().noOcclusion().noCollission());
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConfigurableFloatingTextTileEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof ConfigurableFloatingTextTileEntity t && t.isEditable()) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            ClientLevel world = minecraft.level;
            if (player != null && world != null) {
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, stateIn), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ConfigurableFloatingTextTileEntity tile) {
                Minecraft.getInstance().setScreen(
                        new FloatingTextEditScreen(tile)
                );
            }
        }
        return InteractionResult.SUCCESS;
    }



}
