package xyz.iwolfking.woldsvaults.spellcraft.scope;

import net.minecraft.nbt.CompoundTag;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.spell.PlacedGlyph;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The active unique glyphs of one step (gear glyphs such as Rime or Steel Core, and Chaining), each with the potency
 * and quality of its view line. The cast scope carries them, and so does everything the cast spawns, so readers of a
 * converted gear affix read the step's glyph. A step without unique glyphs grants nothing, which keeps converted
 * affixes off plain casts.
 */
public final class GlyphGrants {
    public static final GlyphGrants NONE = new GlyphGrants(Map.of());

    private final Map<GlyphType, Grant> grants;

    /** One granted glyph: its potency and the quality its linked values are read at. */
    public record Grant(float potency, float quality) {
    }

    private GlyphGrants(Map<GlyphType, Grant> grants) {
        this.grants = grants;
    }

    /** The active unique glyphs among a step's glyphs; the strongest copy wins when a type appears twice. */
    public static GlyphGrants of(Function<GlyphType, SpellcraftConfig.GlyphDefinition> definitions, List<PlacedGlyph> glyphs) {
        Map<GlyphType, Grant> grants = null;
        for (PlacedGlyph glyph : glyphs) {
            SpellcraftConfig.GlyphDefinition definition = definitions.apply(glyph.type());
            if (!glyph.active() || definition == null || !definition.isUnique()) {
                continue;
            }
            if (grants == null) {
                grants = new EnumMap<>(GlyphType.class);
            }
            Grant existing = grants.get(glyph.type());
            if (existing == null || glyph.potency() > existing.potency()) {
                grants.put(glyph.type(), new Grant(glyph.potency(), glyph.quality()));
            }
        }
        return grants == null ? NONE : new GlyphGrants(Collections.unmodifiableMap(grants));
    }

    /** Both grant sets; the stronger grant wins where both hold a type. */
    public static GlyphGrants merge(GlyphGrants a, GlyphGrants b) {
        Map<GlyphType, Grant> grants = new EnumMap<>(GlyphType.class);
        grants.putAll(a.grants);
        b.grants.forEach((type, grant) -> grants.merge(type, grant, (x, y) -> x.potency() >= y.potency() ? x : y));
        return grants.isEmpty() ? NONE : new GlyphGrants(Collections.unmodifiableMap(grants));
    }

    public boolean isEmpty() {
        return this.grants.isEmpty();
    }

    public boolean has(GlyphType type) {
        return this.grants.containsKey(type);
    }

    @Nullable
    public Grant get(GlyphType type) {
        return this.grants.get(type);
    }

    public Map<GlyphType, Grant> all() {
        return this.grants;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        this.grants.forEach((type, grant) -> {
            CompoundTag entry = new CompoundTag();
            entry.putFloat("p", grant.potency());
            entry.putFloat("q", grant.quality());
            tag.put(type.id(), entry);
        });
        return tag;
    }

    public static GlyphGrants read(CompoundTag tag) {
        Map<GlyphType, Grant> grants = new EnumMap<>(GlyphType.class);
        for (String key : tag.getAllKeys()) {
            GlyphType type = GlyphType.byId(key);
            if (type != null) {
                CompoundTag entry = tag.getCompound(key);
                grants.put(type, new Grant(entry.getFloat("p"), entry.getFloat("q")));
            }
        }
        return grants.isEmpty() ? NONE : new GlyphGrants(Collections.unmodifiableMap(grants));
    }
}
