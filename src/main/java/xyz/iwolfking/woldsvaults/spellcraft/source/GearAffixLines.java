package xyz.iwolfking.woldsvaults.spellcraft.source;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityGearAttribute;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import iskallia.vault.gear.attribute.ability.special.base.template.value.IntValue;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.SteelInfernoAttribute;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.StormTrackingAttribute;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The glyph lines converted gear affixes grant: every value of a {@code gearGlyphs} attribute on the stack, read the
 * way VH's snapshot reads it, becomes one line of the entry's glyph with the affix's value as its potency. On an arcana
 * item the lines belong to that item, and on other gear they are transferable auto lines. Each line allows one copy and
 * costs the glyph's minimum, and its quality places the value in the glyph's top potency tier and a linked affix's
 * value in its linked range.
 */
public final class GearAffixLines {
    private static Index index;

    private GearAffixLines() {
    }

    public record Index(SpellcraftConfig config, Set<String> convertedKeys, Map<GlyphType, List<SpellcraftConfig.GearGlyph>> byType) {
    }

    /** The table's gear glyph entries, or an empty list (logged once) when the section is missing. */
    public static List<SpellcraftConfig.GearGlyph> entries(SpellcraftConfig config) {
        if (config.gearGlyphs == null) {
            WoldLogger.errorOnce("ConvertedAffixes:missing", "Spellcraft: the spellcraft table has no gearGlyphs section; converted gear affixes grant no glyphs "
                    + "and stay switched off");
            return List.of();
        }
        return config.gearGlyphs;
    }

    @Nullable
    public static Index index() {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null) {
            return null;
        }
        Index current = index;
        if (current != null && current.config() == config) {
            return current;
        }
        Set<String> keys = new HashSet<>();
        Map<GlyphType, List<SpellcraftConfig.GearGlyph>> byType = new HashMap<>();
        for (SpellcraftConfig.GearGlyph entry : entries(config)) {
            keys.add(entry.attribute + "|" + entry.key);
            GlyphType type = GlyphType.byId(entry.glyph);
            if (type != null) {
                byType.computeIfAbsent(type, t -> new ArrayList<>()).add(entry);
            }
        }
        current = new Index(config, Collections.unmodifiableSet(keys), byType);
        index = current;
        return current;
    }

    public static void resetIndex() {
        index = null;
    }

    @Nullable
    public static SpellcraftConfig.Linked linked(SpellcraftConfig config, GlyphType type, String name) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        if (glyph == null || glyph.potency == null || glyph.potency.linked == null) {
            return null;
        }
        for (SpellcraftConfig.Linked linked : glyph.potency.linked) {
            if (name.equals(linked.name)) {
                return linked;
            }
        }
        return null;
    }

    public static void collect(ItemStack stack, boolean arcana, List<GlyphLineProviders.SourceLine> out) {
        for (GlyphLine line : lines(stack)) {
            out.add(new GlyphLineProviders.SourceLine(line, !arcana, !arcana));
        }
    }

    /**
     * The converted affixes on a stack as glyph lines (not transferable; the caller decides how the source lends them).
     */
    public static List<GlyphLine> lines(ItemStack stack) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null || stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return List.of();
        }
        List<SpellcraftConfig.GearGlyph> entries = entries(config);
        if (entries.isEmpty()) {
            return List.of();
        }
        AttributeGearData data = AttributeGearData.read(stack);
        Map<GlyphType, Float> linkedQuality = linkedQualities(config, entries, data);
        List<GlyphLine> lines = new ArrayList<>();
        for (SpellcraftConfig.GearGlyph entry : entries) {
            VaultGearAttribute<?> attribute = attribute(entry.attribute);
            GlyphType type = GlyphType.byId(entry.glyph);
            if (attribute == null || type == null || entry.linked != null) {
                continue;
            }
            for (Object value : values(data, attribute)) {
                GlyphLine line = line(config, entry, type, value, linkedQuality.get(type));
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private static Map<GlyphType, Float> linkedQualities(SpellcraftConfig config, List<SpellcraftConfig.GearGlyph> entries, AttributeGearData data) {
        Map<GlyphType, Float> out = new HashMap<>();
        for (SpellcraftConfig.GearGlyph entry : entries) {
            if (entry.linked == null) {
                continue;
            }
            VaultGearAttribute<?> attribute = attribute(entry.attribute);
            GlyphType type = GlyphType.byId(entry.glyph);
            if (attribute == null || type == null) {
                continue;
            }
            SpellcraftConfig.Linked linked = linked(config, type, entry.linked);
            if (linked == null) {
                WoldLogger.errorOnce("GearAffixLines:linked:" + type.id() + ":" + entry.linked, "Spellcraft: " + type.id() + " has no linked value " + entry.linked
                        + "; the " + entry.attribute + " affix that fills it is ignored");
                continue;
            }
            for (Object value : values(data, attribute)) {
                Float raw = number(entry, value);
                if (raw != null) {
                    float magnitude = entry.potency(raw);
                    out.put(type, GlyphQuality.linkedQuality(linked, magnitude));
                }
            }
        }
        return out;
    }

    /** A converted affix modifier on a gear item, with its affix type and the glyph it grants. */
    public record Grant(VaultGearModifier<?> modifier, VaultGearModifier.AffixType affixType, GlyphType glyph) {
    }

    /** Every converted affix modifier on a gear item, in tooltip order, for the client tooltip. */
    public static List<Grant> grants(ItemStack stack) {
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (config == null || stack.isEmpty() || !AttributeGearData.hasData(stack)) {
            return List.of();
        }
        AttributeGearData raw = AttributeGearData.read(stack);
        if (!(raw instanceof VaultGearData data)) {
            return List.of();
        }
        List<SpellcraftConfig.GearGlyph> entries = entries(config);
        List<Grant> grants = new ArrayList<>();
        for (VaultGearModifier.AffixType affixType : VaultGearModifier.AffixType.values()) {
            for (VaultGearModifier<?> modifier : data.getModifiers(affixType)) {
                for (SpellcraftConfig.GearGlyph entry : entries) {
                    VaultGearAttribute<?> attribute = attribute(entry.attribute);
                    GlyphType type = GlyphType.byId(entry.glyph);
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
    private static GlyphLine line(SpellcraftConfig config, SpellcraftConfig.GearGlyph entry, GlyphType type, Object value,
                                                @Nullable Float linkedQuality) {
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        float potency;
        float quality;
        if (value instanceof SteelInfernoAttribute steel) {
            potency = entry.potency(steel.getCentreBonus());
            SpellcraftConfig.Linked radius = linked(config, type, "radius");
            quality = GlyphQuality.linkedQuality(radius, steel.getRadiusMultiplier());
        } else {
            Float raw = number(entry, value);
            if (raw == null) {
                return null;
            }
            potency = value instanceof Boolean ? entry.scale : entry.potency(raw);
            if (linkedQuality != null) {
                quality = linkedQuality;
            } else {
                quality = GlyphQuality.tierQuality(glyph, potency);
                if (glyph.potency != null && glyph.potency.linked != null && !glyph.potency.linked.isEmpty() && hasLinkedEntry(config, type)) {
                    WoldLogger.errorOnce("GearAffixLines:linked_missing:" + type.id(), "Spellcraft: an item grants " + type.id() + " without the affix that fills its linked value; "
                            + "the line's quality comes from its potency instead");
                }
            }
        }
        return new GlyphLine(type, potency, quality, glyph.costMin, 1, false, null);
    }

    private static boolean hasLinkedEntry(SpellcraftConfig config, GlyphType type) {
        for (SpellcraftConfig.GearGlyph entry : entries(config)) {
            if (entry.linked != null && type.id().equals(entry.glyph)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Float number(SpellcraftConfig.GearGlyph entry, Object value) {
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
            WoldLogger.errorOnce("GearAffixLines:value:" + entry.key, "Spellcraft: gear glyph affix " + entry.key + " has a value of an unknown kind; it grants nothing");
            return null;
        }
        if (value instanceof Boolean flag) {
            return flag ? 1.0F : null;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        WoldLogger.errorOnce("GearAffixLines:kind:" + entry.attribute, "Spellcraft: gear glyph attribute " + entry.attribute + " has values of kind "
                + value.getClass().getSimpleName() + ", which no reader handles; it grants nothing");
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<Object> values(AttributeGearData data, VaultGearAttribute attribute) {
        return (List<Object>) data.get(attribute, VaultGearAttributeTypeMerger.asList());
    }

    @Nullable
    private static VaultGearAttribute<?> attribute(String id) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(new ResourceLocation(id));
        if (attribute == null) {
            WoldLogger.errorOnce("GearAffixLines:attribute:" + id, "Spellcraft: gear glyph attribute " + id + " is not registered; it grants nothing");
        }
        return attribute;
    }
}
