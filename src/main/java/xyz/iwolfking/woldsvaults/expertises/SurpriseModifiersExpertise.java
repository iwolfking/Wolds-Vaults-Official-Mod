package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class SurpriseModifiersExpertise extends LearnableSkill {
    private int expertiseLevel;

    public SurpriseModifiersExpertise() {
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT.writeBits(this.expertiseLevel, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.expertiseLevel = Adapters.INT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT.writeNbt(this.expertiseLevel).ifPresent(tag -> nbt.put("surpriseLevel", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.expertiseLevel = Adapters.INT.readNbt(nbt.get("surpriseLevel")).orElseThrow();
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT.writeJson(this.expertiseLevel).ifPresent(element -> json.add("surpriseLevel", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.expertiseLevel = Adapters.INT.readJson(json.get("surpriseLevel")).orElseThrow();
    }

    public int getExpertiseLevel() {
        return this.expertiseLevel;
    }
}
