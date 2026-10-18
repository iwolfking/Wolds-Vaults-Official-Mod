package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import java.util.EnumMap;
import java.util.Map;

public class SuppressorTileEntity extends BlockEntity {

    public static class Config {
        private boolean enabled;
        private int range;

        public Config(boolean enabled, int range) {
            this.enabled = enabled;
            this.range = range;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getRange() { return range; }
        public void setRange(int range) { this.range = Math.max(1, Math.min(range, 64)); }
    }

    private final Map<SuppressorType, Config> suppressorConfigs = new EnumMap<>(SuppressorType.class);

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            SuppressorType type = getTypeFromIndex(index / 2);
            if (type == null) return 0;
            Config config = suppressorConfigs.get(type);
            boolean isRange = (index % 2 != 0);

            if (isRange) {
                return config.getRange();
            } else {
                return config.isEnabled() ? 1 : 0;
            }
        }

        @Override
        public void set(int index, int value) {
            SuppressorType type = getTypeFromIndex(index / 2);
            if (type == null) return;
            Config config = suppressorConfigs.get(type);
            boolean isRange = (index % 2 != 0);

            if (isRange) {
                config.setRange(value);
            } else {
                config.setEnabled(value != 0);
            }
            setChanged();
        }

        @Override
        public int getCount() {
            return SuppressorType.values().length * 2;
        }
    };

    public SuppressorTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SUPPRESSOR_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
        for (SuppressorType type : SuppressorType.values()) {
            suppressorConfigs.put(type, new Config(type.isDefaultEnabled(), type.getDefaultRange()));
        }
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public Config getConfig(SuppressorType type) {
        return suppressorConfigs.get(type);
    }

    private static SuppressorType getTypeFromIndex(int index) {
        SuppressorType[] values = SuppressorType.values();
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        return null;
    }

    public boolean isActive(SuppressorType type, Vec3 position) {
        Config config = suppressorConfigs.get(type);
        if (config == null || !config.isEnabled()) {
            return false;
        }
        return this.worldPosition.distSqr(new BlockPos(position)) <= (config.getRange() * config.getRange());
    }

    public static boolean isSuppressionActiveAround(Entity entity, SuppressorType type) {
        if (entity == null) return false;
        return isSuppressionActiveAround(entity.level, entity.position(), type);
    }

    public static boolean isSuppressionActiveAround(Level level, Vec3 pos, SuppressorType type) {
        ChunkPos entityChunk = new ChunkPos(new BlockPos(pos));
        int chunkRadius = 4;

        for (int x = entityChunk.x - chunkRadius; x <= entityChunk.x + chunkRadius; x++) {
            for (int z = entityChunk.z - chunkRadius; z <= entityChunk.z + chunkRadius; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk != null) {
                    for (BlockEntity tile : chunk.getBlockEntities().values()) {
                        if (tile instanceof SuppressorTileEntity suppressor && suppressor.getLevel() == level) {
                            if (suppressor.isActive(type, pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (Map.Entry<SuppressorType, Config> entry : suppressorConfigs.entrySet()) {
            CompoundTag configTag = new CompoundTag();
            configTag.putBoolean("Enabled", entry.getValue().isEnabled());
            configTag.putInt("Range", entry.getValue().getRange());
            tag.put(entry.getKey().getSerializedName(), configTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (SuppressorType type : SuppressorType.values()) {
            if (tag.contains(type.getSerializedName())) {
                CompoundTag configTag = tag.getCompound(type.getSerializedName());
                Config config = suppressorConfigs.get(type);
                config.setEnabled(configTag.getBoolean("Enabled"));
                config.setRange(configTag.getInt("Range"));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public enum SuppressorType {
        MAGNET("magnet_suppressor", true, 16),
        BACKPACKS("bag_suppressor", true, 16),
        HAMMER("hammer_suppressor", true, 16),
        ABILITY("ability_suppressor", true, 16),
        ENDER("enderman_suppressor", true, 64);

        private final String serializedName;
        private final boolean defaultEnabled;
        private final int defaultRange;

        SuppressorType(String serializedName, boolean defaultEnabled, int defaultRange) {
            this.serializedName = serializedName;
            this.defaultEnabled = defaultEnabled;
            this.defaultRange = defaultRange;
        }

        public String getSerializedName() {
            return serializedName;
        }

        public boolean isDefaultEnabled() {
            return defaultEnabled;
        }

        public int getDefaultRange() {
            return defaultRange;
        }
    }
}