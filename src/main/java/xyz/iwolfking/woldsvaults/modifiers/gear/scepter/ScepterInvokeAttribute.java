package xyz.iwolfking.woldsvaults.modifiers.gear.scepter;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.mana.FullManaPlayer;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.NetcodeUtils;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.InstantAbilityAccessor;

public class ScepterInvokeAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private final String abilityId;
    private final int level;
    private final float chargeTicks;

    protected ScepterInvokeAttribute(String abilityId, int level, float chargeTicks) {
        this.abilityId = abilityId;
        this.level = level;
        this.chargeTicks = chargeTicks;
    }

    public String getAbilityId() {
        return this.abilityId;
    }

    public int getLevel() {
        return this.level;
    }

    public float getChargeTicks() {
        return this.chargeTicks;
    }

    public void trigger(ServerPlayer player) {
        AbilityHelper.getAbilityRefFromConfig(abilityId, level).ifPresent(instantAbility -> {
            SkillContext context = SkillContext.of(
                    player,
                    SkillSource.of(player)
                            .setPos(player.position())
                            .setMana(FullManaPlayer.INSTANCE)
            );

            ((InstantAbilityAccessor)instantAbility).callDoAction(context);
        });
    }

    public static VaultGearAttributeType<ScepterInvokeAttribute> type() {
        return VaultGearAttributeType.of(
            (buf, attr) -> {
                buf.writeString(attr.getAbilityId());
                buf.writeInt(attr.getLevel());
                buf.writeFloat(attr.getChargeTicks());
            },
            buf -> new ScepterInvokeAttribute(buf.readString(), buf.readInt(), buf.readFloat()),
            (buf, attr) -> {
                NetcodeUtils.writeString(buf, attr.getAbilityId());
                buf.writeInt(attr.getLevel());
                buf.writeFloat(attr.getChargeTicks());
            },
            buf -> new ScepterInvokeAttribute(NetcodeUtils.readString(buf), buf.readInt(), buf.readFloat()),
            VaultGearAttributeType.GSON::toJsonTree,
            tag -> {
                CompoundTag ct = (CompoundTag) tag;
                return new ScepterInvokeAttribute(ct.getString("abilityId"), ct.getInt("level"), ct.getFloat("chargeTicks"));
            },
            attr -> {
                CompoundTag ct = new CompoundTag();
                ct.putString("abilityId", attr.getAbilityId());
                ct.putInt("level", attr.getLevel());
                ct.putFloat("chargeTicks", attr.getChargeTicks());
                return ct;
            }
        );
    }

    public static ScepterInvokeAttribute.AttributeComparator comparator() {
        return new ScepterInvokeAttribute.AttributeComparator();
    }

    public static ScepterInvokeAttribute.Generator generator() {
        return new ScepterInvokeAttribute.Generator();
    }

    public static ScepterInvokeAttribute.Reader reader() {
        return new ScepterInvokeAttribute.Reader();
    }

    private static class AttributeComparator extends VaultGearAttributeComparator<ScepterInvokeAttribute> {
        public Optional<ScepterInvokeAttribute> merge(ScepterInvokeAttribute thisVal, ScepterInvokeAttribute thatVal) {
            return !Objects.equals(thisVal.getAbilityId(), thatVal.getAbilityId())
                ? Optional.empty()
                : Optional.of(new ScepterInvokeAttribute(
                    thisVal.getAbilityId(),
                    Math.max(thisVal.getLevel(), thatVal.getLevel()),
                    Math.min(thisVal.getChargeTicks(), thatVal.getChargeTicks())
                ));
        }

        public Optional<ScepterInvokeAttribute> difference(ScepterInvokeAttribute thisValue, ScepterInvokeAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<ScepterInvokeAttribute> getComparator() {
            return Comparator.comparing(ScepterInvokeAttribute::getAbilityId)
                .thenComparing(ScepterInvokeAttribute::getLevel)
                .thenComparing(ScepterInvokeAttribute::getChargeTicks);
        }
    }

    public static class Config {
        @Expose private final String abilityId;
        @Expose private final IntRollRangeEntry level;
        @Expose private final FloatRollRangeEntry chargeTicks;

        public Config(String abilityId, IntRollRangeEntry level, FloatRollRangeEntry chargeTicks) {
            this.abilityId = abilityId;
            this.level = level;
            this.chargeTicks = chargeTicks;
        }

        public String getAbilityId() { return this.abilityId; }
        public IntRollRangeEntry getLevel() { return this.level; }
        public FloatRollRangeEntry getChargeTicks() { return this.chargeTicks; }
    }

    public static class Generator extends ConfigurableAttributeGenerator<ScepterInvokeAttribute, ScepterInvokeAttribute.Config> {
        @Nullable
        @Override
        public Class<ScepterInvokeAttribute.Config> getConfigurationObjectClass() {
            return ScepterInvokeAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<ScepterInvokeAttribute> reader, ScepterInvokeAttribute.Config min, ScepterInvokeAttribute.Config max) {
            return new TextComponent(min.getLevel().getMin() + "-" + max.getLevel().getMax() + ", ")
                .append(FORMAT.format(min.getChargeTicks().getMin() / 20.0F))
                .append("-")
                .append(FORMAT.format(max.getChargeTicks().getMax() / 20.0F))
                .append("s");
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<ScepterInvokeAttribute> reader, ScepterInvokeAttribute.Config object) {
            String abilityName = ModConfigs.ABILITIES.getAbilityById(object.getAbilityId()).map(Skill::getName).orElse("");
            return new TextComponent("")
                .withStyle(reader.getColoredTextStyle())
                .append("Invokes ")
                .append(String.valueOf(object.getLevel().getMin()))
                .append(" ")
                .append(abilityName)
                .append(" after ")
                .append(FORMAT.format(object.getChargeTicks().getMin() / 20.0F))
                .append("s");
        }

        public ScepterInvokeAttribute generateRandomValue(ScepterInvokeAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            return new ScepterInvokeAttribute(object.getAbilityId(), object.getLevel().getRandom(rand), object.getChargeTicks().getRandom(rand));
        }

        @Override
        public Optional<ScepterInvokeAttribute> getMinimumValue(List<ScepterInvokeAttribute.Config> configs) {
            return configs.stream().min(Comparator.comparing(c -> c.getLevel().getMin()))
                .map(c -> new ScepterInvokeAttribute(c.getAbilityId(), c.getLevel().getMin(), c.getChargeTicks().getMin()));
        }

        @Override
        public Optional<ScepterInvokeAttribute> getMaximumValue(List<ScepterInvokeAttribute.Config> configs) {
            return configs.stream().max(Comparator.comparing(c -> c.getLevel().getRolledMaximum()))
                .map(c -> new ScepterInvokeAttribute(c.getAbilityId(), c.getLevel().getRolledMaximum(), c.getChargeTicks().getRolledMaximum()));
        }

        public Optional<Float> getRollPercentage(ScepterInvokeAttribute value, List<ScepterInvokeAttribute.Config> configs) {
            return MiscUtils.getFloatValueRange(value.getChargeTicks(), this.getMinimumValue(configs), this.getMaximumValue(configs), ScepterInvokeAttribute::getChargeTicks);
        }
    }

    private static class Reader extends VaultGearModifierReader<ScepterInvokeAttribute> {
        protected Reader() {
            super("", 14901010);
        }

        private Style getHighlightStyle() {
            return Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"));
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<ScepterInvokeAttribute> instance, VaultGearModifier.AffixType type) {
            ScepterInvokeAttribute attr = instance.getValue();
            String abilityName = ModConfigs.ABILITIES.getAbilityById(attr.abilityId).map(Skill::getName).orElse("");
            if (abilityName.isEmpty()) return null;

            return new TextComponent(type.getAffixPrefix(true))
                .withStyle(this.getColoredTextStyle())
                .append(new TextComponent("Invokes ").withStyle(this.getColoredTextStyle()))
                .append(new TextComponent(abilityName).withStyle(this.getHighlightStyle()))
                .append(new TextComponent(" " + attr.level).withStyle(this.getHighlightStyle()))
                .append(new TextComponent(" after ").withStyle(this.getColoredTextStyle()))
                .append(new TextComponent(FORMAT.format(attr.getChargeTicks() / 20.0F) + "s").withStyle(this.getHighlightStyle()));
        }

        @Nullable
        public MutableComponent getValueDisplay(ScepterInvokeAttribute value) {
            return new TextComponent(FORMAT.format(value.getChargeTicks() / 20.0F) + "s");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<ScepterInvokeAttribute> instance, VaultGearModifier.AffixType type) {
            ScepterInvokeAttribute attr = instance.getValue();
            String abilityName = ModConfigs.ABILITIES.getAbilityById(attr.abilityId).map(Skill::getName).orElse("");
            out.add(type.getAffixPrefix(true));
            out.add("Invokes " + abilityName + " " + attr.level + " after " + FORMAT.format(attr.getChargeTicks() / 20.0F) + "s");
        }
    }
}