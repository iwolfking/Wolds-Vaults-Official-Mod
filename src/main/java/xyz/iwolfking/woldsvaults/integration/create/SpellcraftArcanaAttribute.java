package xyz.iwolfking.woldsvaults.integration.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftItemData;
import xyz.iwolfking.woldsvaults.augments.item.SpellcraftRoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Create attribute filter entry matching a spellcraft focus or wand whose arcana budget is at least a threshold.
 * A stack lists one entry per multiple of {@link #STEP} up to its own budget.
 */
public class SpellcraftArcanaAttribute implements ItemAttribute {
    public static final String KEY = "woldsvaults_spellcraft_arcana";
    public static final int STEP = 10;

    private final int min;

    public SpellcraftArcanaAttribute(int min) {
        this.min = min;
    }

    @Override
    public boolean appliesTo(ItemStack stack) {
        return SpellcraftRoller.canCarry(stack) && SpellcraftItemData.has(stack) && SpellcraftItemData.budget(stack) >= this.min;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack stack) {
        List<ItemAttribute> attributes = new ArrayList<>();
        if (!SpellcraftRoller.canCarry(stack) || !SpellcraftItemData.has(stack)) {
            return attributes;
        }
        int budget = SpellcraftItemData.budget(stack);
        for (int n = STEP; n <= budget; n += STEP) {
            attributes.add(new SpellcraftArcanaAttribute(n));
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
        return new SpellcraftArcanaAttribute(tag.getInt("min"));
    }
}
