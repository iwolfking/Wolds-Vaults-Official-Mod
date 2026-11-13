package xyz.iwolfking.woldsvaults.spellcraft.gear;

import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import iskallia.vault.gear.attribute.ability.special.base.template.value.IntValue;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.SteelInfernoAttribute;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.StormTrackingAttribute;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphLines;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The glyph lines converted gear affixes grant (loadout spec sec. 6): every value of a {@code gearGlyphs} attribute on
 * the stack, read the way VH's snapshot reads it, becomes one line of the entry's glyph with the affix's value as its
 * potency. On an arcana item the lines belong to that item (not transferable, not auto); on other gear they are
 * transferable auto lines. Each line allows one copy and its cost is the glyph's minimum (unique glyph costs come from
 * the spell rule). Quality places the value in the glyph's top potency tier; for Steel Core it places the rolled radius
 * multiplier in the glyph's linked radius range, so the cast reproduces both rolled values.
 */
public final class GearGlyphLines {
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private GearGlyphLines() {
    }

    /** The {@link GlyphLines} provider. */
    public static void collect(ItemStack stack, boolean arcana, List<GlyphLines.SourceLine> out) {
        for (SpellcraftItemData.Line line : lines(stack)) {
            out.add(new GlyphLines.SourceLine(line, !arcana, !arcana));
        }
    }

    /** The converted affixes on a stack as glyph lines (not transferable; the caller decides how the source lends them). */
    public static List<SpellcraftItemData.Line> lines(ItemStack stack) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null || stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return List.of();
        }
        List<FocusAugmentsConfig.GearGlyph> entries = GearGlyphs.entries(config);
        if (entries.isEmpty()) {
            return List.of();
        }
        AttributeGearData data = AttributeGearData.read(stack);
        Map<AugmentType, Float> linkedQuality = linkedQualities(config, entries, data);
        List<SpellcraftItemData.Line> lines = new ArrayList<>();
        for (FocusAugmentsConfig.GearGlyph entry : entries) {
            VaultGearAttribute<?> attribute = attribute(entry.attribute);
            AugmentType type = AugmentType.byId(entry.glyph);
            if (attribute == null || type == null || entry.linked != null) {
                continue;
            }
            for (Object value : values(data, attribute)) {
                SpellcraftItemData.Line line = line(config, entry, type, value, linkedQuality.get(type));
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /** The quality each linked affix on the item places in its glyph's linked value. */
    private static Map<AugmentType, Float> linkedQualities(FocusAugmentsConfig config, List<FocusAugmentsConfig.GearGlyph> entries, AttributeGearData data) {
        Map<AugmentType, Float> out = new HashMap<>();
        for (FocusAugmentsConfig.GearGlyph entry : entries) {
            if (entry.linked == null) {
                continue;
            }
            VaultGearAttribute<?> attribute = attribute(entry.attribute);
            AugmentType type = AugmentType.byId(entry.glyph);
            if (attribute == null || type == null) {
                continue;
            }
            FocusAugmentsConfig.Linked linked = GearGlyphs.linked(config, type, entry.linked);
            if (linked == null) {
                reportOnce("linked:" + type.id() + ":" + entry.linked, "Spellcraft: " + type.id() + " has no linked value " + entry.linked
                        + "; the " + entry.attribute + " affix that fills it is ignored");
                continue;
            }
            for (Object value : values(data, attribute)) {
                Float raw = number(entry, value);
                if (raw != null) {
                    float magnitude = entry.potency(raw);
                    out.put(type, linked.max <= linked.min ? 0.0F : Mth.clamp((magnitude - linked.min) / (linked.max - linked.min), 0.0F, 1.0F));
                }
            }
        }
        return out;
    }

    /** A converted affix modifier on a gear item, with its affix type and the glyph it grants. */
    public record Grant(VaultGearModifier<?> modifier, VaultGearModifier.AffixType affixType, AugmentType glyph) {
    }

    /** Every converted affix modifier on a gear item, in tooltip order, for the client tooltip. */
    public static List<Grant> grants(ItemStack stack) {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null || stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return List.of();
        }
        AttributeGearData raw = AttributeGearData.read(stack);
        if (!(raw instanceof VaultGearData data)) {
            return List.of();
        }
        List<FocusAugmentsConfig.GearGlyph> entries = GearGlyphs.entries(config);
        List<Grant> grants = new ArrayList<>();
        for (VaultGearModifier.AffixType affixType : VaultGearModifier.AffixType.values()) {
            for (VaultGearModifier<?> modifier : data.getModifiers(affixType)) {
                for (FocusAugmentsConfig.GearGlyph entry : entries) {
                    VaultGearAttribute<?> attribute = attribute(entry.attribute);
                    AugmentType type = AugmentType.byId(entry.glyph);
                    if (attribute == null || type == null || modifier.getAttribute() != attribute) {
                        continue;
                    }
                    Object value = modifier.getValue();
                    if (value instanceof SteelInfernoAttribute || number(entry, value) != null) {
                        grants.add(new Grant(modifier, affixType, type));
                        break;
                    }
                }
            }
        }
        return grants;
    }

    @Nullable
    private static SpellcraftItemData.Line line(FocusAugmentsConfig config, FocusAugmentsConfig.GearGlyph entry, AugmentType type, Object value,
                                                @Nullable Float linkedQuality) {
        FocusAugmentsConfig.Glyph glyph = config.glyph(type);
        float potency;
        float quality;
        if (value instanceof SteelInfernoAttribute steel) {
            potency = entry.potency(steel.getCentreBonus());
            FocusAugmentsConfig.Linked radius = GearGlyphs.linked(config, type, "radius");
            quality = radius == null || radius.max <= radius.min ? 0.0F
                    : Mth.clamp((steel.getRadiusMultiplier() - radius.min) / (radius.max - radius.min), 0.0F, 1.0F);
        } else {
            Float raw = number(entry, value);
            if (raw == null) {
                return null;
            }
            potency = value instanceof Boolean ? entry.scale : entry.potency(raw);
            if (linkedQuality != null) {
                quality = linkedQuality;
            } else {
                quality = tierQuality(glyph, potency);
                if (glyph.potency != null && glyph.potency.linked != null && !glyph.potency.linked.isEmpty() && hasLinkedEntry(config, type)) {
                    reportOnce("linked_missing:" + type.id(), "Spellcraft: an item grants " + type.id() + " without the affix that fills its linked value; "
                            + "the line's quality comes from its potency instead");
                }
            }
        }
        return new SpellcraftItemData.Line(type, potency, quality, glyph.costMin, 1, false, null);
    }

    private static boolean hasLinkedEntry(FocusAugmentsConfig config, AugmentType type) {
        for (FocusAugmentsConfig.GearGlyph entry : GearGlyphs.entries(config)) {
            if (entry.linked != null && type.id().equals(entry.glyph)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Float number(FocusAugmentsConfig.GearGlyph entry, Object value) {
        if (value instanceof StormTrackingAttribute tracking) {
            return tracking.isFlag() && tracking.getAbilityKey().equals(entry.key) ? tracking.getSpeed() : null;
        }
        if (value instanceof AbilityFloatValueAttribute keyed) {
            return keyed.getAbilityKey().equals(entry.key) ? keyed.getAmount() : null;
        }
        if (value instanceof SpecialAbilityGearAttribute<?, ?> special) {
            if (!special.getModification().getKey().toString().equals(entry.key)) {
                return null;
            }
            if (special.getValue() instanceof IntValue intValue) {
                return (float) intValue.getValue();
            }
            if (special.getValue() instanceof FloatValue floatValue) {
                return floatValue.getValue();
            }
            reportOnce("value:" + entry.key, "Spellcraft: gear glyph affix " + entry.key + " has a value of an unknown kind; it grants nothing");
            return null;
        }
        if (value instanceof Boolean flag) {
            return flag ? 1.0F : null;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        reportOnce("kind:" + entry.attribute, "Spellcraft: gear glyph attribute " + entry.attribute + " has values of kind "
                + value.getClass().getSimpleName() + ", which no reader handles; it grants nothing");
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> values(AttributeGearData data, VaultGearAttribute attribute) {
        return (List<Object>) data.get(attribute, VaultGearAttributeTypeMerger.asList());
    }

    private static float tierQuality(FocusAugmentsConfig.Glyph glyph, float potency) {
        FocusAugmentsConfig.PotencyTier top = null;
        for (FocusAugmentsConfig.PotencyTier tier : glyph.potency.tiers) {
            if (top == null || tier.minLevel > top.minLevel) {
                top = tier;
            }
        }
        if (top == null || top.max <= top.min) {
            return 0.0F;
        }
        return Mth.clamp((potency - top.min) / (top.max - top.min), 0.0F, 1.0F);
    }

    @Nullable
    private static VaultGearAttribute<?> attribute(String id) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(new ResourceLocation(id));
        if (attribute == null) {
            reportOnce("attribute:" + id, "Spellcraft: gear glyph attribute " + id + " is not registered; it grants nothing");
        }
        return attribute;
    }

    private static void reportOnce(String key, String message) {
        if (REPORTED.add(key)) {
            WoldLogger.error(message);
        }
    }
}
