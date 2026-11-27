package xyz.iwolfking.woldsvaults.modifiers.gear;

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
import iskallia.vault.util.MiscUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Battlestaff combo that pulls nearby enemies together. When the combo completes on a target, the target's position is
 * handed to {@link GravityComboPulls}, which pulls every eligible enemy within the rolled radius toward the average
 * position of all targets whose gravity combo completed on the same swing.
 */
public class GravityComboAttribute extends ComboAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.#");

    private final float radius;

    public GravityComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration, float radius) {
        super(requiredHits, associatedEffect, effectDuration);
        this.radius = radius;
    }

    public float getRadius() {
        return this.radius;
    }

    @Override
    public void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute) {
        if (attribute instanceof GravityComboAttribute gravity && player instanceof ServerPlayer serverPlayer && !target.level.isClientSide()) {
            int color = this.getAssociatedEffect() != null ? this.getAssociatedEffect().getColor() : 0x5b3fa8;
            GravityComboPulls.record(serverPlayer, target.position(), gravity.getRadius(), color);
        }
    }

    public static VaultGearAttributeType<GravityComboAttribute> type() {
        return VaultGearAttributeType.of(
                (buf, attr) -> {
                    attr.writeToBuf(buf);
                    buf.writeFloat(attr.getRadius());
                },
                buf -> {
                    BaseData base = readBaseFromBuf(buf);
                    return new GravityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, buf.readFloat());
                },
                (buf, attr) -> {
                    attr.writeNetToBuf(buf);
                    buf.writeFloat(attr.getRadius());
                },
                buf -> {
                    BaseData base = readBaseNetFromBuf(buf);
                    return new GravityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, buf.readFloat());
                },
                VaultGearAttributeType.GSON::toJsonTree,
                tag -> {
                    CompoundTag ct = (CompoundTag) tag;
                    BaseData base = readBaseFromNbt(ct);
                    return new GravityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, ct.getFloat("radius"));
                },
                attr -> {
                    CompoundTag ct = attr.writeToNbt();
                    ct.putFloat("radius", attr.getRadius());
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

    private static class ComparatorImpl extends VaultGearAttributeComparator<GravityComboAttribute> {
        public Optional<GravityComboAttribute> merge(GravityComboAttribute thisVal, GravityComboAttribute thatVal) {
            return Optional.of(new GravityComboAttribute(
                    Math.min(thisVal.getRequiredHits(), thatVal.getRequiredHits()),
                    thisVal.getAssociatedEffectId(),
                    Math.max(thisVal.getEffectDuration(), thatVal.getEffectDuration()),
                    Math.max(thisVal.getRadius(), thatVal.getRadius())
            ));
        }

        public Optional<GravityComboAttribute> difference(GravityComboAttribute thisValue, GravityComboAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<GravityComboAttribute> getComparator() {
            return Comparator.comparing(GravityComboAttribute::getRadius);
        }
    }

    public static class Config extends ComboAttribute.Config {
        @Expose
        private final FloatRollRangeEntry radius;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration, FloatRollRangeEntry radius) {
            super(requiredHits, associatedEffect, effectDuration);
            this.radius = radius;
        }

        public FloatRollRangeEntry getRadius() {
            return this.radius;
        }
    }

    public static class Generator extends ConfigurableAttributeGenerator<GravityComboAttribute, GravityComboAttribute.Config> {
        @Nullable
        @Override
        public Class<GravityComboAttribute.Config> getConfigurationObjectClass() {
            return GravityComboAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<GravityComboAttribute> reader, GravityComboAttribute.Config min, GravityComboAttribute.Config max) {
            return new TextComponent("Every " + min.getRequiredHits().getMin() + "-" + max.getRequiredHits().getMax()
                    + " hits, pull enemies within " + FORMAT.format(min.getRadius().getMin()) + "-" + FORMAT.format(max.getRadius().getMax()) + " blocks");
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<GravityComboAttribute> reader, GravityComboAttribute.Config object) {
            return new TextComponent("")
                    .withStyle(reader.getColoredTextStyle())
                    .append("Every ")
                    .append(String.valueOf(object.getRequiredHits().getMin()))
                    .append(" hits, pull enemies within ")
                    .append(new TextComponent(FORMAT.format(object.getRadius().getMin()) + " blocks").withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))));
        }

        public GravityComboAttribute generateRandomValue(GravityComboAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            int hits = object.getRequiredHits().getRandom(rand);
            float radius = object.getRadius().getRandom(rand);
            return new GravityComboAttribute(hits, object.getAssociatedEffectId(), object.getEffectDuration(), radius);
        }

        @Override
        public Optional<GravityComboAttribute> getMinimumValue(List<GravityComboAttribute.Config> configurations) {
            return configurations.stream()
                    .min(Comparator.comparing(config -> config.getRadius().getMin()))
                    .map(config -> new GravityComboAttribute(config.getRequiredHits().getRolledMaximum(), config.getAssociatedEffectId(),
                            config.getEffectDuration(), config.getRadius().getMin()));
        }

        @Override
        public Optional<GravityComboAttribute> getMaximumValue(List<GravityComboAttribute.Config> configurations) {
            return configurations.stream()
                    .max(Comparator.comparing(config -> config.getRadius().getRolledMaximum()))
                    .map(config -> new GravityComboAttribute(config.getRequiredHits().getMin(), config.getAssociatedEffectId(),
                            config.getEffectDuration(), config.getRadius().getRolledMaximum()));
        }

        public Optional<Float> getRollPercentage(GravityComboAttribute value, List<GravityComboAttribute.Config> configurations) {
            return MiscUtils.getFloatValueRange(
                    value.getRadius(),
                    this.getMinimumValue(configurations),
                    this.getMaximumValue(configurations),
                    GravityComboAttribute::getRadius
            );
        }
    }

    private static class Reader extends VaultGearModifierReader<GravityComboAttribute> {
        protected Reader() {
            super("", 5980072);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<GravityComboAttribute> instance, VaultGearModifier.AffixType type) {
            GravityComboAttribute attribute = instance.getValue();
            return new TextComponent("")
                    .append(type.getAffixPrefixComponent(true))
                    .append("Every ")
                    .append(new TextComponent(String.valueOf(attribute.getRequiredHits())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" hits, pull enemies within ")
                    .append(new TextComponent(FORMAT.format(attribute.getRadius()) + " blocks").withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .setStyle(this.getColoredTextStyle());
        }

        @Nullable
        public MutableComponent getValueDisplay(GravityComboAttribute value) {
            return new TextComponent(FORMAT.format(value.getRadius()) + " blocks");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<GravityComboAttribute> instance, VaultGearModifier.AffixType type) {
            GravityComboAttribute attribute = instance.getValue();
            out.add("Every " + attribute.getRequiredHits() + " hits, pull enemies within " + FORMAT.format(attribute.getRadius()) + " blocks");
        }
    }
}
