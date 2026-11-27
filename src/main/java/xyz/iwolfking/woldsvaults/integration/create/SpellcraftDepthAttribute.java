package xyz.iwolfking.woldsvaults.integration.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.spellcraft.item.SpellcraftItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Create attribute filter entry matching a spellcraft focus or wand whose depth (glyphs per spell) is at least a
 * threshold. A stack lists one entry per threshold from 1 up to its own depth.
 */
public class SpellcraftDepthAttribute implements ItemAttribute {
    public static final String KEY = "woldsvaults_spellcraft_depth";

    private final int min;

    public SpellcraftDepthAttribute(int min) {
        this.min = min;
    }

    @Override
    public boolean appliesTo(ItemStack stack) {
        return SpellcraftItemData.canCarry(stack) && SpellcraftItemData.has(stack) && SpellcraftItemData.depth(stack) >= this.min;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack stack) {
        List<ItemAttribute> attributes = new ArrayList<>();
        if (!SpellcraftItemData.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return attributes;
        }
        int depth = SpellcraftItemData.depth(stack);
        for (int n = 1; n <= depth; n++) {
            attributes.add(new SpellcraftDepthAttribute(n));
        }
        return attributes;
    }

    @Override
    public String getTranslationKey() {
        return KEY;
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{this.min};
    }

    @Override
    public void writeNBT(CompoundTag tag) {
        tag.putInt("min", this.min);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag tag) {
        return new SpellcraftDepthAttribute(tag.getInt("min"));
    }
}
