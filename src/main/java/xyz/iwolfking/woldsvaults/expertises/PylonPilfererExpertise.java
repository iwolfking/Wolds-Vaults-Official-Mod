package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class PylonPilfererExpertise extends LearnableSkill {
    private float chanceIncrease;

    public PylonPilfererExpertise(int unlockLevel, int learnPointCost, int regretCost, float chanceIncrease) {
        super(unlockLevel, learnPointCost, regretCost);
        this.chanceIncrease = chanceIncrease;
    }

    public PylonPilfererExpertise() {
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.chanceIncrease, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.chanceIncrease = (Float)Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.chanceIncrease).ifPresent((tag) -> nbt.put("chanceIncrease", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.chanceIncrease = (Float)Adapters.FLOAT.readNbt(nbt.get("chanceIncrease")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.chanceIncrease).ifPresent((element) -> json.add("chanceIncrease", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.chanceIncrease = (Float)Adapters.FLOAT.readJson(json.get("chanceIncrease")).orElseThrow();
    }

    public float getChanceIncrease() {
        return this.chanceIncrease;
    }
}
