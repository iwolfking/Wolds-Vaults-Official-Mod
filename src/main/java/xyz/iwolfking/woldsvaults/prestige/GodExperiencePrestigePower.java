package xyz.iwolfking.woldsvaults.prestige;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/** God's Disciple: a flat percentage on every god alignment XP award, summed across copies. */
public class GodExperiencePrestigePower extends PrestigePower {
    private float experienceIncrease;

    public GodExperiencePrestigePower() {
    }

    public GodExperiencePrestigePower(float experienceIncrease) {
        this.experienceIncrease = experienceIncrease;
    }

    public float getExperienceIncrease() {
        return this.experienceIncrease;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.experienceIncrease).ifPresent((tag) -> nbt.put("experienceIncrease", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.experienceIncrease = Adapters.FLOAT.readNbt(nbt.get("experienceIncrease")).orElse(0.0F);
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.experienceIncrease).ifPresent((element) -> json.add("experienceIncrease", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.experienceIncrease = Adapters.FLOAT.readJson(json.get("experienceIncrease")).orElse(0.0F);
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.experienceIncrease, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.experienceIncrease = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }
}
