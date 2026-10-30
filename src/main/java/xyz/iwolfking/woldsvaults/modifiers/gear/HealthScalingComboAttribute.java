package xyz.iwolfking.woldsvaults.modifiers.gear;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.mojang.math.Vector3f;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.util.CurrentHealthDamageHelper;
import xyz.iwolfking.woldsvaults.api.util.MaxHealthDamageHelper;
import xyz.iwolfking.woldsvaults.api.util.MissingHealthDamageHelper;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HealthScalingComboAttribute extends ComboAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final DamageType scalingType;
    private final float healthMultiplier;

    public HealthScalingComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration, DamageType scalingType, float healthMultiplier) {
        super(requiredHits, associatedEffect, effectDuration);
        this.scalingType = scalingType;
        this.healthMultiplier = healthMultiplier;
    }

    public DamageType getScalingType() {
        return this.scalingType;
    }

    public float getHealthMultiplier() {
        return this.healthMultiplier;
    }

    @Override
    public void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute) {
        if (attribute instanceof HealthScalingComboAttribute healthCombo) {
            if (!target.level.isClientSide()) {
                ActiveFlags.IS_AOE_ATTACKING.push();
                float damage = switch (healthCombo.getScalingType()) {
                    case MAX_HEALTH -> MaxHealthDamageHelper.applyScaledMaxHealthDamageBonus(target, 0F, healthCombo.healthMultiplier);
                    case MISSING_HEALTH -> MissingHealthDamageHelper.getExecutionDamage(healthMultiplier, target);
                    case CURRENT_HEALTH -> CurrentHealthDamageHelper.getCurrentHealthDamage(healthMultiplier, target);
                };

                target.hurt(DamageSource.playerAttack(player), damage);
                ActiveFlags.IS_AOE_ATTACKING.pop();

                if (this.getAssociatedEffect() != null) {
                    spawnComboParticles(target, this.getAssociatedEffect().getColor());
                }
            }
        }
    }

    private static void spawnComboParticles(LivingEntity target, int colorInt) {
        if (!(target.level instanceof ServerLevel serverLevel)) return;

        float red = (float) ((colorInt >> 16) & 0xFF) / 255.0F;
        float green = (float) ((colorInt >> 8) & 0xFF) / 255.0F;
        float blue = (float) (colorInt & 0xFF) / 255.0F;

        DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(red, green, blue), 1.2F);

        serverLevel.sendParticles(
                particleOptions,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                20,
                0.5D, 0.5D, 0.5D,
                0.05D
        );
    }

    public static VaultGearAttributeType<HealthScalingComboAttribute> type() {
        return VaultGearAttributeType.of(
                (buf, attr) -> {
                    attr.writeToBuf(buf);
                    buf.writeString(attr.getScalingType().name());
                    buf.writeFloat(attr.getHealthMultiplier());
                },
                buf -> {
                    BaseData base = readBaseFromBuf(buf);
                    DamageType type = DamageType.valueOf(buf.readString());
                    float mult = buf.readFloat();
                    return new HealthScalingComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, type, mult);
                },
                (buf, attr) -> {
                    attr.writeNetToBuf(buf);
                    NetcodeUtils.writeString(buf, attr.getScalingType().name());
                    buf.writeFloat(attr.getHealthMultiplier());
                },
                buf -> {
                    BaseData base = readBaseNetFromBuf(buf);
                    DamageType type = DamageType.valueOf(NetcodeUtils.readString(buf));
                    float mult = buf.readFloat();
                    return new HealthScalingComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, type, mult);
                },
                VaultGearAttributeType.GSON::toJsonTree,
                tag -> {
                    CompoundTag ct = (CompoundTag) tag;
                    BaseData base = readBaseFromNbt(ct);
                    DamageType type = DamageType.valueOf(ct.getString("scalingType"));
                    float mult = ct.getFloat("healthMultiplier");
                    return new HealthScalingComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, type, mult);
                },
                attr -> {
                    CompoundTag ct = attr.writeToNbt();
                    ct.putString("scalingType", attr.getScalingType().name());
                    ct.putFloat("healthMultiplier", attr.getHealthMultiplier());
                    return ct;
                }
        );
    }

    public static ComparatorImpl comparator() {
        return new ComparatorImpl();
    }

    public static Generator generator() {
        return new Generator();
    }

    public static Reader reader() {
        return new Reader();
    }

    private static class ComparatorImpl extends VaultGearAttributeComparator<HealthScalingComboAttribute> {
        public Optional<HealthScalingComboAttribute> merge(HealthScalingComboAttribute thisVal, HealthScalingComboAttribute thatVal) {
            return thisVal.getScalingType() != thatVal.getScalingType()
                    ? Optional.empty()
                    : Optional.of(new HealthScalingComboAttribute(
                    Math.max(thisVal.getRequiredHits(), thatVal.getRequiredHits()),
                    thisVal.getAssociatedEffectId(),
                    Math.max(thisVal.getEffectDuration(), thatVal.getEffectDuration()),
                    thisVal.getScalingType(),
                    Math.max(thisVal.getHealthMultiplier(), thatVal.getHealthMultiplier())
            ));
        }

        public Optional<HealthScalingComboAttribute> difference(HealthScalingComboAttribute thisValue, HealthScalingComboAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<HealthScalingComboAttribute> getComparator() {
            return Comparator.comparing(HealthScalingComboAttribute::getHealthMultiplier)
                    .thenComparing(HealthScalingComboAttribute::getRequiredHits);
        }
    }

    public static class Config extends ComboAttribute.Config {
        @Expose
        private final DamageType scalingType;
        @Expose
        private final FloatRollRangeEntry healthMultiplier;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration, DamageType scalingType, FloatRollRangeEntry healthMultiplier) {
            super(requiredHits, associatedEffect, effectDuration);
            this.scalingType = scalingType;
            this.healthMultiplier = healthMultiplier;
        }

        public DamageType getScalingType() {
            return this.scalingType;
        }

        public FloatRollRangeEntry getHealthMultiplier() {
            return this.healthMultiplier;
        }
    }

    public static class Generator extends ConfigurableAttributeGenerator<HealthScalingComboAttribute, HealthScalingComboAttribute.Config> {
        @Nullable
        @Override
        public Class<HealthScalingComboAttribute.Config> getConfigurationObjectClass() {
            return HealthScalingComboAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<HealthScalingComboAttribute> reader, HealthScalingComboAttribute.Config min, HealthScalingComboAttribute.Config max) {
            return new TextComponent(FORMAT.format(min.getHealthMultiplier().getMin() * 100.0F) + "%")
                    .append("-")
                    .append(FORMAT.format(max.getHealthMultiplier().getMax() * 100.0F) + "%");
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<HealthScalingComboAttribute> reader, HealthScalingComboAttribute.Config object) {
            MutableComponent range = this.getConfigRangeDisplay(reader, object);
            if (range == null) return null;

            String typeDesc = switch (object.getScalingType()) {
                case CURRENT_HEALTH -> "current health";
                case MAX_HEALTH -> "maximum health";
                case MISSING_HEALTH -> "missing health";
            };

            return new TextComponent("")
                    .withStyle(reader.getColoredTextStyle())
                    .append("Every ")
                    .append(String.valueOf(object.getRequiredHits().getMin()))
                    .append(" hits, deals ")
                    .append(range.withStyle(reader.getColoredTextStyle()))
                    .append(" of target's ")
                    .append(new TextComponent(typeDesc).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" as bonus damage.");
        }

        public HealthScalingComboAttribute generateRandomValue(HealthScalingComboAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            float multiplier = object.getHealthMultiplier().getRandom(rand);
            int hits = object.getRequiredHits().getRandom(rand);
            return new HealthScalingComboAttribute(hits, object.getAssociatedEffectId(), object.getEffectDuration(), object.getScalingType(), multiplier);
        }

        @Override
        public Optional<HealthScalingComboAttribute> getMinimumValue(List<HealthScalingComboAttribute.Config> configurations) {
            return configurations.stream()
                    .min(Comparator.comparing(config -> config.getHealthMultiplier().getMin()))
                    .map(config -> new HealthScalingComboAttribute(
                            config.getRequiredHits().getMin(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getScalingType(),
                            config.getHealthMultiplier().getMin()
                    ));
        }

        @Override
        public Optional<HealthScalingComboAttribute> getMaximumValue(List<HealthScalingComboAttribute.Config> configurations) {
            return configurations.stream()
                    .max(Comparator.comparing(config -> config.getHealthMultiplier().getRolledMaximum()))
                    .map(config -> new HealthScalingComboAttribute(
                            config.getRequiredHits().getRolledMaximum(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getScalingType(),
                            config.getHealthMultiplier().getRolledMaximum()
                    ));
        }

        public Optional<Float> getRollPercentage(HealthScalingComboAttribute value, List<HealthScalingComboAttribute.Config> configurations) {
            return MiscUtils.getFloatValueRange(
                    value.getHealthMultiplier(),
                    this.getMinimumValue(configurations),
                    this.getMaximumValue(configurations),
                    HealthScalingComboAttribute::getHealthMultiplier
            );
        }
    }

    private static class Reader extends VaultGearModifierReader<HealthScalingComboAttribute> {
        protected Reader() {
            super("", 14901010);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<HealthScalingComboAttribute> instance, VaultGearModifier.AffixType type) {
            HealthScalingComboAttribute attribute = instance.getValue();
            MutableComponent multDisplay = this.getValueDisplay(attribute);
            if (multDisplay == null) return null;

            String typeDesc = switch (attribute.getScalingType()) {
                case CURRENT_HEALTH -> "current health";
                case MAX_HEALTH -> "maximum health";
                case MISSING_HEALTH -> "missing health";
            };

            return new TextComponent("")
                    .append(type.getAffixPrefixComponent(true))
                    .append("Every ")
                    .append(new TextComponent(String.valueOf(attribute.getRequiredHits())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" hits, deal ")
                    .append(multDisplay.withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" of target's ")
                    .append(new TextComponent(typeDesc).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" as bonus damage")
                    .setStyle(this.getColoredTextStyle());
        }

        @Nullable
        public MutableComponent getValueDisplay(HealthScalingComboAttribute value) {
            return new TextComponent(FORMAT.format(value.getHealthMultiplier() * 100.0F) + "%");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<HealthScalingComboAttribute> instance, VaultGearModifier.AffixType type) {
            HealthScalingComboAttribute attribute = instance.getValue();
            out.add(type.getAffixPrefix(attribute.getHealthMultiplier() >= 0.0F));
            out.add("Every " + attribute.getRequiredHits() + " hits, deal " + FORMAT.format(attribute.getHealthMultiplier() * 100.0F) + "% health scaling bonus damage");
        }
    }

    public enum DamageType {
        CURRENT_HEALTH,
        MAX_HEALTH,
        MISSING_HEALTH
    }
}