package xyz.iwolfking.woldsvaults.modifiers.gear.unique;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.MiscUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Steel Inferno: Fireball explosions shrink to {@code radiusMultiplier} of their radius and deal up to
 * {@code 1 + centreBonus} times damage at the centre, falling off as {@code 1 - (d/R)^2}.
 */
public class SteelInfernoAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private final float radiusMultiplier;
    private final float centreBonus;

    public SteelInfernoAttribute(float radiusMultiplier, float centreBonus) {
        this.radiusMultiplier = radiusMultiplier;
        this.centreBonus = centreBonus;
    }

    public float getRadiusMultiplier() {
        return this.radiusMultiplier;
    }

    public float getCentreBonus() {
        return this.centreBonus;
    }

    public static VaultGearAttributeType<SteelInfernoAttribute> type() {
        return VaultGearAttributeType.of(
            (buf, attr) -> {
                buf.writeFloat(attr.getRadiusMultiplier());
                buf.writeFloat(attr.getCentreBonus());
            },
            buf -> new SteelInfernoAttribute(buf.readFloat(), buf.readFloat()),
            (buf, attr) -> {
                buf.writeFloat(attr.getRadiusMultiplier());
                buf.writeFloat(attr.getCentreBonus());
            },
            buf -> new SteelInfernoAttribute(buf.readFloat(), buf.readFloat()),
            VaultGearAttributeType.GSON::toJsonTree,
            tag -> {
                CompoundTag ct = (CompoundTag) tag;
                return new SteelInfernoAttribute(ct.getFloat("radiusMultiplier"), ct.getFloat("centreBonus"));
            },
            attr -> {
                CompoundTag ct = new CompoundTag();
                ct.putFloat("radiusMultiplier", attr.getRadiusMultiplier());
                ct.putFloat("centreBonus", attr.getCentreBonus());
                return ct;
            }
        );
    }

    public static SteelInfernoAttribute.AttributeComparator comparator() {
        return new SteelInfernoAttribute.AttributeComparator();
    }

    public static SteelInfernoAttribute.Generator generator() {
        return new SteelInfernoAttribute.Generator();
    }

    public static SteelInfernoAttribute.Reader reader() {
        return new SteelInfernoAttribute.Reader();
    }

    private static String percent(float value) {
        return FORMAT.format(value * 100.0F) + "%";
    }

    private static class AttributeComparator extends VaultGearAttributeComparator<SteelInfernoAttribute> {
        public Optional<SteelInfernoAttribute> merge(SteelInfernoAttribute thisVal, SteelInfernoAttribute thatVal) {
            return Optional.of(thisVal.getCentreBonus() >= thatVal.getCentreBonus() ? thisVal : thatVal);
        }

        public Optional<SteelInfernoAttribute> difference(SteelInfernoAttribute thisValue, SteelInfernoAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<SteelInfernoAttribute> getComparator() {
            return Comparator.comparing(SteelInfernoAttribute::getCentreBonus)
                .thenComparing(SteelInfernoAttribute::getRadiusMultiplier);
        }
    }

    public static class Config {
        @Expose private final FloatRollRangeEntry radiusMultiplier;
        @Expose private final FloatRollRangeEntry centreBonus;

        public Config(FloatRollRangeEntry radiusMultiplier, FloatRollRangeEntry centreBonus) {
            this.radiusMultiplier = radiusMultiplier;
            this.centreBonus = centreBonus;
        }

        public FloatRollRangeEntry getRadiusMultiplier() { return this.radiusMultiplier; }
        public FloatRollRangeEntry getCentreBonus() { return this.centreBonus; }
    }

    public static class Generator extends ConfigurableAttributeGenerator<SteelInfernoAttribute, SteelInfernoAttribute.Config> {
        @Nullable
        @Override
        public Class<SteelInfernoAttribute.Config> getConfigurationObjectClass() {
            return SteelInfernoAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<SteelInfernoAttribute> reader, SteelInfernoAttribute.Config min, SteelInfernoAttribute.Config max) {
            return new TextComponent(FORMAT.format(min.getRadiusMultiplier().getMin()) + "-" + FORMAT.format(max.getRadiusMultiplier().getMax()) + "x, ")
                .append(percent(min.getCentreBonus().getMin()))
                .append("-")
                .append(percent(max.getCentreBonus().getMax()));
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<SteelInfernoAttribute> reader, SteelInfernoAttribute.Config object) {
            return new TextComponent("")
                .withStyle(reader.getColoredTextStyle())
                .append("Fireball explodes at ")
                .append(FORMAT.format(object.getRadiusMultiplier().getMin()) + "x")
                .append(" radius, up to +")
                .append(percent(object.getCentreBonus().getMin()))
                .append(" damage at its centre");
        }

        public SteelInfernoAttribute generateRandomValue(SteelInfernoAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            return new SteelInfernoAttribute(object.getRadiusMultiplier().getRandom(rand), object.getCentreBonus().getRandom(rand));
        }

        @Override
        public Optional<SteelInfernoAttribute> getMinimumValue(List<SteelInfernoAttribute.Config> configs) {
            return configs.stream().min(Comparator.comparing(c -> c.getCentreBonus().getMin()))
                .map(c -> new SteelInfernoAttribute(c.getRadiusMultiplier().getMin(), c.getCentreBonus().getMin()));
        }

        @Override
        public Optional<SteelInfernoAttribute> getMaximumValue(List<SteelInfernoAttribute.Config> configs) {
            return configs.stream().max(Comparator.comparing(c -> c.getCentreBonus().getRolledMaximum()))
                .map(c -> new SteelInfernoAttribute(c.getRadiusMultiplier().getRolledMaximum(), c.getCentreBonus().getRolledMaximum()));
        }

        public Optional<Float> getRollPercentage(SteelInfernoAttribute value, List<SteelInfernoAttribute.Config> configs) {
            return MiscUtils.getFloatValueRange(value.getCentreBonus(), this.getMinimumValue(configs), this.getMaximumValue(configs), SteelInfernoAttribute::getCentreBonus);
        }
    }

    private static class Reader extends VaultGearModifierReader<SteelInfernoAttribute> {
        protected Reader() {
            super("", 16737843);
        }

        private Style getHighlightStyle() {
            return Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"));
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<SteelInfernoAttribute> instance, VaultGearModifier.AffixType type) {
            SteelInfernoAttribute attr = instance.getValue();
            return new TextComponent(type.getAffixPrefix(true))
                .withStyle(this.getColoredTextStyle())
                .append(new TextComponent("Fireball explodes at ").withStyle(this.getColoredTextStyle()))
                .append(new TextComponent(FORMAT.format(attr.getRadiusMultiplier()) + "x").withStyle(this.getHighlightStyle()))
                .append(new TextComponent(" radius, up to ").withStyle(this.getColoredTextStyle()))
                .append(new TextComponent("+" + percent(attr.getCentreBonus())).withStyle(this.getHighlightStyle()))
                .append(new TextComponent(" damage at its centre").withStyle(this.getColoredTextStyle()));
        }

        @Nullable
        public MutableComponent getValueDisplay(SteelInfernoAttribute value) {
            return new TextComponent("+" + percent(value.getCentreBonus()));
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<SteelInfernoAttribute> instance, VaultGearModifier.AffixType type) {
            SteelInfernoAttribute attr = instance.getValue();
            out.add(type.getAffixPrefix(true));
            out.add("Fireball explodes at " + FORMAT.format(attr.getRadiusMultiplier()) + "x radius, up to +" + percent(attr.getCentreBonus()) + " damage at its centre");
        }
    }
}
