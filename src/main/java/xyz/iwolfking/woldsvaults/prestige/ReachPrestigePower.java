package xyz.iwolfking.woldsvaults.prestige;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class ReachPrestigePower extends PrestigePower {
    private float reachIncrease;

    public ReachPrestigePower() {
    }

    public ReachPrestigePower(float reachIncrease) {
        this.reachIncrease = reachIncrease;
    }

    public float getReachIncrease() {
        return reachIncrease;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.reachIncrease).ifPresent((tag) -> nbt.put("reachIncrease", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.reachIncrease = Adapters.FLOAT.readNbt(nbt.get("reachIncrease")).orElse(0.5F);
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.reachIncrease).ifPresent((e) -> json.add("reachIncrease", e));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.reachIncrease = Adapters.FLOAT.readJson(json.get("reachIncrease")).orElse(0.5F);
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.reachIncrease, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.reachIncrease = Adapters.FLOAT.readBits(buffer).orElse(0.5F);
    }
}
