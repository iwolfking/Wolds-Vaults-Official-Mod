package xyz.iwolfking.woldsvaults.blocks.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEvent;
import xyz.iwolfking.woldsvaults.api.core.vault_events.VaultEventSystem;
import xyz.iwolfking.woldsvaults.api.core.vault_events.lib.EventTag;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;
import java.util.Set;

public class VaultEventActivatorTileEntity extends BlockEntity {

    private ResourceLocation eventId;
    private int cooldownTicks;

    public VaultEventActivatorTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VAULT_EVENT_ACTIVATOR_TILE_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
        if(eventId == null) {
            resolveEvent();
        }
    }

    public boolean isOnCooldown() {
        return cooldownTicks > 0;
    }

    public void startCooldown(int ticks) {
        this.cooldownTicks = ticks;
        setChanged();
        sync();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VaultEventActivatorTileEntity blockEntity) {
        if (blockEntity.cooldownTicks > 0) {
            blockEntity.cooldownTicks--;
            if (blockEntity.cooldownTicks % 20 == 0) {
                blockEntity.setChanged();
                blockEntity.sync();
            }
        }

    }

    public void resolveEvent() {
        if (eventId != null) {
            VaultEventSystem.getEventById(eventId);
            return;
        }

        VaultEvent event = VaultEventSystem.getRandomEventByTags(Set.of(EventTag.SURVIVAL));

        if(event != null) {
            setEventId(event.getId());
        }

    }


    public void setEventId(ResourceLocation id) {
        this.eventId = id;
        setChanged();
    }


    public VaultEvent getEvent() {
        return VaultEventSystem.getEventById(eventId);
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
                             ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }




    public void sync() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    3
            );
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (eventId != null) {
            tag.putString("EventId", eventId.toString());
        }
        tag.putInt("Cooldown", cooldownTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("EventId")) {
            eventId = ResourceLocation.parse(tag.getString("EventId"));
        }
        cooldownTicks = tag.getInt("Cooldown");
    }

}
