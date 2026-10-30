package xyz.iwolfking.woldsvaults.mixins.sophisticatedbackpacks;

import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackModel;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = BackpackModel.class, remap = false)
public class MixinBackpackModel {
    /**
     * @author iwolfking
     * @reason Add new backpack item.
     */
    @Overwrite
    private static Map<Integer, Item> getBackpackItems() {
        return new LinkedHashMap<>(Map.of(
            0, ModItems.BACKPACK.get(),
            1, ModItems.COPPER_BACKPACK.get(),
            2, ModItems.IRON_BACKPACK.get(),
            3, ModItems.GOLD_BACKPACK.get(),
            4, ModItems.DIAMOND_BACKPACK.get(),
            5, ModItems.NETHERITE_BACKPACK.get(),
            6, xyz.iwolfking.woldsvaults.init.ModItems.XL_BACKPACK));
    }
}
