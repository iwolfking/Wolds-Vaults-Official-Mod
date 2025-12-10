package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class EclecticGearExpertise extends LearnableSkill {
    private float increasedChance;

    public EclecticGearExpertise(int unlockLevel, int learnPointCost, int regretCost, float increasedChance) {
        super(unlockLevel, learnPointCost, regretCost);
        this.increasedChance = increasedChance;
    }

    public EclecticGearExpertise() {
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.increasedChance, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.increasedChance = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.increasedChance).ifPresent((tag) -> nbt.put("increasedChance", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.increasedChance = Adapters.FLOAT.readNbt(nbt.get("increasedChance")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.increasedChance).ifPresent((element) -> json.add("increasedChance", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.increasedChance = Adapters.FLOAT.readJson(json.get("increasedChance")).orElseThrow();
   }

    public float getIncreasedChance() {
        return increasedChance;
    }
}
