package xyz.iwolfking.woldsvaults.spellcraft.gear;

import net.minecraft.nbt.CompoundTag;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.spell.SpellModel;
import xyz.iwolfking.woldsvaults.config.FocusAugmentsConfig;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The active unique glyphs of one step (gear glyphs such as Rime or Steel Core, and Chaining), each with the potency and
 * quality of its view line. The cast scope carries them, and so does everything the cast spawns, so the readers that
 * used to read the gear affix read the step's glyph instead. A step without unique glyphs grants nothing, which is
 * what keeps converted affixes off plain casts.
 */
public final class GlyphGrants {
    public static final GlyphGrants NONE = new GlyphGrants(Map.of());

    private final Map<AugmentType, Grant> grants;

    /** One granted glyph: its potency and the quality its linked values are read at. */
    public record Grant(float potency, float quality) {
    }

    private GlyphGrants(Map<AugmentType, Grant> grants) {
        this.grants = grants;
    }

    /** The active unique glyphs among a step's glyphs; the strongest copy wins when a type appears twice. */
    public static GlyphGrants of(Function<AugmentType, FocusAugmentsConfig.Glyph> specs, List<SpellModel.Glyph> glyphs) {
        Map<AugmentType, Grant> grants = null;
        for (SpellModel.Glyph glyph : glyphs) {
            FocusAugmentsConfig.Glyph spec = specs.apply(glyph.type());
            if (!glyph.active() || spec == null || !"unique".equals(spec.glyphClass)) {
                continue;
            }
            if (grants == null) {
                grants = new EnumMap<>(AugmentType.class);
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
        Map<AugmentType, Grant> grants = new EnumMap<>(AugmentType.class);
        grants.putAll(a.grants);
        b.grants.forEach((type, grant) -> grants.merge(type, grant, (x, y) -> x.potency() >= y.potency() ? x : y));
        return grants.isEmpty() ? NONE : new GlyphGrants(Collections.unmodifiableMap(grants));
    }

    public boolean isEmpty() {
        return this.grants.isEmpty();
    }

    public boolean has(AugmentType type) {
        return this.grants.containsKey(type);
    }

    @Nullable
    public Grant get(AugmentType type) {
        return this.grants.get(type);
    }

    public Map<AugmentType, Grant> all() {
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
        Map<AugmentType, Grant> grants = new EnumMap<>(AugmentType.class);
        for (String key : tag.getAllKeys()) {
            AugmentType type = AugmentType.byId(key);
            if (type != null) {
                CompoundTag entry = tag.getCompound(key);
                grants.put(type, new Grant(entry.getFloat("p"), entry.getFloat("q")));
            }
        }
        return grants.isEmpty() ? NONE : new GlyphGrants(Collections.unmodifiableMap(grants));
    }
}
