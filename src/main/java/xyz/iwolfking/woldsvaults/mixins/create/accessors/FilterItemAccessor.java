package xyz.iwolfking.woldsvaults.mixins.create.accessors;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = FilterItem.class, remap = false)
public interface FilterItemAccessor {
    @Invoker("makeSummary")
    List<Component> runMakeSummary(ItemStack filter);
}
