package xyz.iwolfking.woldsvaults.datagen.spellcraft;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;

import java.util.List;

/**
 * An etching that grants a glyph: the glyph's definition plus the entry that reads the etching's value into it. The
 * value maps to potency as {@code offset + value * scale}, by default the value itself. An unset lingering source means
 * the glyph is read during its own cast.
 */
public final class EtchingGlyphBuilder {
    private final GlyphBuilder glyph;
    private final VaultGearAttribute<?> attribute;
    private float scale = 1.0F;
    private float offset = 0.0F;
    private List<String> choices = List.of();
    private List<SpellcraftConfig.EtchingPart> parts = List.of();
    private String lingering;

    public EtchingGlyphBuilder(GlyphBuilder glyph, VaultGearAttribute<?> attribute) {
        this.glyph = glyph;
        this.attribute = attribute;
    }

    public EtchingGlyphBuilder potency(SpellcraftConfig.Potency potency) {
        this.glyph.potency(potency);
        return this;
    }

    public EtchingGlyphBuilder scale(float scale) {
        this.scale = scale;
        return this;
    }

    public EtchingGlyphBuilder offset(float offset) {
        this.offset = offset;
        return this;
    }

    public EtchingGlyphBuilder choices(List<String> choices) {
        this.choices = choices;
        return this;
    }

    public EtchingGlyphBuilder parts(SpellcraftConfig.EtchingPart... parts) {
        this.parts = List.of(parts);
        return this;
    }

    /**
     * For a glyph read by a buff that outlasts its cast: where the read takes the glyph from, the note the ability's
     * cast left or the totem covering the player.
     */
    public EtchingGlyphBuilder lingering(String lingering) {
        this.lingering = lingering;
        return this;
    }

    public void register(SpellcraftConfig table) {
        this.glyph.register(table);
        SpellcraftConfig.EtchingGlyph entry = new SpellcraftConfig.EtchingGlyph();
        entry.attribute = this.attribute.getRegistryName().toString();
        entry.glyph = this.glyph.type().id();
        entry.scale = this.scale;
        entry.offset = this.offset;
        entry.parts = this.parts;
        entry.choices = this.choices;
        entry.lingering = this.lingering;
        table.etchingGlyphs.add(entry);
    }
}
