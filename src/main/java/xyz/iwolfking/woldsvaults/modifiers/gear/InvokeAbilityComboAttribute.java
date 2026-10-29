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
import iskallia.vault.mana.FullManaPlayer;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.source.SkillSource;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.NetcodeUtils;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.api.util.AbilityHelper;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.InstantAbilityAccessor;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class InvokeAbilityComboAttribute extends ComboAttribute {
    private final String abilityId;
    private final int abilityLevel;

    public InvokeAbilityComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration, String abilityId, int abilityLevel) {
        super(requiredHits, associatedEffect, effectDuration);
        this.abilityId = abilityId;
        this.abilityLevel = abilityLevel;
    }

    public String getAbilityId() {
        return this.abilityId;
    }

    public int getAbilityLevel() {
        return this.abilityLevel;
    }

    @Override
    public void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute) {
        if (attribute instanceof InvokeAbilityComboAttribute invokeCombo && player instanceof ServerPlayer serverPlayer) {
            AbilityHelper.getAbilityRefFromConfig(invokeCombo.getAbilityId(), invokeCombo.getAbilityLevel()).ifPresent(instantAbility -> {
                SkillContext context = SkillContext.of(
                        serverPlayer,
                        SkillSource.of(serverPlayer)
                                .setPos(serverPlayer.position())
                                .setMana(FullManaPlayer.INSTANCE)
                );

                ((InstantAbilityAccessor) instantAbility).callDoAction(context);

                if (this.getAssociatedEffect() != null) {
                    spawnComboParticles(target, this.getAssociatedEffect().getColor());
                }
            });
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

    public static VaultGearAttributeType<InvokeAbilityComboAttribute> type() {
        return VaultGearAttributeType.of(
                (buf, attr) -> {
                    attr.writeToBuf(buf);
                    buf.writeString(attr.getAbilityId());
                    buf.writeInt(attr.getAbilityLevel());
                },
                buf -> {
                    BaseData base = readBaseFromBuf(buf);
                    String ability = buf.readString();
                    int level = buf.readInt();
                    return new InvokeAbilityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, ability, level);
                },
                (buf, attr) -> {
                    attr.writeNetToBuf(buf);
                    NetcodeUtils.writeString(buf, attr.getAbilityId());
                    buf.writeInt(attr.getAbilityLevel());
                },
                buf -> {
                    BaseData base = readBaseNetFromBuf(buf);
                    String ability = NetcodeUtils.readString(buf);
                    int level = buf.readInt();
                    return new InvokeAbilityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, ability, level);
                },
                VaultGearAttributeType.GSON::toJsonTree,
                tag -> {
                    CompoundTag ct = (CompoundTag) tag;
                    BaseData base = readBaseFromNbt(ct);
                    String ability = ct.getString("abilityId");
                    int level = ct.getInt("abilityLevel");
                    return new InvokeAbilityComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, ability, level);
                },
                attr -> {
                    CompoundTag ct = attr.writeToNbt();
                    ct.putString("abilityId", attr.getAbilityId());
                    ct.putInt("abilityLevel", attr.getAbilityLevel());
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

    private static class ComparatorImpl extends VaultGearAttributeComparator<InvokeAbilityComboAttribute> {
        public Optional<InvokeAbilityComboAttribute> merge(InvokeAbilityComboAttribute thisVal, InvokeAbilityComboAttribute thatVal) {
            return !Objects.equals(thisVal.getAbilityId(), thatVal.getAbilityId())
                    ? Optional.empty()
                    : Optional.of(new InvokeAbilityComboAttribute(
                    Math.max(thisVal.getRequiredHits(), thatVal.getRequiredHits()),
                    thisVal.getAssociatedEffectId(),
                    Math.max(thisVal.getEffectDuration(), thatVal.getEffectDuration()),
                    thisVal.getAbilityId(),
                    Math.max(thisVal.getAbilityLevel(), thatVal.getAbilityLevel())
            ));
        }

        public Optional<InvokeAbilityComboAttribute> difference(InvokeAbilityComboAttribute thisValue, InvokeAbilityComboAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<InvokeAbilityComboAttribute> getComparator() {
            return Comparator.comparing(InvokeAbilityComboAttribute::getAbilityId)
                    .thenComparing(InvokeAbilityComboAttribute::getAbilityLevel)
                    .thenComparing(InvokeAbilityComboAttribute::getRequiredHits);
        }
    }

    public static class Config extends ComboAttribute.Config {
        @Expose
        private final String abilityId;
        @Expose
        private final IntRollRangeEntry level;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration, String abilityId, IntRollRangeEntry level) {
            super(requiredHits, associatedEffect, effectDuration);
            this.abilityId = abilityId;
            this.level = level;
        }

        public String getAbilityId() {
            return this.abilityId;
        }

        public IntRollRangeEntry getLevel() {
            return this.level;
        }
    }

    public static class Generator extends ConfigurableAttributeGenerator<InvokeAbilityComboAttribute, InvokeAbilityComboAttribute.Config> {
        @Nullable
        @Override
        public Class<InvokeAbilityComboAttribute.Config> getConfigurationObjectClass() {
            return InvokeAbilityComboAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<InvokeAbilityComboAttribute> reader, InvokeAbilityComboAttribute.Config min, InvokeAbilityComboAttribute.Config max) {
            String abilityName = ModConfigs.ABILITIES.getAbilityById(min.getAbilityId()).map(Skill::getName).orElse(min.getAbilityId());
            return new TextComponent("Every " + min.getRequiredHits().getMin() + " hits, casts " + abilityName + " " + min.getLevel().getMin() + "-" + max.getLevel().getMax());
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<InvokeAbilityComboAttribute> reader, InvokeAbilityComboAttribute.Config object) {
            String abilityName = ModConfigs.ABILITIES.getAbilityById(object.getAbilityId()).map(Skill::getName).orElse(object.getAbilityId());
            return new TextComponent("")
                    .withStyle(reader.getColoredTextStyle())
                    .append("Every ")
                    .append(String.valueOf(object.getRequiredHits().getMin()))
                    .append(" hits, casts ")
                    .append(new TextComponent(abilityName).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" ")
                    .append(String.valueOf(object.getLevel().getMin()));
        }

        public InvokeAbilityComboAttribute generateRandomValue(InvokeAbilityComboAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            int hits = object.getRequiredHits().getRandom(rand);
            int lvl = object.getLevel().getRandom(rand);
            return new InvokeAbilityComboAttribute(hits, object.getAssociatedEffectId(), object.getEffectDuration(), object.getAbilityId(), lvl);
        }

        @Override
        public Optional<InvokeAbilityComboAttribute> getMinimumValue(List<InvokeAbilityComboAttribute.Config> configurations) {
            return configurations.stream()
                    .min(Comparator.comparing(config -> config.getRequiredHits().getMin()))
                    .map(config -> new InvokeAbilityComboAttribute(
                            config.getRequiredHits().getMin(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getAbilityId(),
                            config.getLevel().getMin()
                    ));
        }

        @Override
        public Optional<InvokeAbilityComboAttribute> getMaximumValue(List<InvokeAbilityComboAttribute.Config> configurations) {
            return configurations.stream()
                    .max(Comparator.comparing(config -> config.getRequiredHits().getRolledMaximum()))
                    .map(config -> new InvokeAbilityComboAttribute(
                            config.getRequiredHits().getRolledMaximum(),
                            config.getAssociatedEffectId(),
                            config.getEffectDuration(),
                            config.getAbilityId(),
                            config.getLevel().getRolledMaximum()
                    ));
        }

        public Optional<Float> getRollPercentage(InvokeAbilityComboAttribute value, List<InvokeAbilityComboAttribute.Config> configurations) {
            return MiscUtils.getFloatValueRange(
                    (float) value.getRequiredHits(),
                    this.getMinimumValue(configurations),
                    this.getMaximumValue(configurations),
                    attr -> (float) attr.getRequiredHits()
            );
        }
    }

    private static class Reader extends VaultGearModifierReader<InvokeAbilityComboAttribute> {
        protected Reader() {
            super("", 14901010);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<InvokeAbilityComboAttribute> instance, VaultGearModifier.AffixType type) {
            InvokeAbilityComboAttribute attribute = instance.getValue();
            String abilityName = ModConfigs.ABILITIES.getAbilityById(attribute.getAbilityId()).map(Skill::getName).orElse(attribute.getAbilityId());

            return new TextComponent("")
                    .append(type.getAffixPrefixComponent(true))
                    .append("Every ")
                    .append(new TextComponent(String.valueOf(attribute.getRequiredHits())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" hits, cast ")
                    .append(new TextComponent(abilityName).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" ")
                    .append(new TextComponent(String.valueOf(attribute.getAbilityLevel())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .setStyle(this.getColoredTextStyle());
        }

        @Nullable
        public MutableComponent getValueDisplay(InvokeAbilityComboAttribute value) {
            String abilityName = ModConfigs.ABILITIES.getAbilityById(value.getAbilityId()).map(Skill::getName).orElse(value.getAbilityId());
            return new TextComponent(abilityName + " " + value.getAbilityLevel());
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<InvokeAbilityComboAttribute> instance, VaultGearModifier.AffixType type) {
            InvokeAbilityComboAttribute attribute = instance.getValue();
            String abilityName = ModConfigs.ABILITIES.getAbilityById(attribute.getAbilityId()).map(Skill::getName).orElse(attribute.getAbilityId());
            out.add("Every " + attribute.getRequiredHits() + " hits, cast " + abilityName + " " + attribute.getAbilityLevel());
        }
    }
}