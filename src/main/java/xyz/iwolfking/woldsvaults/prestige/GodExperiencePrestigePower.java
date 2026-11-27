package xyz.iwolfking.woldsvaults.prestige;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/**
 * God's Disciple: a flat percentage on every god alignment XP award, summed across copies, and an
 * optional multiplier on the base experience a god altar pays, multiplied across the copies that
 * carry one. Only the first tier ships an altar multiplier; a power without one leaves the base alone.
 */
public class GodExperiencePrestigePower extends PrestigePower {
    private float experienceIncrease;
    private float altarBaseMultiplier = 1.0F;

    public GodExperiencePrestigePower() {
    }

    public GodExperiencePrestigePower(float experienceIncrease) {
        this.experienceIncrease = experienceIncrease;
    }

    public float getExperienceIncrease() {
        return this.experienceIncrease;
    }

    /** Scales the base god altar award; 1 leaves it alone, and a non-positive config value is ignored. */
    public float getAltarBaseMultiplier() {
        return this.altarBaseMultiplier <= 0.0F ? 1.0F : this.altarBaseMultiplier;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.experienceIncrease).ifPresent((tag) -> nbt.put("experienceIncrease", tag));
            Adapters.FLOAT.writeNbt(this.altarBaseMultiplier).ifPresent((tag) -> nbt.put("altarBaseMultiplier", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.experienceIncrease = Adapters.FLOAT.readNbt(nbt.get("experienceIncrease")).orElse(0.0F);
        this.altarBaseMultiplier = Adapters.FLOAT.readNbt(nbt.get("altarBaseMultiplier")).orElse(1.0F);
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.experienceIncrease).ifPresent((element) -> json.add("experienceIncrease", element));
            Adapters.FLOAT.writeJson(this.altarBaseMultiplier).ifPresent((element) -> json.add("altarBaseMultiplier", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.experienceIncrease = Adapters.FLOAT.readJson(json.get("experienceIncrease")).orElse(0.0F);
        this.altarBaseMultiplier = Adapters.FLOAT.readJson(json.get("altarBaseMultiplier")).orElse(1.0F);
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.experienceIncrease, buffer);
        Adapters.FLOAT.writeBits(this.altarBaseMultiplier, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.experienceIncrease = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.altarBaseMultiplier = Adapters.FLOAT.readBits(buffer).orElse(1.0F);
    }
}
