package xyz.iwolfking.woldsvaults.spellcraft.gear;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.config.gear.EtchingTierConfig;
import iskallia.vault.config.gear.VaultEtchingConfig;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.util.Throuple;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.buff.LingeringBuffs;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.scope.CastScope;
import xyz.iwolfking.woldsvaults.spellcraft.scope.GlyphGrants;
import xyz.iwolfking.woldsvaults.spellcraft.source.EtchingLines;
import xyz.iwolfking.woldsvaults.spellcraft.source.GearAffixLines;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphLineProviders;
import xyz.iwolfking.woldsvaults.spellcraft.source.GlyphQuality;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Etchings that grant unique glyphs (the table's {@code etchingGlyphs}). An etching on any equipped source is a
 * transferable auto line of its glyph, and every read of the etching gets the active glyph instead, rebuilt as an
 * etching value, or nothing when the glyph is not active. Etchings on the table's removed list read as absent
 * everywhere.
 */
public final class ConvertedEtchings {
    private ConvertedEtchings() {
    }

    public static void init() {
        GlyphLineProviders.register(EtchingLines::collect);
    }

    /**
     * The etching's own description filled with the value a glyph of this potency and quality stands for, so an etching
     * glyph's tooltip reads like its etching. Null when the type is not an etching glyph or its etching config is
     * missing (logged once).
     */
    @Nullable
    public static String description(GlyphType type, float potency, float quality) {
        EtchingLines.Index current = EtchingLines.index();
        if (current == null) {
            return null;
        }
        EtchingLines.Entry entry = null;
        for (EtchingLines.Entry candidate : current.byAttribute().values()) {
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
            WoldLogger.errorOnce("ConvertedEtchings:description:" + entry.config().attribute, "Spellcraft: no etching config describes " + entry.config().attribute
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

    /**
     * VH's etching read with converted etchings replaced by the active glyph, rebuilt as one etching value, and removed
     * etchings read as absent. Null when the read is left as it is (an etching that is not converted, a client or a
     * non-player entity).
     */
    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List replace(LivingEntity entity, VaultGearAttribute<?> attribute) {
        EtchingLines.Index current = EtchingLines.index();
        if (current == null || attribute.getRegistryName() == null) {
            return null;
        }
        String id = attribute.getRegistryName().toString();
        if (current.removed().contains(id)) {
            return new ArrayList<>();
        }
        EtchingLines.Entry entry = current.byAttribute().get(id);
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
     * a spell switched on, else the player's best auto line of the type. A lingering etching (read by a buff after its
     * cast) is decided by its own ability's open cast step, else by the note its buff's cast left ({@link
     * LingeringBuffs}).
     */
    @Nullable
    public static GlyphGrants.Grant grant(ServerPlayer player, GlyphType type) {
        CastScope.Active scope = CastScope.forPlayer(player);
        EtchingLines.Index current = EtchingLines.index();
        EtchingLines.Entry entry = current == null ? null : current.byType().get(type);
        if (entry != null && entry.config().lingering != null) {
            return lingering(player, entry, current.config(), scope);
        }
        GlyphGrants cast = ActiveGrants.ofCast(player);
        if (cast != null) {
            return cast.get(type);
        }
        GlyphGrants.Grant toggled = ConvertedAffixes.toggleGrants(player).get(type);
        if (toggled != null) {
            return toggled;
        }
        SpellcraftConfig config = xyz.iwolfking.woldsvaults.init.ModConfigs.SPELLCRAFT;
        if (config == null) {
            return null;
        }
        return LingeringBuffs.autoLine(player, type, config);
    }

    @Nullable
    private static GlyphGrants.Grant lingering(ServerPlayer player, EtchingLines.Entry entry, SpellcraftConfig config, @Nullable CastScope.Active scope) {
        GlyphType type = entry.type();
        SpellcraftConfig.GlyphDefinition glyph = config.glyph(type);
        if (glyph == null || glyph.specs == null || glyph.specs.size() != 1) {
            WoldLogger.errorOnce("ConvertedEtchings:lingering_spec:" + type.id(), "Spellcraft: lingering etching glyph " + type.id()
                    + " is not locked to exactly one ability; it reads the gear auto line");
            return LingeringBuffs.autoLine(player, type, config);
        }
        String spec = glyph.specs.get(0);
        if (scope != null && spec.equals(scope.spec())) {
            return scope.modifiers().grants().get(type);
        }
        LingeringBuffs.Resolution resolution = LingeringBuffs.resolve(player, type, entry.config().lingering, spec, config);
        if (resolution.known()) {
            return resolution.grant();
        }
        WoldLogger.errorOnce("ConvertedEtchings:lingering_note:" + type.id(), "Spellcraft: no buff note for " + spec + " when " + type.id()
                + " was read (buff active since before a relog or restart?); it reads the gear auto line");
        return LingeringBuffs.autoLine(player, type, config);
    }

    @Nullable
    private static Object value(SpellcraftConfig config, EtchingLines.Entry entry, GlyphGrants.Grant grant) {
        List<SpellcraftConfig.EtchingPart> parts = entry.config().parts;
        Object[] components = new Object[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            SpellcraftConfig.EtchingPart part = parts.get(i);
            switch (part.role) {
                case EtchingLines.POTENCY -> components[i] = number(part, entry.config().value(grant.potency()));
                case EtchingLines.FLAG -> components[i] = Boolean.TRUE;
                case EtchingLines.LINKED -> {
                    SpellcraftConfig.Linked linked = GearAffixLines.linked(config, entry.type(), part.linked);
                    if (linked == null) {
                        WoldLogger.errorOnce("ConvertedEtchings:linked:" + entry.type().id(), "Spellcraft: " + entry.type().displayName() + " has no linked value " + part.linked
                                + "; its etching acts as absent");
                        return null;
                    }
                    components[i] = number(part, GlyphQuality.linkedValue(linked, grant.quality()));
                }
                case EtchingLines.CHOICE -> {
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

    private static Object number(SpellcraftConfig.EtchingPart part, float value) {
        if (part.isInt()) {
            return Integer.valueOf(Math.round(value));
        }
        return Float.valueOf(value);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        EtchingLines.resetIndex();
    }
}
