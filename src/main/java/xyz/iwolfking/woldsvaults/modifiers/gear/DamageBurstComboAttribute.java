package xyz.iwolfking.woldsvaults.modifiers.gear;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.mojang.math.Vector3f;
import iskallia.vault.config.entry.FloatRollRangeEntry;
import iskallia.vault.config.entry.IntRollRangeEntry;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.ConfigurableAttributeGenerator;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.comparator.VaultGearAttributeComparator;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.mana.ManaPlayer;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.util.NetcodeUtils;
import iskallia.vault.util.calc.*;
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
import xyz.iwolfking.woldsvaults.api.util.VaultGearAttributeHelper;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DamageBurstComboAttribute extends ComboAttribute {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

    private final VaultGearAttribute<?> scaledAttribute;
    private final float damageMultiplier;

    public DamageBurstComboAttribute(int requiredHits, ResourceLocation associatedEffect, int effectDuration, VaultGearAttribute<?> scaledAttribute, float damageMultiplier) {
        super(requiredHits, associatedEffect, effectDuration);
        this.scaledAttribute = scaledAttribute;
        this.damageMultiplier = damageMultiplier;
    }

    public VaultGearAttribute<?> getScaledAttribute() {
        return this.scaledAttribute;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public float calculateBonusDamage(LivingEntity attacker) {
        if (this.scaledAttribute == null || !(attacker instanceof Player player)) {
            return 0.0F;
        }

        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(attacker);

        float statValue = 0.0F;
        if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.ABILITY_POWER.getRegistryName())) {
            statValue = AbilityPowerHelper.getAbilityPower(player);
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.THORNS_DAMAGE_FLAT.getRegistryName())) {
            statValue = ThornsHelper.getAdditionalThornsFlatDamage(player);
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.HEALTH.getRegistryName())) {
            statValue = attacker.getMaxHealth();
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.ITEM_QUANTITY.getRegistryName())) {
            statValue = ItemQuantityHelper.getItemQuantity(attacker);
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.ITEM_RARITY.getRegistryName())) {
            statValue = ItemRarityHelper.getItemRarity(attacker);
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.COPIOUSLY.getRegistryName())) {
            statValue = CopiousHelper.getCopiousChance(attacker);
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.MANA_ADDITIVE_PERCENTILE.getRegistryName())) {
            if(player instanceof ManaPlayer manaPlayer) {
                statValue = manaPlayer.getManaMax();
            }
        }
        else if (this.scaledAttribute.getRegistryName().equals(ModGearAttributes.MANA_ADDITIVE.getRegistryName())) {
            if(player instanceof ManaPlayer manaPlayer) {
                statValue = manaPlayer.getMana();
            }
        }
        else {
            statValue = VaultGearAttributeHelper.resolveSnapshotValue(snapshot, this.scaledAttribute);
        }

        return statValue * this.damageMultiplier;
    }

    public static VaultGearAttributeType<DamageBurstComboAttribute> type() {
        return VaultGearAttributeType.of(
                (buf, attribute) -> {
                    attribute.writeToBuf(buf);
                    ResourceLocation attrKey = attribute.getScaledAttribute() == null ? ResourceLocation.parse("") : attribute.getScaledAttribute().getRegistryName();
                    buf.writeIdentifier(attrKey == null ? ResourceLocation.parse("") : attrKey);
                    buf.writeFloat(attribute.getDamageMultiplier());
                },
                buf -> {
                    BaseData base = readBaseFromBuf(buf);
                    ResourceLocation attrLoc = buf.readIdentifier();
                    VaultGearAttribute<?> attr = attrLoc.toString().isEmpty() ? null : VaultGearAttributeRegistry.getAttribute(attrLoc);
                    float mult = buf.readFloat();
                    return new DamageBurstComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, attr, mult);
                },
                (buf, attribute) -> {
                    attribute.writeNetToBuf(buf);
                    ResourceLocation attrKey = attribute.getScaledAttribute() == null ? ResourceLocation.parse("") : attribute.getScaledAttribute().getRegistryName();
                    NetcodeUtils.writeIdentifier(buf, attrKey == null ? ResourceLocation.parse("") : attrKey);
                    buf.writeFloat(attribute.getDamageMultiplier());
                },
                buf -> {
                    BaseData base = readBaseNetFromBuf(buf);
                    ResourceLocation attrLoc = NetcodeUtils.readIdentifier(buf);
                    VaultGearAttribute<?> attr = attrLoc.toString().isEmpty() ? null : VaultGearAttributeRegistry.getAttribute(attrLoc);
                    float mult = buf.readFloat();
                    return new DamageBurstComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, attr, mult);
                },
                VaultGearAttributeType.GSON::toJsonTree,
                tag -> {
                    CompoundTag compoundTag = (CompoundTag) tag;
                    BaseData base = readBaseFromNbt(compoundTag);
                    String attrLoc = compoundTag.getString("scaledAttribute");
                    VaultGearAttribute<?> attr = attrLoc.isEmpty() ? null : VaultGearAttributeRegistry.getAttribute(ResourceLocation.parse(attrLoc));
                    float mult = compoundTag.getFloat("damageMultiplier");
                    return new DamageBurstComboAttribute(base.requiredHits, base.associatedEffect, base.effectDuration, attr, mult);
                },
                attribute -> {
                    CompoundTag compoundTag = attribute.writeToNbt();
                    ResourceLocation attrKey = attribute.getScaledAttribute() == null ? null : attribute.getScaledAttribute().getRegistryName();
                    compoundTag.putString("scaledAttribute", attrKey == null ? "" : attrKey.toString());
                    compoundTag.putFloat("damageMultiplier", attribute.getDamageMultiplier());
                    return compoundTag;
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

    @Override
    public void triggerComboEffect(Player player, LivingEntity target, ComboAttribute attribute) {
        if (attribute instanceof DamageBurstComboAttribute damageBurst) {
            float bonusDamage = damageBurst.calculateBonusDamage(player);
            if (bonusDamage > 0) {
                ActiveFlags.IS_AOE_ATTACKING.push();
                target.hurt(DamageSource.playerAttack(player), bonusDamage);
                if(damageBurst.getScaledAttribute().equals(ModGearAttributes.MANA_ADDITIVE)) {
                    if(player instanceof ManaPlayer manaPlayer) {
                        manaPlayer.decreaseMana(ManaAction.PLAYER_ACTION, manaPlayer.getManaMax() * 0.025F);
                    }
                }
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

    private static class ComparatorImpl extends VaultGearAttributeComparator<DamageBurstComboAttribute> {
        public Optional<DamageBurstComboAttribute> merge(DamageBurstComboAttribute thisValue, DamageBurstComboAttribute thatValue) {
            return Optional.of(
                new DamageBurstComboAttribute(
                    Math.max(thisValue.getRequiredHits(), thatValue.getRequiredHits()),
                    thisValue.getAssociatedEffectId(),
                    Math.max(thisValue.getEffectDuration(), thatValue.getEffectDuration()),
                    thisValue.getScaledAttribute(),
                    Math.max(thisValue.getDamageMultiplier(), thatValue.getDamageMultiplier())
                )
            );
        }

        public Optional<DamageBurstComboAttribute> difference(DamageBurstComboAttribute thisValue, DamageBurstComboAttribute thatValue) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Comparator<DamageBurstComboAttribute> getComparator() {
            return Comparator.comparing(DamageBurstComboAttribute::getDamageMultiplier)
                .thenComparing(DamageBurstComboAttribute::getRequiredHits);
        }
    }

    public static class Config extends ComboAttribute.Config {
        @Expose
        private final ResourceLocation scaledAttribute;
        @Expose
        private final FloatRollRangeEntry damageMultiplier;

        public Config(IntRollRangeEntry requiredHits, ResourceLocation associatedEffect, int effectDuration, ResourceLocation scaledAttribute, FloatRollRangeEntry damageMultiplier) {
            super(requiredHits, associatedEffect, effectDuration);
            this.scaledAttribute = scaledAttribute;
            this.damageMultiplier = damageMultiplier;
        }

        public VaultGearAttribute<?> getScaledAttribute() {
            if (this.scaledAttribute == null) {
                return null;
            }
            return VaultGearAttributeRegistry.getAttribute(this.scaledAttribute);
        }

        public ResourceLocation getScaledAttributeResource() {
            return this.scaledAttribute;
        }

        public FloatRollRangeEntry getDamageMultiplier() {
            return this.damageMultiplier;
        }
    }

    public static class Generator extends ConfigurableAttributeGenerator<DamageBurstComboAttribute, DamageBurstComboAttribute.Config> {
        @Nullable
        @Override
        public Class<DamageBurstComboAttribute.Config> getConfigurationObjectClass() {
            return DamageBurstComboAttribute.Config.class;
        }

        @Nullable
        public MutableComponent getConfigRangeDisplay(VaultGearModifierReader<DamageBurstComboAttribute> reader, DamageBurstComboAttribute.Config min, DamageBurstComboAttribute.Config max) {
            return new TextComponent(FORMAT.format(min.getDamageMultiplier().getMin() * 100.0F) + "%")
                .append("-")
                .append(FORMAT.format(max.getDamageMultiplier().getMax() * 100.0F) + "%");
        }

        @Nullable
        public MutableComponent getConfigDisplay(VaultGearModifierReader<DamageBurstComboAttribute> reader, DamageBurstComboAttribute.Config object) {
            MutableComponent range = this.getConfigRangeDisplay(reader, object);
            if (range == null) return null;

            VaultGearAttribute<?> attr = object.getScaledAttribute();
            String attrName = "stat";
            if (attr != null && attr.getReader() != null) {
                attrName = attr.getReader().getModifierName();
            } else if (object.getScaledAttributeResource() != null) {
                String path = object.getScaledAttributeResource().getPath();
                attrName = path.substring(path.lastIndexOf('/') + 1).replace('_', ' ');
            }

            return new TextComponent("")
                    .withStyle(reader.getColoredTextStyle())
                    .append("Every ")
                    .append(String.valueOf(object.getRequiredHits().getMin()))
                    .append(" hits, deals ")
                    .append(range.withStyle(reader.getColoredTextStyle()))
                    .append(" of your ")
                    .append(new TextComponent(attrName).withStyle(attr.getReader().getColoredTextStyle()))
                    .append(" as bonus damage.");
        }

        public DamageBurstComboAttribute generateRandomValue(DamageBurstComboAttribute.Config object, Random random) {
            JavaRandom rand = JavaRandom.ofScrambled(random.nextLong());
            float multiplier = object.getDamageMultiplier().getRandom(rand);
            int hits = object.getRequiredHits().getRandom(rand);
            return new DamageBurstComboAttribute(hits, object.getAssociatedEffectId(), object.getEffectDuration(), object.getScaledAttribute(), multiplier);
        }

        @Override
        public Optional<DamageBurstComboAttribute> getMinimumValue(List<DamageBurstComboAttribute.Config> configurations) {
            return configurations.stream()
                .min(Comparator.comparing(config -> config.getDamageMultiplier().getMin()))
                .map(config -> new DamageBurstComboAttribute(
                    config.getRequiredHits().getMin(),
                    config.getAssociatedEffectId(),
                    config.getEffectDuration(),
                    config.getScaledAttribute(),
                    config.getDamageMultiplier().getMin()
                ));
        }

        @Override
        public Optional<DamageBurstComboAttribute> getMaximumValue(List<DamageBurstComboAttribute.Config> configurations) {
            return configurations.stream()
                .max(Comparator.comparing(config -> config.getDamageMultiplier().getRolledMaximum()))
                .map(config -> new DamageBurstComboAttribute(
                    config.getRequiredHits().getRolledMaximum(),
                    config.getAssociatedEffectId(),
                    config.getEffectDuration(),
                    config.getScaledAttribute(),
                    config.getDamageMultiplier().getRolledMaximum()
                ));
        }

        public Optional<Float> getRollPercentage(DamageBurstComboAttribute value, List<DamageBurstComboAttribute.Config> configurations) {
            return MiscUtils.getFloatValueRange(
                value.getDamageMultiplier(),
                this.getMinimumValue(configurations),
                this.getMaximumValue(configurations),
                DamageBurstComboAttribute::getDamageMultiplier
            );
        }
    }

    private static class Reader extends VaultGearModifierReader<DamageBurstComboAttribute> {
        protected Reader() {
            super("", 14901010);
        }

        @Nullable
        @Override
        public MutableComponent getDisplay(VaultGearAttributeInstance<DamageBurstComboAttribute> instance, VaultGearModifier.AffixType type) {
            DamageBurstComboAttribute attribute = instance.getValue();
            MutableComponent multDisplay = this.getValueDisplay(attribute);
            if (multDisplay == null) return null;

            VaultGearAttribute<?> attr = attribute.getScaledAttribute();
            MutableComponent attrNameComponent;

            if (attr != null && attr.getReader() != null) {
                attrNameComponent = new TextComponent(attr.getReader().getModifierName()).withStyle(attr.getReader().getColoredTextStyle());
            } else if (attr != null) {
                attrNameComponent = new TextComponent(attr.getRegistryName().getPath().replace('_', ' '));
            } else {
                attrNameComponent = new TextComponent("stat");
            }

            return new TextComponent("")
                    .append(type.getAffixPrefixComponent(true))
                    .append("Every ")
                    .append(new TextComponent(String.valueOf(attribute.getRequiredHits())).withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" hits, deal ")
                    .append(multDisplay.withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("uniqueHighlight"))))
                    .append(" of your ")
                    .append(attrNameComponent)
                    .append(" as bonus damage")
                    .setStyle(this.getColoredTextStyle());
        }

        @Nullable
        public MutableComponent getValueDisplay(DamageBurstComboAttribute value) {
            return new TextComponent(FORMAT.format(value.getDamageMultiplier() * 100.0F) + "%");
        }

        @Override
        protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<DamageBurstComboAttribute> instance, VaultGearModifier.AffixType type) {
            DamageBurstComboAttribute attribute = instance.getValue();
            out.add(type.getAffixPrefix(attribute.getDamageMultiplier() >= 0.0F));
            out.add("Every " + attribute.getRequiredHits() + " hits, deal " + FORMAT.format(attribute.getDamageMultiplier() * 100.0F) + "% bonus damage");
        }
    }
}