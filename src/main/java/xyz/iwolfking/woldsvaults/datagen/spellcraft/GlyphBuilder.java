package xyz.iwolfking.woldsvaults.datagen.spellcraft;

import xyz.iwolfking.woldsvaults.config.SpellcraftConfig;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;

import java.util.List;

/**
 * One glyph definition of the spellcraft table, set field by field. Unset trait, spec, exclusion and effect lists are
 * empty, the level floor is 0 and the step effects reach only the step the glyph follows; {@link #register} refuses a
 * glyph without a kind, class, category, weight, potency, uses, cap or cost.
 */
public final class GlyphBuilder {
    private final GlyphType type;
    private String kind;
    private String glyphClass;
    private String category;
    private int minLevel;
    private Integer weight;
    private List<List<String>> requires = List.of();
    private List<String> forbids = List.of();
    private List<String> specs = List.of();
    private List<String> excludes = List.of();
    private SpellcraftConfig.Potency potency;
    private List<SpellcraftConfig.Effect> step = List.of();
    private List<SpellcraftConfig.Effect> spell = List.of();
    private Integer maxUses;
    private List<Integer> useWeights;
    private Integer capPerSpell;
    private Integer costMin;
    private Integer costMax;
    private boolean noEscalation;
    private String scope = SpellcraftConfig.GlyphDefinition.SCOPE_STEP;
    private String description;

    public GlyphBuilder(GlyphType type) {
        this.type = type;
    }

    public GlyphType type() {
        return this.type;
    }

    public GlyphBuilder kind(String kind) {
        this.kind = kind;
        return this;
    }

    public GlyphBuilder glyphClass(String glyphClass) {
        this.glyphClass = glyphClass;
        return this;
    }

    public GlyphBuilder category(String category) {
        this.category = category;
        return this;
    }

    public GlyphBuilder minLevel(int minLevel) {
        this.minLevel = minLevel;
        return this;
    }

    public GlyphBuilder weight(int weight) {
        this.weight = weight;
        return this;
    }

    /** The trait sets an ability may match to take the glyph; it fits when it has every trait of any one set. */
    @SafeVarargs
    public final GlyphBuilder requires(List<String>... traitSets) {
        this.requires = List.of(traitSets);
        return this;
    }

    public GlyphBuilder forbids(String... traits) {
        this.forbids = List.of(traits);
        return this;
    }

    public GlyphBuilder specs(List<String> specs) {
        this.specs = specs;
        return this;
    }

    public GlyphBuilder excludes(String... glyphs) {
        this.excludes = List.of(glyphs);
        return this;
    }

    public GlyphBuilder potency(SpellcraftConfig.Potency potency) {
        this.potency = potency;
        return this;
    }

    public GlyphBuilder step(SpellcraftConfig.Effect... effects) {
        this.step = List.of(effects);
        return this;
    }

    public GlyphBuilder spell(SpellcraftConfig.Effect... effects) {
        this.spell = List.of(effects);
        return this;
    }

    public GlyphBuilder uses(int maxUses, List<Integer> useWeights) {
        this.maxUses = maxUses;
        this.useWeights = useWeights;
        return this;
    }

    public GlyphBuilder cap(int capPerSpell) {
        this.capPerSpell = capPerSpell;
        return this;
    }

    public GlyphBuilder cost(int costMin, int costMax) {
        this.costMin = costMin;
        this.costMax = costMax;
        return this;
    }

    public GlyphBuilder noEscalation() {
        this.noEscalation = true;
        return this;
    }

    public GlyphBuilder spellWide() {
        this.scope = SpellcraftConfig.GlyphDefinition.SCOPE_SPELL;
        return this;
    }

    public GlyphBuilder description(String description) {
        this.description = description;
        return this;
    }

    public void register(SpellcraftConfig table) {
        require(this.kind, "kind");
        require(this.glyphClass, "class");
        require(this.category, "category");
        require(this.weight, "weight");
        require(this.potency, "potency");
        require(this.maxUses, "uses");
        require(this.capPerSpell, "cap");
        require(this.costMin, "cost");
        SpellcraftConfig.GlyphDefinition glyph = new SpellcraftConfig.GlyphDefinition();
        glyph.kind = this.kind;
        glyph.glyphClass = this.glyphClass;
        glyph.category = this.category;
        glyph.minLevel = this.minLevel;
        glyph.weight = this.weight;
        glyph.requires = this.requires;
        glyph.forbids = this.forbids;
        glyph.specs = this.specs;
        glyph.excludes = this.excludes;
        glyph.potency = this.potency;
        glyph.step = this.step;
        glyph.spell = this.spell;
        glyph.maxUses = this.maxUses;
        glyph.useWeights = this.useWeights;
        glyph.capPerSpell = this.capPerSpell;
        glyph.costMin = this.costMin;
        glyph.costMax = this.costMax;
        glyph.noEscalation = this.noEscalation;
        glyph.scope = this.scope;
        glyph.description = this.description;
        table.glyphs.put(this.type.id(), glyph);
    }

    private void require(Object value, String field) {
        if (value == null) {
            throw new IllegalStateException("Glyph " + this.type.id() + " has no " + field);
        }
    }
}
