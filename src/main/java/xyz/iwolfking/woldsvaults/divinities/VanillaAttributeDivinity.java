package xyz.iwolfking.woldsvaults.divinities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.basic.EnumAdapter;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TickingSkill;
import iskallia.vault.skill.talent.GearAttributeSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Optional;
import java.util.stream.Stream;

public class VanillaAttributeDivinity extends LearnableSkill implements GearAttributeSkill, TickingSkill {
    protected static final EnumAdapter<AttributeModifier.Operation> OPERATION_ORDINAL = Adapters.ofEnum(AttributeModifier.Operation.class, EnumAdapter.Mode.ORDINAL);
    protected static final EnumAdapter<AttributeModifier.Operation> OPERATION_NAME = Adapters.ofEnum(AttributeModifier.Operation.class, EnumAdapter.Mode.NAME);
    private Attribute attribute;
    private AttributeModifier.Operation operation;
    public double amount;

    public VanillaAttributeDivinity(int unlockLevel, int learnPointCost, int regretPointCost, Attribute attribute, AttributeModifier.Operation operation, double amount) {
        super(unlockLevel, learnPointCost, regretPointCost);
        this.attribute = attribute;
        this.operation = operation;
        this.amount = amount;
    }

    public VanillaAttributeDivinity() {
    }

    public void onAdd(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
    }

    public void onRemove(SkillContext context) {
        context.getSource().as(ServerPlayer.class).ifPresent(this::refreshSnapshot);
    }

    public void onTick(SkillContext context) {
        if (!this.isUnlocked()) {
            this.onRemoveModifiers(context);
        } else {
            this.onAddModifiers(context);
        }

    }

    public Stream<VaultGearAttributeInstance<?>> getGearAttributes(SkillContext context) {
        VaultGearAttribute<?> attribute = ModGearAttributes.getGearAttribute(this.attribute, this.operation);
        return attribute == null ? Stream.empty() : Stream.of(VaultGearAttributeInstance.cast(attribute, this.amount));
    }

    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.ATTRIBUTE.writeBits(this.attribute, buffer);
        OPERATION_ORDINAL.writeBits(this.operation, buffer);
        Adapters.DOUBLE.writeBits(this.amount, buffer);
    }

    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.attribute = Adapters.ATTRIBUTE.readBits(buffer).orElseThrow();
        this.operation = OPERATION_ORDINAL.readBits(buffer).orElseThrow();
        this.amount = Adapters.DOUBLE.readBits(buffer).orElseThrow();
    }

    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map((nbt) -> {
            Adapters.ATTRIBUTE.writeNbt(this.attribute).ifPresent((tag) -> {
                nbt.put("attribute", tag);
            });
            OPERATION_NAME.writeNbt(this.operation).ifPresent((tag) -> {
                nbt.put("operation", tag);
            });
            Adapters.DOUBLE.writeNbt(this.amount).ifPresent((tag) -> {
                nbt.put("amount", tag);
            });
            return nbt;
        });
    }

    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.attribute = Adapters.ATTRIBUTE.readNbt(nbt.get("attribute")).orElseThrow(() -> {
            return new IllegalStateException("Unknown attribute in " + nbt);
        });
        this.operation = OPERATION_NAME.readNbt(nbt.get("operation")).orElseThrow();
        this.amount = Adapters.DOUBLE.readNbt(nbt.get("amount")).orElseThrow();
    }

    public Optional<JsonObject> writeJson() {
        return super.writeJson().map((json) -> {
            Adapters.ATTRIBUTE.writeJson(this.attribute).ifPresent((element) -> {
                json.add("attribute", element);
            });
            OPERATION_NAME.writeJson(this.operation).ifPresent((element) -> {
                json.add("operation", element);
            });
            Adapters.DOUBLE.writeJson(this.amount).ifPresent((element) -> {
                json.add("amount", element);
            });
            return json;
        });
    }

    public void readJson(JsonObject json) {
        super.readJson(json);
        this.attribute = Adapters.ATTRIBUTE.readJson(json.get("attribute")).orElseThrow(() -> {
            return new IllegalStateException("Unknown attribute in " + json);
        });
        this.operation = OPERATION_NAME.readJson(json.get("operation")).orElseThrow();
        this.amount = Adapters.DOUBLE.readJson(json.get("amount")).orElseThrow();
    }
}
