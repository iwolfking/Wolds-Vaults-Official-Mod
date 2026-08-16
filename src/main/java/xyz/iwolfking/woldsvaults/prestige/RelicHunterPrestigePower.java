package xyz.iwolfking.woldsvaults.prestige;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.prestige.core.PrestigePower;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/**
 * Relic Hunter: a multiplicative bonus on the ancient unique identify chance, applied additively
 * across owned copies. Both shipped ranks carry 0.5, so the design sheet's Champion 3 result is
 * 0.8% x (1 + 0.5 + 0.5) = 1.6%. Consumed by
 * {@link xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper#getRelicHunterMultiplier}.
 */
public class RelicHunterPrestigePower extends PrestigePower {
    private float chanceMultiplier;

    public RelicHunterPrestigePower() {
    }

    public RelicHunterPrestigePower(float chanceMultiplier) {
        this.chanceMultiplier = chanceMultiplier;
    }

    public float getChanceMultiplier() {
        return this.chanceMultiplier;
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.chanceMultiplier).ifPresent((tag) -> nbt.put("chanceMultiplier", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.chanceMultiplier = Adapters.FLOAT.readNbt(nbt.get("chanceMultiplier")).orElse(0.0F);
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.chanceMultiplier).ifPresent((element) -> json.add("chanceMultiplier", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.chanceMultiplier = Adapters.FLOAT.readJson(json.get("chanceMultiplier")).orElse(0.0F);
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.chanceMultiplier, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.chanceMultiplier = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }
}
