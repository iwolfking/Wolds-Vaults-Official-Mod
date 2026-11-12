package xyz.iwolfking.woldsvaults.modifiers.gear.unique;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Stormcrown storm tracking: a storm from {@code abilityKey} locks onto the mob closest to the arrow's hit
 * and follows it at up to {@code speed} blocks per second.
 */
public class StormTrackingAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private final boolean flag;
    private final String abilityKey;
    private final float speed;

    public StormTrackingAttribute(boolean flag, String abilityKey, float speed) {
        this.flag = flag;
        this.abilityKey = abilityKey;
        this.speed = speed;
    }

    public boolean isFlag() {
        return this.flag;
    }

    public String getAbilityKey() {
        return this.abilityKey;
    }

    public float getSpeed() {
        return this.speed;
    }

    public static VaultGearAttributeType<StormTrackingAttribute> type() {
        return VaultGearAttributeType.of(
            (buf, attr) -> {
                buf.writeBoolean(attr.isFlag());
                buf.writeString(attr.getAbilityKey());
                buf.writeFloat(attr.getSpeed());
            },
            buf -> new StormTrackingAttribute(buf.readBoolean(), buf.readString(), buf.readFloat()),
            (buf, attr) -> {
                buf.writeBoolean(attr.isFlag());
                NetcodeUtils.writeString(buf, attr.getAbilityKey());
                buf.writeFloat(attr.getSpeed());
            },
            buf -> new StormTrackingAttribute(buf.readBoolean(), NetcodeUtils.readString(buf), buf.readFloat()),
            VaultGearAttributeType.GSON::toJsonTree,
            tag -> {
                CompoundTag ct = (CompoundTag) tag;
                return new StormTrackingAttribute(ct.getBoolean("flag"), ct.getString("abilityKey"), ct.getFloat("speed"));
            },
            attr -> {
                CompoundTag ct = new CompoundTag();
                ct.putBoolean("flag", attr.isFlag());
                ct.putString("abilityKey", attr.getAbilityKey());
                ct.putFloat("speed", attr.getSpeed());
                return ct;
            }
        );
    }

    public static StormTrackingAttribute.AttributeComparator comparator() {
        return new StormTrackingAttribute.AttributeComparator();
    }

    public static StormTrackingAttribute.Generator generator() {
        return new StormTrackingAttribute.Generator();
    }

    public static StormTrackingAttribute.Reader reader() {
        return new StormTrackingAttribute.Reader();
    }

    private static String abilityName(String abilityKey) {
        return ModConfigs.ABILITIES.getAbilityById(abilityKey).map(Skill::getName).orElse(abilityKey);
    }

    private static class AttributeComparator extends VaultGearAttributeComparator<StormTrackingAttribute> {
        public Optional<StormTrackingAttribute> merge(StormTrackingAttribute thisVal, StormTrackingAttribute thatVal) {
            return !Objects.equals(thisVal.getAbilityKey(), thatVal.getAbilityKey())
                ? Optional.empty()
                : Optional.of(new StormTrackingAttribute(thisVal.isFlag() || thatVal.isFlag(), thisVal.getAbilityKey(), Math.max(thisVal.getSpeed(), thatVal.getSpeed())));
        }

        public Optional<StormTrackingAttribute> difference(StormTrackingAttribute thisValue, StormTrackingAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<StormTrackingAttribute> getComparator() {
            return Comparator.comparing(StormTrackingAttribute::getAbilityKey)
                .thenComparing(StormTrackingAttribute::getSpeed);
        }
    }

    public static class Config {
        @Expose private final boolean flag;
        @Expose private final String abilityKey;
        @Expose private final float speed;

        public Config(boolean flag, String abilityKey, float speed) {
            this.flag = flag;
            this.abilityKey = abilityKey;
            this.speed = speed;
        }

        public boolean isFlag() { return this.flag; }
        public String getAbilityKey() { return this.abilityKey; }
        public float getSpeed() { return this.speed; }
    }

    public static class Generator extends ConfigurableAttributeGenerator<StormTrackingAttribute, StormTrackingAttribute.Config> {
        @Nullable
        @Override
        public Class<StormTrackingAttribute.Config> getConfigurationObjectClass() {
            return StormTrackingAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<StormTrackingAttribute> reader, StormTrackingAttribute.Config min, StormTrackingAttribute.Config max) {
            return new TextComponent(FORMAT.format(min.getSpeed()) + "-" + FORMAT.format(max.getSpeed()) + " blocks/s");
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<StormTrackingAttribute> reader, StormTrackingAttribute.Config object) {
            return new TextComponent("")
                .withStyle(reader.getColoredTextStyle())
                .append(abilityName(object.getAbilityKey()))
                .append(" storm follows the closest enemy at ")
                .append(FORMAT.format(object.getSpeed()))
                .append(" blocks/s");
        }

        public StormTrackingAttribute generateRandomValue(StormTrackingAttribute.Config object, Random random) {
            return new StormTrackingAttribute(object.isFlag(), object.getAbilityKey(), object.getSpeed());
        }

        @Override
        public Optional<StormTrackingAttribute> getMinimumValue(List<StormTrackingAttribute.Config> configs) {
            return configs.stream().min(Comparator.comparing(StormTrackingAttribute.Config::getSpeed))
                .map(c -> new StormTrackingAttribute(c.isFlag(), c.getAbilityKey(), c.getSpeed()));
        }

        @Override
        public Optional<StormTrackingAttribute> getMaximumValue(List<StormTrackingAttribute.Config> configs) {
            return configs.stream().max(Comparator.comparing(StormTrackingAttribute.Config::getSpeed))
                .map(c -> new StormTrackingAttribute(c.isFlag(), c.getAbilityKey(), c.getSpeed()));
        }
    }

    private static class Reader extends VaultGearModifierReader<StormTrackingAttribute> {
        protected Reader() {
            super("", 15918901);
        }

        private Style getHighlightStyle() {
            return Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"));
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<StormTrackingAttribute> instance, VaultGearModifier.AffixType type) {
            StormTrackingAttribute attr = instance.getValue();
            if (!attr.isFlag()) {
                return null;
            }
            return new TextComponent(type.getAffixPrefix(true))
                .withStyle(this.getColoredTextStyle())
                .append(new TextComponent(abilityName(attr.getAbilityKey())).withStyle(this.getHighlightStyle()))
                .append(new TextComponent(" storm follows the closest enemy at ").withStyle(this.getColoredTextStyle()))
                .append(new TextComponent(FORMAT.format(attr.getSpeed()) + " blocks/s").withStyle(this.getHighlightStyle()));
        }

        @Nullable
        public MutableComponent getValueDisplay(StormTrackingAttribute value) {
            return new TextComponent(FORMAT.format(value.getSpeed()) + " blocks/s");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<StormTrackingAttribute> instance, VaultGearModifier.AffixType type) {
            StormTrackingAttribute attr = instance.getValue();
            out.add(type.getAffixPrefix(true));
            out.add(abilityName(attr.getAbilityKey()) + " storm follows the closest enemy at " + FORMAT.format(attr.getSpeed()) + " blocks/s");
        }
    }
}
