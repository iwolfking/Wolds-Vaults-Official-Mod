package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.ability.effect.spi.AbstractVeinMinerAbility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import xyz.iwolfking.woldsvaults.util.ducks.DuckGetRange;

import java.util.Optional;

public class VeinMinerChainAbility extends AbstractVeinMinerAbility implements DuckGetRange {
    public VeinMinerChainAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, int blockLimit, int range) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, blockLimit);
        this.range = range;
    }

    public VeinMinerChainAbility() {
    }

    private int range;

    @Override
    public int getRange() {
        return this.range;
    }

    @Override
    protected ItemStack getVeinMiningItemProxy(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).copy();
    }

    @Override
    public boolean shouldVoid(ServerLevel level, ServerPlayer player, BlockState target) {
        return false;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT_SEGMENTED_7.writeBits(Integer.valueOf(this.range), buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.range = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT.writeNbt(Integer.valueOf(this.range)).ifPresent(tag -> nbt.put("range", tag));
            return (CompoundTag)nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.range = Adapters.INT.readNbt(nbt.get("range")).orElse(1);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT.writeJson(Integer.valueOf(this.range)).ifPresent(element -> json.add("range", element));
            return (JsonObject)json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.range = Adapters.INT.readJson(json.get("range")).orElse(1);
    }
}