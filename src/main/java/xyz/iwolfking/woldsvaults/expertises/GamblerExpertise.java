package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class GamblerExpertise extends LearnableSkill {

    private int additionalChoices;

    public GamblerExpertise() {
    }

    public GamblerExpertise(int unlockLevel, int learnPointCost, int regretCost, int additionalChoices) {
        super(unlockLevel, learnPointCost, regretCost);
        this.additionalChoices = additionalChoices;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT.writeBits(this.additionalChoices, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.additionalChoices = Adapters.INT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT.writeNbt(this.additionalChoices).ifPresent(tag -> nbt.put("additionalChoices", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.additionalChoices = Adapters.INT.readNbt(nbt.get("additionalChoices")).orElseThrow();
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT.writeJson(this.additionalChoices).ifPresent(element -> json.add("additionalChoices", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.additionalChoices = Adapters.INT.readJson(json.get("additionalChoices")).orElseThrow();
    }

    public int getAdditionalChoices() {
        return this.additionalChoices;
    }
}
