package xyz.iwolfking.woldsvaults.mixins.create.accessors;

import com.simibubi.create.content.logistics.filter.FilterItem;
import dev.latvian.mods.itemfilters.api.IItemFilter;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.joseph.vaultfilters.VFTests;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "create"),
                @Condition(type = Condition.Type.MOD, value = "itemfilters"),
                @Condition(type = Condition.Type.MOD, value = "vaultfilters")
        }
)
@Mixin(value = FilterItem.class, remap = false)
public class MixinFilterItem implements IItemFilter {
    @Override
    public boolean filter(ItemStack itemStack, ItemStack itemStack1) {
        return VFTests.checkFilter(itemStack1, itemStack, true, null);
    }
}
