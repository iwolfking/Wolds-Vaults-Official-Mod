package xyz.iwolfking.woldsvaults.spellcraft.gear;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.gear.attribute.ability.AbilityAreaOfEffectPercentAttribute;
import iskallia.vault.gear.attribute.ability.AbilityFloatValueAttribute;
import iskallia.vault.gear.attribute.ability.special.base.ConfiguredModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModification;
import iskallia.vault.gear.attribute.ability.special.base.SpecialAbilityModificationRegistry;
import iskallia.vault.gear.attribute.ability.special.base.template.FloatRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.IntRangeModification;
import iskallia.vault.gear.attribute.ability.special.base.template.value.FloatValue;
import iskallia.vault.gear.attribute.ability.special.base.template.value.IntValue;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.augments.AugmentAbility;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.FocusAugments;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.effect.DeferredReach;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.SteelInfernoAttribute;
import xyz.iwolfking.woldsvaults.spellcraft.pool.GlyphLines;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * Gear effects that became glyph grants (loadout spec sec. 6, 9.4). The table's {@code gearGlyphs} section names each
 * converted affix; its rolled value is a unique glyph line of the item ({@link GearGlyphLines}). VH and WV no longer
 * read those affixes from the attribute snapshot: the snapshot reads filter them out, and the step's active unique
 * glyphs ({@link GlyphGrants}) stand in for them, as a synthetic attribute of today's form where VH reads one (special
 * modifications, keyed area of effect) or through WV's own readers (Steel Core, Implosion, Warp Velocity). The grants in
 * effect come from the open cast scope, the deferred scope of a spell step (its frozen glyphs ride on the cast tag of
 * what it spawned or scheduled), or a toggle switched on by a spell.
 * Outside all three nothing is granted, so a plain cast sees none of the converted affixes.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GearGlyphs {
    public static final String SPECIAL_MODIFICATION = "the_vault:ability_special_modification";
    public static final String ABILITY_AREA = "the_vault:ability_area_of_effect_percent";
    public static final String ABILITY_COOLDOWN = "the_vault:ability_cooldown_percent";
    private static final String RADIUS = "radius";
    private static final Object LISTENER_OWNER = new Object();
    private static final Map<UUID, Map<String, GlyphGrants>> TOGGLES = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    private static Index index;

    private GearGlyphs() {
    }

    private record Index(FocusAugmentsConfig config, Set<String> convertedKeys, Map<AugmentType, List<FocusAugmentsConfig.GearGlyph>> byType) {
    }

    /** Registers the gear glyph line provider and the toggle listener; called once from common setup. */
    public static void init() {
        GlyphLines.register(GearGlyphLines::collect);
        CommonEvents.ABILITY_CAST.register(LISTENER_OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                noteToggle(player, data.getAbility());
            }
        });
    }

    /** The table's gear glyph entries, or an empty list (logged once) when the section is missing. */
    public static List<FocusAugmentsConfig.GearGlyph> entries(FocusAugmentsConfig config) {
        if (config.gearGlyphs == null) {
            reportOnce("missing", "Spellcraft: the focus augments table has no gearGlyphs section; converted gear affixes grant no glyphs "
                    + "and stay switched off");
            return List.of();
        }
        return config.gearGlyphs;
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
        Set<String> keys = new HashSet<>();
        Map<AugmentType, List<FocusAugmentsConfig.GearGlyph>> byType = new HashMap<>();
        for (FocusAugmentsConfig.GearGlyph entry : entries(config)) {
            keys.add(entry.attribute + "|" + entry.key);
            AugmentType type = AugmentType.byId(entry.glyph);
            if (type != null) {
                byType.computeIfAbsent(type, t -> new ArrayList<>()).add(entry);
            }
        }
        current = new Index(config, Collections.unmodifiableSet(keys), byType);
        index = current;
        return current;
    }

    /** Whether a keyed or special-modification affix of this attribute and key is converted (and so filtered from VH's reads). */
    public static boolean isConverted(String attribute, @Nullable String key) {
        Index current = index();
        return current != null && current.convertedKeys().contains(attribute + "|" + key);
    }

    /**
     * The unique glyphs in effect for an entity's ability reads: the open cast scope's step, else the deferred scope of
     * one of its spell steps (an entity it cast ticking, scheduled work), else the toggles a spell switched on. Clients and
     * other entities get none.
     */
    public static GlyphGrants grants(@Nullable Entity holder) {
        if (!(holder instanceof ServerPlayer player)) {
            return GlyphGrants.NONE;
        }
        CastScope.Active scope = CastScope.current().filter(active -> active.player() == player).orElse(null);
        if (scope != null) {
            return scope.modifiers().grants();
        }
        DeferredReach.Reach reach = DeferredReach.current(player);
        if (reach != null) {
            return reach.glyphs().modifiers().grants();
        }
        return toggleGrants(player);
    }

    /** VH's special modifications for {@code modClass}, with converted ones replaced by the granted glyphs. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List filterSpecial(Entity entity, Class<?> modClass, List<ConfiguredModification> found) {
        Index current = index();
        if (current == null) {
            return found;
        }
        List<ConfiguredModification> result = new ArrayList<>();
        for (ConfiguredModification modification : found) {
            if (!current.convertedKeys().contains(SPECIAL_MODIFICATION + "|" + modification.modification().getKey())) {
                result.add(modification);
            }
        }
        GlyphGrants grants = grants(entity);
        grants.all().forEach((type, grant) -> {
            for (FocusAugmentsConfig.GearGlyph entry : current.byType().getOrDefault(type, List.of())) {
                if (!SPECIAL_MODIFICATION.equals(entry.attribute) || entry.key == null) {
                    continue;
                }
                SpecialAbilityModification<?, ?> modification = specialModification(entry.key);
                if (modification == null || !modClass.isInstance(modification)) {
                    continue;
                }
                float value = entry.value(grant.potency());
                if (modification instanceof IntRangeModification) {
                    result.add(new ConfiguredModification(modification, new IntValue(Math.round(value))));
                } else if (modification instanceof FloatRangeModification) {
                    result.add(new ConfiguredModification(modification, new FloatValue(value)));
                } else {
                    reportOnce("value:" + entry.key, "Spellcraft: special modification " + entry.key + " takes neither an int nor a float value; "
                            + type.displayName() + " grants nothing through it");
                }
            }
        });
        return result;
    }

    @Nullable
    private static SpecialAbilityModification<?, ?> specialModification(String key) {
        try {
            return SpecialAbilityModificationRegistry.getAbilityModification(new ResourceLocation(key));
        } catch (IllegalArgumentException e) {
            reportOnce("special:" + key, "Spellcraft: gear glyph names special modification " + key + ", which is not registered; it grants nothing");
            return null;
        }
    }

    /** VH's keyed area-of-effect attributes, with converted ones replaced by the granted glyphs. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List filterArea(Entity entity, List found) {
        Index current = index();
        if (current == null) {
            return found;
        }
        List result = withoutConverted(current, ABILITY_AREA, found);
        grants(entity).all().forEach((type, grant) -> {
            for (FocusAugmentsConfig.GearGlyph entry : current.byType().getOrDefault(type, List.of())) {
                if (!ABILITY_AREA.equals(entry.attribute) || entry.key == null) {
                    continue;
                }
                if (entry.linked == null) {
                    result.add(new AbilityAreaOfEffectPercentAttribute(entry.key, entry.value(grant.potency())));
                    continue;
                }
                FocusAugmentsConfig.Linked linked = linked(current.config(), type, entry.linked);
                if (linked == null) {
                    reportOnce("area_linked:" + type.id(), "Spellcraft: " + type.id() + " has no linked value " + entry.linked
                            + "; its area change is not applied");
                    continue;
                }
                result.add(new AbilityAreaOfEffectPercentAttribute(entry.key, entry.linkedValue(linked, grant.quality())));
            }
        });
        return result;
    }

    /** VH's keyed cooldown attributes without the converted ones; their glyphs act as the spell's cooldown factor. */
    @SuppressWarnings("rawtypes")
    public static List filterCooldown(List found) {
        Index current = index();
        return current == null ? found : withoutConverted(current, ABILITY_COOLDOWN, found);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List withoutConverted(Index current, String attribute, List found) {
        List result = new ArrayList<>();
        for (Object value : found) {
            if (!(value instanceof AbilityFloatValueAttribute keyed) || !current.convertedKeys().contains(attribute + "|" + keyed.getAbilityKey())) {
                result.add(value);
            }
        }
        return result;
    }

    /** The potency of a granted glyph for the entity's reads, or null when it is not granted. */
    @Nullable
    public static GlyphGrants.Grant grant(@Nullable Entity holder, AugmentType type) {
        return grants(holder).get(type);
    }

    /**
     * Steel Core on a fireball: the centre bonus is the glyph's potency and the radius multiplier its linked
     * {@code radius} value at the line's quality. Null when the fireball's cast had no Steel Core.
     */
    @Nullable
    public static SteelInfernoAttribute steelCore(VaultFireball fireball) {
        Entity owner = fireball.getOwner();
        if (owner == null) {
            return null;
        }
        GlyphGrants.Grant grant = carried(fireball, owner).get(AugmentType.STEEL_CORE);
        FocusAugmentsConfig config = FocusAugments.config();
        if (grant == null || config == null) {
            return null;
        }
        FocusAugmentsConfig.Linked radius = linked(config, AugmentType.STEEL_CORE, RADIUS);
        if (radius == null) {
            reportOnce("steel_core_radius", "Spellcraft: Steel Core has no linked radius value in the table; its fireballs keep their radius");
            return new SteelInfernoAttribute(1.0F, grant.potency());
        }
        return new SteelInfernoAttribute(radius.min + (radius.max - radius.min) * grant.quality(), grant.potency());
    }

    /** The grants of the spell step that cast {@code entity}, when {@code owner} cast it; none otherwise. */
    public static GlyphGrants carried(@Nullable Entity entity, @Nullable Entity owner) {
        CastTag tag = CastContext.tagOf(entity);
        if (tag == null || tag.glyphs() == null || !(owner instanceof Player player) || !tag.glyphs().isOwnedBy(player)) {
            return GlyphGrants.NONE;
        }
        return tag.glyphs().modifiers().grants();
    }

    @Nullable
    public static FocusAugmentsConfig.Linked linked(FocusAugmentsConfig config, AugmentType type, String name) {
        FocusAugmentsConfig.Glyph glyph = config.glyph(type);
        if (glyph == null || glyph.potency == null || glyph.potency.linked == null) {
            return null;
        }
        for (FocusAugmentsConfig.Linked linked : glyph.potency.linked) {
            if (name.equals(linked.name)) {
                return linked;
            }
        }
        return null;
    }

    private static void noteToggle(ServerPlayer player, Ability ability) {
        Skill spec = ability.getParent();
        CastScope.Active scope = CastScope.current().filter(active -> active.player() == player).orElse(null);
        if (spec == null || scope == null) {
            return;
        }
        GlyphGrants grants = scope.modifiers().grants();
        Map<String, GlyphGrants> toggles = TOGGLES.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
        if (grants.isEmpty()) {
            toggles.remove(spec.getId());
        } else {
            toggles.put(spec.getId(), grants);
        }
    }

    private static GlyphGrants toggleGrants(ServerPlayer player) {
        Map<String, GlyphGrants> toggles = TOGGLES.get(player.getUUID());
        if (toggles == null || toggles.isEmpty()) {
            return GlyphGrants.NONE;
        }
        GlyphGrants merged = GlyphGrants.NONE;
        for (Map.Entry<String, GlyphGrants> entry : toggles.entrySet()) {
            if (!toggleActive(player, entry.getKey())) {
                toggles.remove(entry.getKey());
            } else if (merged.isEmpty()) {
                merged = entry.getValue();
            } else {
                merged = GlyphGrants.merge(merged, entry.getValue());
            }
        }
        return merged;
    }

    private static boolean toggleActive(ServerPlayer player, String spec) {
        return AugmentAbility.serverTree(player).getForId(spec)
                .filter(TieredSkill.class::isInstance)
                .map(TieredSkill.class::cast)
                .filter(TieredSkill::isUnlocked)
                .map(TieredSkill::getChild)
                .filter(Ability.class::isInstance)
                .map(Ability.class::cast)
                .map(Ability::isActive)
                .orElse(false);
    }

    private static void reportOnce(String key, String message) {
        if (REPORTED.add(key)) {
            WoldLogger.error(message);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TOGGLES.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TOGGLES.clear();
        index = null;
    }
}
