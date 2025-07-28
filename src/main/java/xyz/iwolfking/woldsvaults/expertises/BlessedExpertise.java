package xyz.iwolfking.woldsvaults.expertises;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.talent.GearAttributeSkill;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.stream.Stream;

public class BlessedExpertise extends LearnableSkill implements GearAttributeSkill {
    private float affinityIncrease;

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.affinityIncrease, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.affinityIncrease = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.FLOAT.writeNbt(this.affinityIncrease).ifPresent((tag) -> nbt.put("affinityIncrease", tag));
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.affinityIncrease = Adapters.FLOAT.readNbt(nbt.get("affinityIncrease")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.FLOAT.writeJson(this.affinityIncrease).ifPresent((element) -> json.add("affinityIncrease", element));
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.affinityIncrease = Adapters.FLOAT.readJson(json.get("affinityIncrease")).orElseThrow();
    }

    @Override
    public Stream<VaultGearAttributeInstance<?>> getGearAttributes(SkillContext skillContext) {
        return Stream.of(
                new VaultGearAttributeInstance<>(ModGearAttributes.IDONA_AFFINITY, affinityIncrease),
                new VaultGearAttributeInstance<>(ModGearAttributes.TENOS_AFFINITY, affinityIncrease),
                new VaultGearAttributeInstance<>(ModGearAttributes.VELARA_AFFINITY, affinityIncrease),
                new VaultGearAttributeInstance<>(ModGearAttributes.WENDARR_AFFINITY, affinityIncrease)
        );
    }
}
