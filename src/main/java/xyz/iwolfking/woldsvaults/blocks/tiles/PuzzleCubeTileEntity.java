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

public class PuzzleCubeTileEntity extends LootableTileEntity implements TemplateTagContainer {
    private final List<String> templateTags = new ArrayList<>();

    public PuzzleCubeTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ISKALLIAN_LEAVES_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
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
