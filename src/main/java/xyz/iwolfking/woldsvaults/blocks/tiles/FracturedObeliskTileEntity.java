package xyz.iwolfking.woldsvaults.blocks.tiles;

import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.events.vaultevents.WoldCommonEvents;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.HashMap;
import java.util.Map;


public class FracturedObeliskTileEntity extends BlockEntity {
    private boolean generated;
    private Map<ResourceLocation, Integer> modifiers = new HashMap<>();


    public FracturedObeliskTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FRACTURED_OBELISK_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public boolean isGenerated() {
        return this.generated;
    }

    public Map<ResourceLocation, Integer> getModifiers() {
        return this.modifiers;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
        this.sendUpdates();
    }

    public void addModifier(VaultModifier<?> modifier) {
        this.modifiers.put(modifier.getId(), this.modifiers.getOrDefault(modifier.getId(), 0) + 1);
        this.sendUpdates();
    }

    public void removeModifiers() {
        this.modifiers.clear();
        this.sendUpdates();
    }

    public void sendUpdates() {
        if(this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        this.setChanged();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        var tag = pkt.getTag();
        if (tag != null)
        {
            handleUpdateTag(tag);

            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.generated = nbt.getBoolean("Generated");
        this.modifiers = new HashMap<>();

        CompoundTag modifiersNBT = nbt.getCompound("Modifiers");
        modifiersNBT.getAllKeys().forEach(key -> {
            ResourceLocation id = new ResourceLocation(key);
            int count = modifiersNBT.getInt(key);
            this.modifiers.put(id, count);
        });
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("Generated", this.generated);
        CompoundTag modifiersNBT = new CompoundTag();
        this.modifiers.forEach((id, count) -> modifiersNBT.putInt(id.toString(), count));
        nbt.put("Modifiers", modifiersNBT);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FracturedObeliskTileEntity tile) {
        WoldCommonEvents.FRACTURED_OBELISK_UPDATE.invoke(level, state, pos, tile);

        if(level.isClientSide()) {
            tile.playEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEffects() {
        if(this.getLevel() == null) return;

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();

//        if (state.getValue(MonolithBlock.STATE) == MonolithBlock.State.LIT) {
//            Random random = this.getLevel().getRandom();
//
//            if (random.nextInt(5) == 0) {
//                Vec3 offset = new Vec3(random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0, random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1));
//                this.getLevel().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, (double) pos.getX() + 0.5D + offset.x, (double) pos.getY() + random.nextDouble() * 0.15f + 0.55f, (double) pos.getZ() + 0.5D + offset.z, offset.x / 120f, random.nextDouble() * -0.005D + 0.075D, offset.z / 120f);
//            }
//
//            if (random.nextInt(2) == 0) {
//                Vec3 offset = new Vec3(random.nextDouble() / 9.0D * (double) (random.nextBoolean() ? 1 : -1), 0, random.nextDouble() / 9.0D * (double) (random.nextBoolean() ? 1 : -1));
//                this.getLevel().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, (double) pos.getX() + 0.5D + offset.x, (double) pos.getY() + 1.55f + random.nextDouble() * 0.15f, (double) pos.getZ() + 0.5D + offset.z, offset.x / 120f, random.nextDouble() * -0.005D + 0.075D, offset.z / 120f);
//            }
//
//            if (random.nextInt(3 * 5) == 0) {
//                Vec3 offset = new Vec3(random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0, random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1));
//                this.getLevel().addParticle(ParticleTypes.LAVA, true, (double) pos.getX() + 0.5D + offset.x, (double) pos.getY() + random.nextDouble() * 0.15f + 0.55f, (double) pos.getZ() + 0.5D + offset.z, offset.x / 12f, random.nextDouble() * 0.1D + 0.1D, offset.z / 12f);
//            }
//        }

    }
}