package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.block.base.LootableTileEntity;
import iskallia.vault.block.entity.base.TemplateTagContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IskallianLeavesTileEntity extends LootableTileEntity implements TemplateTagContainer {
    private static final Random rand = new Random();
    private final List<String> templateTags = new ArrayList<>();

    public IskallianLeavesTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ISKALLIAN_LEAVES_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IskallianLeavesTileEntity tile) {
        if (level.isClientSide()) {
            clientTick(level, pos, state, tile);
        }

    }

    @OnlyIn(Dist.CLIENT)
    private static void clientTick(Level level, BlockPos pos, BlockState state, IskallianLeavesTileEntity tile) {
        if (rand.nextInt(14) == 0) {
            boolean hasEmptyBlockAround = false;

            for(Direction dir : Direction.values()) {
                BlockPos offsetPos = pos.relative(dir);
                if (level.isEmptyBlock(offsetPos)) {
                    hasEmptyBlockAround = true;
                    break;
                }
            }

            if (hasEmptyBlockAround) {
                ParticleEngine engine = Minecraft.getInstance().particleEngine;
                float r = 0;
                float g = 128;
                float b = 0;
                SimpleAnimatedParticle particle = (SimpleAnimatedParticle)engine.createParticle(ParticleTypes.FIREWORK.getType(), pos.getX() + 0.5 + rand.nextFloat() * (rand.nextBoolean() ? 1 : -1), pos.getY() + 0.5 + rand.nextFloat() * (rand.nextBoolean() ? 1 : -1), pos.getZ() + 0.5 + rand.nextFloat() * (rand.nextBoolean() ? 1 : -1), 0.0, rand.nextFloat() * 0.01F, 0.0);
                if (particle != null) {
                    particle.setColor(r, g, b);
                }
            }
        }
    }

    public List<String> getTemplateTags() {
        return Collections.unmodifiableList(this.templateTags);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.templateTags.clear();
        this.templateTags.addAll(this.loadTemplateTags(nbt));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.saveTemplateTags(nbt);
    }
}
