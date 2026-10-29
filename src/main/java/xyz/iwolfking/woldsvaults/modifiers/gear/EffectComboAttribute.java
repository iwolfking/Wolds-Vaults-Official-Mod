package xyz.iwolfking.woldsvaults.modifiers.gear;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.mojang.math.Vector3f;
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
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class EffectComboAttribute extends ComboAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final ResourceLocation effectId;
    private final int effectAmplifier;
    private final int effectDuration;

    public EffectComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration, ResourceLocation effectId, int effectAmplifier, int effectDurationTicks) {
        super(requiredHits, associatedEffect, effectDuration);
        this.effectId = effectId;
        this.effectAmplifier = effectAmplifier;
        this.effectDuration = effectDurationTicks;
    }

    public ResourceLocation getEffectId() {
        return this.effectId;
    }

    public MobEffect getTargetEffect() {
        return ForgeRegistries.MOB_EFFECTS.getValue(this.effectId);
    }

    public int getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    @Override
    public void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute) {
        if (attribute instanceof EffectComboAttribute effectCombo) {
            MobEffect effect = effectCombo.getTargetEffect();
            if (effect != null && !target.level.isClientSide()) {
                target.addEffect(new MobEffectInstance(effect, effectCombo.getEffectDuration(), effectCombo.getEffectAmplifier()));
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

    public static VaultGearAttributeType<EffectComboAttribute> type() {
        return VaultGearAttributeType.of(
                (buf, attr) -> {
                    attr.writeToBuf(buf);
                    buf.writeIdentifier(attr.getEffectId() == null ? ResourceLocation.parse("") : attr.getEffectId());
                    buf.writeInt(attr.getEffectAmplifier());
                    buf.writeInt(attr.getEffectDuration());
                },
                buf -> {
                    BaseData base = readBaseFromBuf(buf);
                    ResourceLocation effect = buf.readIdentifier();
                    int amp = buf.readInt();
                    int duration = buf.readInt();
                    return new EffectComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, effect, amp, duration);
                },
                (buf, attr) -> {
                    attr.writeNetToBuf(buf);
                    NetcodeUtils.writeIdentifier(buf, attr.getEffectId() == null ? ResourceLocation.parse("") : attr.getEffectId());
                    buf.writeInt(attr.getEffectAmplifier());
                    buf.writeInt(attr.getEffectDuration());
                },
                buf -> {
                    BaseData base = readBaseNetFromBuf(buf);
                    ResourceLocation effect = NetcodeUtils.readIdentifier(buf);
                    int amp = buf.readInt();
                    int duration = buf.readInt();
                    return new EffectComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, effect, amp, duration);
                },
                VaultGearAttributeType.GSON::toJsonTree,
                tag -> {
                    CompoundTag ct = (CompoundTag) tag;
                    BaseData base = readBaseFromNbt(ct);
                    String effectLoc = ct.getString("effectId");
                    ResourceLocation effect = effectLoc.isEmpty() ? null : ResourceLocation.parse(effectLoc);
                    int amp = ct.getInt("effectAmplifier");
                    int duration = ct.getInt("effectDurationTicks");
                    return new EffectComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, effect, amp, duration);
                },
                attr -> {
                    CompoundTag ct = attr.writeToNbt();
                    ct.putString("effectId", attr.getEffectId() == null ? "" : attr.getEffectId().toString());
                    ct.putInt("effectAmplifier", attr.getEffectAmplifier());
                    ct.putInt("effectDurationTicks", attr.getEffectDuration());
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

    private static class ComparatorImpl extends VaultGearAttributeComparator<EffectComboAttribute> {
        public Optional<EffectComboAttribute> merge(EffectComboAttribute thisVal, EffectComboAttribute thatVal) {
            return !Objects.equals(thisVal.getEffectId(), thatVal.getEffectId())
                    ? Optional.empty()
                    : Optional.of(new EffectComboAttribute(
                    Math.max(thisVal.getRequiredHits(), thatVal.getRequiredHits()),
                    thisVal.getAssociatedEffectId(),
                    Math.max(thisVal.getEffectDuration(), thatVal.getEffectDuration()),
                    thisVal.getEffectId(),
                    Math.max(thisVal.getEffectAmplifier(), thatVal.getEffectAmplifier()),
                    Math.max(thisVal.getEffectDuration(), thatVal.getEffectDuration())
            ));
        }

        public Optional<EffectComboAttribute> difference(EffectComboAttribute thisValue, EffectComboAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<EffectComboAttribute> getComparator() {
            return Comparator.comparing(attr -> attr.getEffectId().toString());
        }
    }

    public static class Config extends ComboAttribute.Config {
        @Expose
        private final ResourceLocation effectId;
        @Expose
        private final IntRollRangeEntry amplifier;
        @Expose
        private final IntRollRangeEntry duration;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration, ResourceLocation effectId, IntRollRangeEntry amplifier, IntRollRangeEntry duration) {
            super(requiredHits, associatedEffect, effectDuration);
            this.effectId = effectId;
            this.amplifier = amplifier;
            this.duration = duration;
        }

        public ResourceLocation getEffectId() {
            return this.effectId;
        }

        public IntRollRangeEntry getAmplifier() {
            return this.amplifier;
        }

        public IntRollRangeEntry getDuration() {
            return this.duration;
        }
    }

    public static class Generator extends ConfigurableAttributeGenerator<EffectComboAttribute, EffectComboAttribute.Config> {
        @Nullable
        @Override
        public Class<EffectComboAttribute.Config> getConfigurationObjectClass() {
            return EffectComboAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<EffectComboAttribute> reader, EffectComboAttribute.Config min, EffectComboAttribute.Config max) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(min.getEffectId());
            String effectName = effect != null ? effect.getDisplayName().getString() : min.getEffectId().toString();
            return new TextComponent("Every " + min.getRequiredHits().getMin() + " hits, applies " + effectName);
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<EffectComboAttribute> reader, EffectComboAttribute.Config object) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(object.getEffectId());
            String effectName = effect != null ? effect.getDisplayName().getString() : object.getEffectId().toString();
            return new TextComponent("")
                    .withStyle(reader.getColoredTextStyle())
                    .append("Every ")
                    .append(String.valueOf(object.getRequiredHits().getMin()))
                    .append(" hits, applies ")
                    .append(new TextComponent(effectName).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))));
        }

        public EffectComboAttribute generateRandomValue(EffectComboAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            int hits = object.getRequiredHits().getRandom(rand);
            int amp = object.getAmplifier().getRandom(rand);
            int dur = object.getDuration().getRandom(rand);
            return new EffectComboAttribute(hits, object.getAssociatedEffectId(), object.getEffectDuration(), object.getEffectId(), amp, dur);
        }

        @Override
        public Optional<EffectComboAttribute> getMinimumValue(List<EffectComboAttribute.Config> configurations) {
            return configurations.stream()
                    .min(Comparator.comparing(config -> config.getRequiredHits().getMin()))
                    .map(config -> new EffectComboAttribute(
                            config.getRequiredHits().getMin(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getEffectId(),
                            config.getAmplifier().getMin(),
                            config.getDuration().getMin()
                    ));
        }

        @Override
        public Optional<EffectComboAttribute> getMaximumValue(List<EffectComboAttribute.Config> configurations) {
            return configurations.stream()
                    .max(Comparator.comparing(config -> config.getRequiredHits().getRolledMaximum()))
                    .map(config -> new EffectComboAttribute(
                            config.getRequiredHits().getRolledMaximum(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getEffectId(),
                            config.getAmplifier().getRolledMaximum(),
                            config.getDuration().getRolledMaximum()
                    ));
        }

        public Optional<Float> getRollPercentage(EffectComboAttribute value, List<EffectComboAttribute.Config> configurations) {
            return MiscUtils.getFloatValueRange(
                    (float) value.getRequiredHits(),
                    this.getMinimumValue(configurations),
                    this.getMaximumValue(configurations),
                    attr -> (float) attr.getRequiredHits()
            );
        }
    }

    private static class Reader extends VaultGearModifierReader<EffectComboAttribute> {
        protected Reader() {
            super("", 14901010);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<EffectComboAttribute> instance, VaultGearModifier.AffixType type) {
            EffectComboAttribute attribute = instance.getValue();
            MobEffect effect = attribute.getTargetEffect();
            String effectName = effect != null ? effect.getDisplayName().getString() : "Effect";
            int level = attribute.getEffectAmplifier() + 1;

            return new TextComponent("")
                    .append(type.getAffixPrefixComponent(true))
                    .append("Every ")
                    .append(new TextComponent(String.valueOf(attribute.getRequiredHits())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" hits, apply ")
                    .append(new TextComponent(effectName + " " + level).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .setStyle(this.getColoredTextStyle());
        }

        @Nullable
        public MutableComponent getValueDisplay(EffectComboAttribute value) {
            MobEffect effect = value.getTargetEffect();
            String effectName = effect != null ? effect.getDisplayName().getString() : "Effect";
            return new TextComponent(effectName);
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<EffectComboAttribute> instance, VaultGearModifier.AffixType type) {
            EffectComboAttribute attribute = instance.getValue();
            MobEffect effect = attribute.getTargetEffect();
            String effectName = effect != null ? effect.getDisplayName().getString() : "Effect";
            out.add("Every " + attribute.getRequiredHits() + " hits, apply " + effectName);
        }
    }
}