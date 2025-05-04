package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class ShopRerollExpertise extends LearnableSkill {
    private int rerollTimeout;

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT.writeBits(this.rerollTimeout, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.rerollTimeout = Adapters.INT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.INT.writeNbt(this.rerollTimeout).ifPresent((tag) -> nbt.put("rerollTimeout", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.rerollTimeout = Adapters.INT.readNbt(nbt.get("rerollTimeout")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.INT.writeJson(this.rerollTimeout).ifPresent((element) -> json.add("rerollTimeout", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.rerollTimeout = Adapters.INT.readJson(json.get("rerollTimeout")).orElseThrow();
    }

    public int getRerollTimeout() {
        return this.rerollTimeout;
    }
}
