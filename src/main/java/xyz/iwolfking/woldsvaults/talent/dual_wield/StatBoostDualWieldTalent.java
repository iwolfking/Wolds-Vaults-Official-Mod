package xyz.iwolfking.woldsvaults.talent.dual_wield;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TickingSkill;
import iskallia.vault.skill.talent.GearAttributeSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class StatBoostDualWieldTalent extends DualWieldTalent implements GearAttributeSkill, TickingSkill {

    private List<VaultGearAttribute<?>> attributes = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private transient boolean lastDualWielding = false;

    public StatBoostDualWieldTalent(int unlockLevel, int learnPointCost, int regretPointCost,
                                    List<VaultGearAttribute<?>> attributes, List<Double> values) {
        super(unlockLevel, learnPointCost, regretPointCost);
        setAttributes(attributes, values);
    }

    public StatBoostDualWieldTalent(int unlockLevel, int learnPointCost, int regretPointCost,
                                    VaultGearAttribute<?> attribute, double value) {
        this(unlockLevel, learnPointCost, regretPointCost, List.of(attribute), List.of(value));
    }

    public StatBoostDualWieldTalent() {
    }

    private void setAttributes(List<VaultGearAttribute<?>> attributes, List<Double> values) {
        this.attributes = attributes == null ? new ArrayList<>() : new ArrayList<>(attributes);
        this.values = values == null ? new ArrayList<>() : new ArrayList<>(values);
        while (this.values.size() < this.attributes.size()) {
            this.values.add(1.0D);
        }
    }

    public List<VaultGearAttribute<?>> getAttributes() {
        return attributes;
    }

    public List<Double> getValues() {
        return values;
    }

    public boolean canApply(SkillContext context) {
        if (!this.isUnlocked()) {
            return false;
        }
        return context.getSource().as(ServerPlayer.class)
                .map(DualWieldTalent::isDualWielding)
                .orElse(false);
    }

    @Override
    public void onAdd(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
    }

    @Override
    public void onRemove(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
    }

    @Override
    public void onTick(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            boolean nowDualWielding = DualWieldTalent.isDualWielding(player);
            if (nowDualWielding != this.lastDualWielding) {
                this.lastDualWielding = nowDualWielding;
                this.refreshSnapshot(player);
            }
        });

        if (!this.isUnlocked()) {
            this.onRemoveModifiers(context);
        } else if (this.canApply(context)) {
            this.onAddModifiers(context);
        } else {
            this.onRemoveModifiers(context);
        }
    }

    @Override
    public Stream<VaultGearAttributeInstance<?>> getGearAttributes(SkillContext context) {
        if (!this.canApply(context)) {
            return Stream.empty();
        }
        return buildInstances();
    }

    @Override
    public void onRemoveModifiers(SkillContext context) {
        context.getSource().as(net.minecraft.world.entity.LivingEntity.class).ifPresent(entity -> {
            net.minecraft.world.entity.ai.attributes.AttributeModifier multimapEntry = null;
            java.util.stream.Stream<VaultGearAttributeInstance<?>> instances = buildInstances();
            VaultGearHelper.getModifiers(this.getUuid(), instances).forEach((attribute, modifier) -> {
                net.minecraft.world.entity.ai.attributes.AttributeInstance present = entity.getAttribute(attribute);
                if (present != null) {
                    present.removeModifier(modifier.getId());
                }
            });
        });
    }

    private Stream<VaultGearAttributeInstance<?>> buildInstances() {
        List<VaultGearAttributeInstance<?>> instances = new ArrayList<>();
        for (int i = 0; i < this.attributes.size(); i++) {
            VaultGearAttribute<?> attribute = this.attributes.get(i);
            double value = i < this.values.size() ? this.values.get(i) : 1.0D;
            instances.add(VaultGearAttributeInstance.cast(attribute, value));
        }
        return instances.stream();
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT.writeBits(this.attributes.size(), buffer);
        for (int i = 0; i < this.attributes.size(); i++) {
            Adapters.GEAR_ATTRIBUTE.writeBits(this.attributes.get(i), buffer);
            Adapters.DOUBLE.writeBits(i < this.values.size() ? this.values.get(i) : 1.0D, buffer);
        }
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        int size = Adapters.INT.readBits(buffer).orElse(0);
        this.attributes = new ArrayList<>();
        this.values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.attributes.add(Adapters.GEAR_ATTRIBUTE.readBits(buffer).orElseThrow());
            this.values.add(Adapters.DOUBLE.readBits(buffer).orElse(1.0D));
        }
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            nbt.putInt("attributeCount", this.attributes.size());
            for (int i = 0; i < this.attributes.size(); i++) {
                final int index = i;
                Adapters.GEAR_ATTRIBUTE.writeNbt(this.attributes.get(index)).ifPresent(tag -> nbt.put("attribute_" + index, tag));
                Adapters.DOUBLE.writeNbt(index < this.values.size() ? this.values.get(index) : 1.0D).ifPresent(tag -> nbt.put("value_" + index, tag));
            }
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.attributes = new ArrayList<>();
        this.values = new ArrayList<>();
        int size = nbt.contains("attributeCount") ? nbt.getInt("attributeCount") : nbt.contains("attribute") ? 1 : 0;
        if (size == 0 && nbt.contains("attribute")) {
            Adapters.GEAR_ATTRIBUTE.readNbt(nbt.get("attribute")).ifPresent(attr -> {
                this.attributes.add(attr);
                this.values.add(Adapters.DOUBLE.readNbt(nbt.get("value")).orElse(1.0D));
            });
            return;
        }
        for (int i = 0; i < size; i++) {
            Adapters.GEAR_ATTRIBUTE.readNbt(nbt.get("attribute_" + i)).ifPresent(attr -> this.attributes.add(attr));
            this.values.add(Adapters.DOUBLE.readNbt(nbt.get("value_" + i)).orElse(1.0D));
        }
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            com.google.gson.JsonArray attributeArray = new com.google.gson.JsonArray();
            com.google.gson.JsonArray valueArray = new com.google.gson.JsonArray();
            for (int i = 0; i < this.attributes.size(); i++) {
                Adapters.GEAR_ATTRIBUTE.writeJson(this.attributes.get(i)).ifPresent(attributeArray::add);
                Adapters.DOUBLE.writeJson(i < this.values.size() ? this.values.get(i) : 1.0D).ifPresent(valueArray::add);
            }
            json.add("attributes", attributeArray);
            json.add("values", valueArray);
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.attributes = new ArrayList<>();
        this.values = new ArrayList<>();
        if (json.has("attributes")) {
            json.get("attributes").getAsJsonArray().forEach(element ->
                    Adapters.GEAR_ATTRIBUTE.readJson(element).ifPresent(attr -> this.attributes.add(attr)));
        }
        if (json.has("values")) {
            json.get("values").getAsJsonArray().forEach(element ->
                    this.values.add(Adapters.DOUBLE.readJson(element).orElse(1.0D)));
        }
        while (this.values.size() < this.attributes.size()) {
            this.values.add(1.0D);
        }
    }
}