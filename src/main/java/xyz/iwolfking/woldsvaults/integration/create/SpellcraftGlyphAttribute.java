package xyz.iwolfking.woldsvaults.integration.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.spellcraft.glyph.GlyphType;
import xyz.iwolfking.woldsvaults.spellcraft.item.GlyphLine;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create attribute filter entry matching a spellcraft focus or wand that rolled a given glyph line. Stored by glyph id.
 */
public class SpellcraftGlyphAttribute implements ItemAttribute {
    public static final String KEY = "woldsvaults_spellcraft_glyph";
    private static final Set<String> REPORTED_UNKNOWN = ConcurrentHashMap.newKeySet();

    private final String id;
    @Nullable
    private final GlyphType glyph;

    public SpellcraftGlyphAttribute(GlyphType glyph) {
        this.id = glyph.id();
        this.glyph = glyph;
    }

    private SpellcraftGlyphAttribute(String id, @Nullable GlyphType glyph) {
        this.id = id;
        this.glyph = glyph;
    }

    @Override
    public boolean appliesTo(ItemStack stack) {
        if (this.glyph == null || !SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return false;
        }
        return SpellcraftItemData.line(stack, this.glyph).isPresent();
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack stack) {
        List<ItemAttribute> attributes = new ArrayList<>();
        if (!SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return attributes;
        }
        for (GlyphLine line : SpellcraftItemData.lines(stack)) {
            attributes.add(new SpellcraftGlyphAttribute(line.type()));
        }
        return attributes;
    }

    @Override
    public String getTranslationKey() {
        return KEY;
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{this.glyph != null ? this.glyph.displayName() : this.id};
    }

    @Override
    public void writeNBT(CompoundTag tag) {
        tag.putString("glyph", this.id);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag tag) {
        String id = tag.getString("glyph");
        GlyphType glyph = GlyphType.byId(id);
        if (glyph == null && REPORTED_UNKNOWN.add(id)) {
            WoldLogger.error("Spellcraft filter: unknown glyph id '{}' in a Create attribute filter; that entry matches nothing", id);
        }
        return new SpellcraftGlyphAttribute(id, glyph);
    }
}
