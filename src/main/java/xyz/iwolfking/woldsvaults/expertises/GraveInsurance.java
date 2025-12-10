package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class GraveInsurance extends LearnableSkill {
    private float costReduction;

    public GraveInsurance(int unlockLevel, int learnPointCost, int regretCost, float costReduction) {
        super(unlockLevel, learnPointCost, regretCost);
        this.costReduction = costReduction;
    }

    public GraveInsurance() {
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.costReduction, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.costReduction = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.costReduction).ifPresent((tag) -> nbt.put("costReduction", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.costReduction = Adapters.FLOAT.readNbt(nbt.get("costReduction")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.costReduction).ifPresent((element) -> json.add("costReduction", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.costReduction = Adapters.FLOAT.readJson(json.get("costReduction")).orElseThrow();
   }

    public float getCostReduction() {
        return costReduction;
    }
}
