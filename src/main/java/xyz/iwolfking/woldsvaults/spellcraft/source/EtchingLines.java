package xyz.iwolfking.woldsvaults.spellcraft.source;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.Throuple;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EtchingLines {
    public static final String POTENCY = "potency";
    public static final String LINKED = "linked";
    public static final String CHOICE = "choice";
    public static final String FLAG = "flag";
    private static Index index;

    private EtchingLines() {
    }

    public record Entry(SpellcraftConfig.EtchingGlyph config, GlyphType type) {
    }

    public record Index(SpellcraftConfig config, Map<String, Entry> byAttribute, Map<GlyphType, Entry> byType, Set<String> removed) {
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
        Map<String, Entry> byAttribute = new HashMap<>();
        Map<GlyphType, Entry> byType = new HashMap<>();
        if (config.etchingGlyphs == null) {
            WoldLogger.errorOnce("ConvertedEtchings:missing", "Spellcraft: the spellcraft table has no etchingGlyphs section; etchings act on their own and grant no glyphs");
        } else {
            for (SpellcraftConfig.EtchingGlyph entry : config.etchingGlyphs) {
                GlyphType type = GlyphType.byId(entry.glyph);
                if (type != null) {
                    Entry indexed = new Entry(entry, type);
                    byAttribute.put(entry.attribute, indexed);
                    byType.put(type, indexed);
                }
            }
        }
        Set<String> removed = config.removedEtchings == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(config.removedEtchings));
        current = new Index(config, Collections.unmodifiableMap(byAttribute), Collections.unmodifiableMap(byType), removed);
        index = current;
        return current;
    }

    public static void resetIndex() {
        index = null;
    }

    /**
     * The glyph an item's etching grants, or null when it has no etching or its etching grants none (client tooltips).
     */
    @Nullable
    public static GlyphType glyphOf(ItemStack stack) {
        Index current = index();
        if (current == null || current.byAttribute().isEmpty() || stack.isEmpty() || !VaultGearData.hasData(stack)
                || !(AttributeGearData.read(stack) instanceof VaultGearData data)) {
            return null;
        }
        ResourceLocation etching = data.getFirstValue(ModGearAttributes.ETCHING).orElse(null);
        if (etching == null) {
            return null;
        }
        Entry entry = current.byAttribute().get(etching.toString());
        return entry == null ? null : entry.type();
    }

    /**
     * The {@link GlyphLineProviders} provider: every converted etching on the stack is a transferable auto line,
     * whatever the source.
     */
    public static void collect(ItemStack stack, boolean arcana, List<GlyphLineProviders.SourceLine> out) {
        Index current = index();
        if (current == null || current.byAttribute().isEmpty() || stack.isEmpty() || !VaultGearData.hasData(stack)
                || !(AttributeGearData.read(stack) instanceof VaultGearData data)) {
            return;
        }
        for (Entry entry : current.byAttribute().values()) {
            VaultGearAttribute<?> attribute = attribute(entry.config().attribute);
            if (attribute == null) {
                continue;
            }
            for (VaultGearAttributeInstance<?> instance : data.getModifiers(attribute, VaultGearData.Type.ALL)) {
                GlyphLine line = line(current.config(), entry, instance.getValue());
                if (line != null) {
                    out.add(new GlyphLineProviders.SourceLine(line, true, true));
                }
            }
        }
    }

    @Nullable
    private static GlyphLine line(SpellcraftConfig config, Entry entry, Object value) {
        List<Object> components = components(value);
        List<SpellcraftConfig.EtchingPart> parts = entry.config().parts;
        if (components.size() != parts.size()) {
            WoldLogger.errorOnce("ConvertedEtchings:shape:" + entry.config().attribute, "Spellcraft: etching " + entry.config().attribute + " has a value of " + components.size()
                    + " parts but its glyph entry reads " + parts.size() + "; it grants nothing");
            return null;
        }
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(entry.type());
        Float potency = null;
        Float quality = null;
        for (int i = 0; i < parts.size(); i++) {
            SpellcraftConfig.EtchingPart part = parts.get(i);
            Object component = components.get(i);
            switch (part.role) {
                case POTENCY -> {
                    if (!(component instanceof Number number)) {
                        return mismatch(entry, i, component);
                    }
                    potency = entry.config().potency(number.floatValue());
                }
                case FLAG -> {
                    if (!(component instanceof Boolean flag)) {
                        return mismatch(entry, i, component);
                    }
                    if (!flag) {
                        return null;
                    }
                    potency = entry.config().potency(1.0F);
                }
                case LINKED -> {
                    if (!(component instanceof Number number)) {
                        return mismatch(entry, i, component);
                    }
                    SpellcraftConfig.Linked linked = GearAffixLines.linked(config, entry.type(), part.linked);
                    quality = GlyphQuality.linkedQuality(linked, number.floatValue());
                }
                case CHOICE -> {
                    List<String> choices = entry.config().choices;
                    int choice = choices.indexOf(String.valueOf(component));
                    if (choice < 0) {
                        WoldLogger.errorOnce("ConvertedEtchings:choice:" + entry.config().attribute + ":" + component, "Spellcraft: etching " + entry.config().attribute + " rolled "
                                + component + ", which its glyph entry does not list; it grants nothing");
                        return null;
                    }
                    if (choices.size() > 1) {
                        quality = choice / (float) (choices.size() - 1);
                    }
                }
                default -> {
                    return mismatch(entry, i, component);
                }
            }
        }
        if (potency == null) {
            return null;
        }
        return new GlyphLine(entry.type(), potency, quality != null ? quality : GlyphQuality.tierQuality(glyph, potency), glyph.costMin, 1, false, null);
    }

    @Nullable
    private static GlyphLine mismatch(Entry entry, int index, Object component) {
        WoldLogger.errorOnce("ConvertedEtchings:part:" + entry.config().attribute + ":" + index, "Spellcraft: etching " + entry.config().attribute + " part " + index + " is "
                + (component == null ? "missing" : component.getClass().getSimpleName()) + ", which its glyph entry cannot read; it grants nothing");
        return null;
    }

    private static List<Object> components(Object value) {
        if (value instanceof Pair<?, ?> pair) {
            return Arrays.asList(pair.getFirst(), pair.getSecond());
        }
        if (value instanceof Throuple<?, ?, ?> throuple) {
            return Arrays.asList(throuple.getFirst(), throuple.getSecond(), throuple.getThird());
        }
        return Collections.singletonList(value);
    }

    @Nullable
    private static VaultGearAttribute<?> attribute(String id) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(new ResourceLocation(id));
        if (attribute == null) {
            WoldLogger.errorOnce("ConvertedEtchings:attribute:" + id, "Spellcraft: etching glyph attribute " + id + " is not registered; it grants nothing");
        }
        return attribute;
    }
}
