package xyz.iwolfking.woldsvaults.spellcraft.gear;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.config.gear.EtchingTierConfig;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.Throuple;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.effect.DeferredReach;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphLines;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphSources;
import xyz.iwolfking.woldsvaults.spellcraft.pool.ViewLine;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Etchings that grant unique glyphs (the table's {@code etchingGlyphs}). An etching on any equipped source is a
 * transferable auto line of its glyph, so plain base-bar casts of the ability it is locked to carry it with no arcana
 * item, and arcana spells may take it like any other unique glyph. The etching itself no longer acts: every read of
 * the etching (VH's {@code EtchingHelper.getEtchings}, WV's {@code WoldEtchingHelper.hasEtching}) gets the active glyph
 * instead, rebuilt as an etching value from the glyph's potency and quality, or nothing when the glyph is not active.
 * The glyph in effect is the open cast scope's step, else the deferred scope of one of the player's spell steps, else
 * a toggle a spell switched on, else the player's best auto line, which covers reads that happen outside any cast
 * (an expiring Mana Shield, a kill, a hit taken). Etchings on the table's removed list read as absent everywhere.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class EtchingGlyphs {
    private static final String POTENCY = "potency";
    private static final String LINKED = "linked";
    private static final String CHOICE = "choice";
    private static final String FLAG = "flag";
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static Index index;

    private EtchingGlyphs() {
    }

    private record Entry(FocusAugmentsConfig.EtchingGlyph config, AugmentType type) {
    }

    private record Index(FocusAugmentsConfig config, Map<String, Entry> byAttribute, Set<String> removed) {
    }

    /** Registers the etching glyph line provider; called once from common setup. */
    public static void init() {
        GlyphLines.register(EtchingGlyphs::collect);
    }

    @Nullable
    private static Index index() {
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return null;
        }
        Index current = index;
        if (current != null && current.config() == config) {
            return current;
        }
        Map<String, Entry> byAttribute = new HashMap<>();
        if (config.etchingGlyphs == null) {
            reportOnce("missing", "Spellcraft: the focus augments table has no etchingGlyphs section; etchings act on their own and grant no glyphs");
        } else {
            for (FocusAugmentsConfig.EtchingGlyph entry : config.etchingGlyphs) {
                AugmentType type = AugmentType.byId(entry.glyph);
                if (type != null) {
                    byAttribute.put(entry.attribute, new Entry(entry, type));
                }
            }
        }
        Set<String> removed = config.removedEtchings == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(config.removedEtchings));
        current = new Index(config, Collections.unmodifiableMap(byAttribute), removed);
        index = current;
        return current;
    }

    /**
     * The etching's own description filled with the value a glyph of this potency and quality stands for, or null when
     * the type is not an etching glyph or its etching config is missing (logged once). Used for glyph tooltips, so an
     * etching glyph reads exactly like its etching.
     */
    @Nullable
    public static String description(AugmentType type, float potency, float quality) {
        Index current = index();
        if (current == null) {
            return null;
        }
        Entry entry = null;
        for (Entry candidate : current.byAttribute().values()) {
            if (candidate.type() == type) {
                entry = candidate;
                break;
            }
        }
        if (entry == null) {
            return null;
        }
        VaultEtchingConfig.EtchingEntry etching = etchingConfig(entry.config().attribute);
        if (etching == null) {
            reportOnce("description:" + entry.config().attribute, "Spellcraft: no etching config describes " + entry.config().attribute
                    + "; glyph " + type.id() + " shows its potency instead of the etching text");
            return null;
        }
        Object value = value(current.config(), entry, new GlyphGrants.Grant(potency, quality));
        return EtchingHelper.formatDescription(etching.getDescription(), Optional.ofNullable(value));
    }

    @Nullable
    private static VaultEtchingConfig.EtchingEntry etchingConfig(String attribute) {
        if (ModConfigs.ETCHINGS == null) {
            return null;
        }
        ResourceLocation id = new ResourceLocation(attribute);
        VaultEtchingConfig.EtchingEntry direct = ModConfigs.ETCHINGS.getEtchingConfig(id);
        if (direct != null) {
            return direct;
        }
        for (VaultEtchingConfig.EtchingEntry entry : ModConfigs.ETCHINGS.getEtchingConfigs()) {
            if (entry.getAttributes() == null) {
                continue;
            }
            for (EtchingTierConfig.EtchingModifierTierGroup group : entry.getAttributes()) {
                if (id.equals(group.getAttribute())) {
                    return entry;
                }
            }
        }
        return null;
    }

    /** The glyph an item's etching grants, or null when it has no etching or its etching grants none (client tooltips). */
    @Nullable
    public static AugmentType glyphOf(ItemStack stack) {
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

    /** The {@link GlyphLines} provider: every converted etching on the stack is a transferable auto line, whatever the source. */
    public static void collect(ItemStack stack, boolean arcana, List<GlyphLines.SourceLine> out) {
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
                SpellcraftItemData.Line line = line(current.config(), entry, instance.getValue());
                if (line != null) {
                    out.add(new GlyphLines.SourceLine(line, true, true));
                }
            }
        }
    }

    @Nullable
    private static SpellcraftItemData.Line line(FocusAugmentsConfig config, Entry entry, Object value) {
        List<Object> components = components(value);
        List<FocusAugmentsConfig.EtchingPart> parts = entry.config().parts;
        if (components.size() != parts.size()) {
            reportOnce("shape:" + entry.config().attribute, "Spellcraft: etching " + entry.config().attribute + " has a value of " + components.size()
                    + " parts but its glyph entry reads " + parts.size() + "; it grants nothing");
            return null;
        }
        FocusAugmentsConfig.Glyph glyph = config.glyph(entry.type());
        Float potency = null;
        Float quality = null;
        for (int i = 0; i < parts.size(); i++) {
            FocusAugmentsConfig.EtchingPart part = parts.get(i);
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
                    FocusAugmentsConfig.Linked linked = GearGlyphs.linked(config, entry.type(), part.linked);
                    quality = linked == null || linked.max <= linked.min ? 0.0F
                            : Mth.clamp((number.floatValue() - linked.min) / (linked.max - linked.min), 0.0F, 1.0F);
                }
                case CHOICE -> {
                    List<String> choices = entry.config().choices;
                    int choice = choices.indexOf(String.valueOf(component));
                    if (choice < 0) {
                        reportOnce("choice:" + entry.config().attribute + ":" + component, "Spellcraft: etching " + entry.config().attribute + " rolled "
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
        return new SpellcraftItemData.Line(entry.type(), potency, quality != null ? quality : tierQuality(glyph, potency), glyph.costMin, 1, false, null);
    }

    @Nullable
    private static SpellcraftItemData.Line mismatch(Entry entry, int index, Object component) {
        reportOnce("part:" + entry.config().attribute + ":" + index, "Spellcraft: etching " + entry.config().attribute + " part " + index + " is "
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

    /**
     * VH's etching read with converted etchings replaced by the active glyph: one rebuilt etching value, or none. Removed
     * etchings read as absent. Returns null when the read is left as it is (an etching that is not converted, a client
     * or a non-player entity).
     */
    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List replace(LivingEntity entity, VaultGearAttribute<?> attribute) {
        Index current = index();
        if (current == null || attribute.getRegistryName() == null) {
            return null;
        }
        String id = attribute.getRegistryName().toString();
        if (current.removed().contains(id)) {
            return new ArrayList<>();
        }
        Entry entry = current.byAttribute().get(id);
        if (entry == null || !(entity instanceof ServerPlayer player)) {
            return null;
        }
        List result = new ArrayList<>();
        GlyphGrants.Grant grant = grant(player, entry.type());
        if (grant != null) {
            Object value = value(current.config(), entry, grant);
            if (value != null) {
                result.add(new VaultGearAttributeInstance(attribute, value));
            }
        }
        return result;
    }

    /** WV's etching check for a converted or removed etching, or null when the check is left as it is. */
    @Nullable
    public static Boolean has(LivingEntity entity, VaultGearAttribute<?> attribute) {
        List<?> replaced = replace(entity, attribute);
        return replaced == null ? null : !replaced.isEmpty();
    }

    /**
     * The glyph in effect for an etching read: the open cast scope's step, the deferred scope of a spell step, a toggle
     * a spell switched on, else the player's best auto line of the type.
     */
    @Nullable
    public static GlyphGrants.Grant grant(ServerPlayer player, AugmentType type) {
        CastScope.Active scope = CastScope.current().filter(active -> active.player() == player).orElse(null);
        if (scope != null) {
            return scope.modifiers().grants().get(type);
        }
        DeferredReach.Reach reach = DeferredReach.current(player);
        if (reach != null) {
            return reach.glyphs().modifiers().grants().get(type);
        }
        GlyphGrants.Grant toggled = GearGlyphs.grants(player).get(type);
        if (toggled != null) {
            return toggled;
        }
        FocusAugmentsConfig config = FocusAugments.config();
        if (config == null) {
            return null;
        }
        ViewLine line = GlyphSources.get(player).autoLines(config).get(type);
        return line == null ? null : new GlyphGrants.Grant(line.potency(), line.quality());
    }

    @Nullable
    private static Object value(FocusAugmentsConfig config, Entry entry, GlyphGrants.Grant grant) {
        List<FocusAugmentsConfig.EtchingPart> parts = entry.config().parts;
        Object[] components = new Object[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            FocusAugmentsConfig.EtchingPart part = parts.get(i);
            switch (part.role) {
                case POTENCY -> components[i] = number(part, entry.config().value(grant.potency()));
                case FLAG -> components[i] = Boolean.TRUE;
                case LINKED -> {
                    FocusAugmentsConfig.Linked linked = GearGlyphs.linked(config, entry.type(), part.linked);
                    if (linked == null) {
                        reportOnce("linked:" + entry.type().id(), "Spellcraft: " + entry.type().displayName() + " has no linked value " + part.linked
                                + "; its etching acts as absent");
                        return null;
                    }
                    components[i] = number(part, linked.min + (linked.max - linked.min) * grant.quality());
                }
                case CHOICE -> {
                    List<String> choices = entry.config().choices;
                    int choice = choices.size() == 1 ? 0 : Mth.clamp(Math.round(grant.quality() * (choices.size() - 1)), 0, choices.size() - 1);
                    components[i] = choices.get(choice);
                }
                default -> {
                    return null;
                }
            }
        }
        return switch (components.length) {
            case 1 -> components[0];
            case 2 -> Pair.of(components[0], components[1]);
            default -> Throuple.of(components[0], components[1], components[2]);
        };
    }

    private static Object number(FocusAugmentsConfig.EtchingPart part, float value) {
        if (part.isInt()) {
            return Integer.valueOf(Math.round(value));
        }
        return Float.valueOf(value);
    }

    @Nullable
    private static VaultGearAttribute<?> attribute(String id) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(new ResourceLocation(id));
        if (attribute == null) {
            reportOnce("attribute:" + id, "Spellcraft: etching glyph attribute " + id + " is not registered; it grants nothing");
        }
        return attribute;
    }

    private static void reportOnce(String key, String message) {
        if (REPORTED.add(key)) {
            WoldsVaults.LOGGER.error(message);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        index = null;
    }
}
