package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class DeckMasterExpertise extends LearnableSkill {
    private float chance;
    private String forcedRollId;
    private String poolId;

    public DeckMasterExpertise(int unlockLevel, int learnPointCost, int regretCost, float chance, String forcedRollId, String poolId) {
        super(unlockLevel, learnPointCost, regretCost);
        this.chance = chance;
        this.forcedRollId = forcedRollId;
        this.poolId = poolId;
    }

    public DeckMasterExpertise() {
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.chance, buffer);
        Adapters.UTF_8.writeBits(this.forcedRollId, buffer);
        Adapters.UTF_8.writeBits(this.poolId, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.chance = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.forcedRollId = Adapters.UTF_8.readBits(buffer).orElseThrow();
        this.poolId = Adapters.UTF_8.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.chance).ifPresent((tag) -> nbt.put("chance", tag));
            Adapters.UTF_8.writeNbt(this.forcedRollId).ifPresent((tag) -> nbt.put("forcedRollId", tag));
            Adapters.UTF_8.writeNbt(this.poolId).ifPresent((tag) -> nbt.put("poolId", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.chance = Adapters.FLOAT.readNbt(nbt.get("chance")).orElseThrow();
        this.forcedRollId = Adapters.UTF_8.readNbt(nbt.get("forcedRollId")).orElseThrow();
        this.poolId = Adapters.UTF_8.readNbt(nbt.get("poolId")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.chance).ifPresent((element) -> json.add("chance", element));
            Adapters.UTF_8.writeJson(this.forcedRollId).ifPresent((element) -> json.add("forcedRollId", element));
            Adapters.UTF_8.writeJson(this.poolId).ifPresent((element) -> json.add("poolId", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.chance = Adapters.FLOAT.readJson(json.get("chance")).orElseThrow();
        this.forcedRollId = Adapters.UTF_8.readJson(json.get("forcedRollId")).orElseThrow();
        this.poolId = Adapters.UTF_8.readJson(json.get("poolId")).orElseThrow();
    }

    public float getChance() {
        return this.chance;
    }

    public String getForcedRollId() {
        return this.forcedRollId;
    }

    public String getPoolId() {
        return this.poolId;
    }
}
