package xyz.iwolfking.woldsvaults.talent.special;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.item.bottle.*;
import iskallia.vault.skill.base.LearnableSkill;
import net.minecraft.nbt.CompoundTag;
import java.util.Optional;

public class DebuffDamageBonusTalent extends LearnableSkill {
    private float damageIncrease;

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.damageIncrease, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.damageIncrease = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.damageIncrease).ifPresent(tag -> nbt.put("damageIncrease", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.damageIncrease = Adapters.FLOAT.readNbt(nbt.get("damageIncrease")).orElseThrow();
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.damageIncrease).ifPresent(element -> json.add("damageIncrease", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.damageIncrease = Adapters.FLOAT.readJson(json.get("damageIncrease")).orElseThrow();
    }

    public float getDamageIncrease() {
        return this.damageIncrease;
    }
}