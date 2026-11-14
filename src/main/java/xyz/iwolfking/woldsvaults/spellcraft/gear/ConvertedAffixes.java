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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.modifiers.gear.unique.SteelInfernoAttribute;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.GlyphGrants;
import xyz.iwolfking.woldsvaults.spellcraft.source.GearAffixLines;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphLineProviders;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphQuality;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gear affixes that became glyph grants. Each affix in the table's {@code gearGlyphs} section is a unique glyph line of
 * its item, the attribute snapshot reads filter it out, and the step's active unique glyphs ({@link GlyphGrants}) stand
 * in for it. The grants in effect come from the open cast scope, the deferred scope of a spell step, or a toggle a
 * spell switched on, so a plain cast sees none of the converted affixes.
 */
public final class ConvertedAffixes {
    private static final String SPECIAL_MODIFICATION = "the_vault:ability_special_modification";
    private static final String ABILITY_AREA = "the_vault:ability_area_of_effect_percent";
    private static final String ABILITY_COOLDOWN = "the_vault:ability_cooldown_percent";
    private static final String RADIUS = "radius";
    private static final Object LISTENER_OWNER = new Object();
    private static final Map<UUID, Map<String, GlyphGrants>> TOGGLES = new ConcurrentHashMap<>();

    private ConvertedAffixes() {
    }

    public static void init() {
        GlyphLineProviders.register(GearAffixLines::collect);
        CommonEvents.ABILITY_CAST.register(LISTENER_OWNER, data -> {
            if (data.getEntity() instanceof ServerPlayer player) {
                noteToggle(player, data.getAbility());
            }
        });
    }

    /**
     * The unique glyphs in effect for an entity's ability reads: the open cast scope's step, else the deferred scope of
     * one of its spell steps, else the toggles a spell switched on. Clients and other entities get none.
     */
    public static GlyphGrants grants(@Nullable Entity holder) {
        if (!(holder instanceof ServerPlayer player)) {
            return GlyphGrants.NONE;
        }
        GlyphGrants cast = ActiveGrants.ofCast(player);
        return cast != null ? cast : toggleGrants(player);
    }

    /** VH's special modifications for {@code modClass}, with converted ones replaced by the granted glyphs. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List filterSpecial(Entity entity, Class<?> modClass, List<ConfiguredModification> found) {
        GearAffixLines.Index current = GearAffixLines.index();
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
            for (SpellcraftConfig.GearGlyph entry : current.byType().getOrDefault(type, List.of())) {
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
                    WoldLogger.errorOnce("ConvertedAffixes:value:" + entry.key, "Spellcraft: special modification " + entry.key + " takes neither an int nor a float value; "
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
            WoldLogger.errorOnce("ConvertedAffixes:special:" + key, "Spellcraft: gear glyph names special modification " + key + ", which is not registered; it grants nothing");
            return null;
        }
    }

    /** VH's keyed area-of-effect attributes, with converted ones replaced by the granted glyphs. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List filterArea(Entity entity, List found) {
        GearAffixLines.Index current = GearAffixLines.index();
        if (current == null) {
            return found;
        }
        List result = withoutConverted(current, ABILITY_AREA, found);
        grants(entity).all().forEach((type, grant) -> {
            for (SpellcraftConfig.GearGlyph entry : current.byType().getOrDefault(type, List.of())) {
                if (!ABILITY_AREA.equals(entry.attribute) || entry.key == null) {
                    continue;
                }
                if (entry.linked == null) {
                    result.add(new AbilityAreaOfEffectPercentAttribute(entry.key, entry.value(grant.potency())));
                    continue;
                }
                SpellcraftConfig.Linked linked = GearAffixLines.linked(current.config(), type, entry.linked);
                if (linked == null) {
                    WoldLogger.errorOnce("ConvertedAffixes:area_linked:" + type.id(), "Spellcraft: " + type.id() + " has no linked value " + entry.linked
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
        GearAffixLines.Index current = GearAffixLines.index();
        return current == null ? found : withoutConverted(current, ABILITY_COOLDOWN, found);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List withoutConverted(GearAffixLines.Index current, String attribute, List found) {
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
    public static GlyphGrants.Grant grant(@Nullable Entity holder, GlyphType type) {
        return grants(holder).get(type);
    }

    /**
     * Steel Core on a fireball: the centre bonus is the glyph's potency and the radius multiplier its linked {@code
     * radius} value at the line's quality. Null when the fireball's cast had no Steel Core.
     */
    @Nullable
    public static SteelInfernoAttribute steelCore(VaultFireball fireball) {
        Entity owner = fireball.getOwner();
        if (owner == null) {
            return null;
        }
        GlyphGrants.Grant grant = carried(fireball, owner).get(GlyphType.STEEL_CORE);
        SpellcraftConfig config = ModConfigs.SPELLCRAFT;
        if (grant == null || config == null) {
            return null;
        }
        SpellcraftConfig.Linked radius = GearAffixLines.linked(config, GlyphType.STEEL_CORE, RADIUS);
        if (radius == null) {
            WoldLogger.errorOnce("ConvertedAffixes:steel_core_radius", "Spellcraft: Steel Core has no linked radius value in the table; its fireballs keep their radius");
            return new SteelInfernoAttribute(1.0F, grant.potency());
        }
        return new SteelInfernoAttribute(GlyphQuality.linkedValue(radius, grant.quality()), grant.potency());
    }

    /** The grants of the spell step that cast {@code entity}, when {@code owner} cast it; none otherwise. */
    public static GlyphGrants carried(@Nullable Entity entity, @Nullable Entity owner) {
        CastTag tag = CastContext.tagOf(entity);
        if (tag == null || tag.glyphs() == null || !(owner instanceof Player player) || !tag.glyphs().isOwnedBy(player)) {
            return GlyphGrants.NONE;
        }
        return tag.glyphs().modifiers().grants();
    }

    private static void noteToggle(ServerPlayer player, Ability ability) {
        Skill spec = ability.getParent();
        CastScope.Active scope = CastScope.forPlayer(player);
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

    static GlyphGrants toggleGrants(ServerPlayer player) {
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
        return AbilityLookup.unlockedChild(AbilityLookup.tree(player), spec)
                .filter(Ability.class::isInstance)
                .map(Ability.class::cast)
                .map(Ability::isActive)
                .orElse(false);
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        TOGGLES.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        TOGGLES.clear();
        GearAffixLines.resetIndex();
    }
}
