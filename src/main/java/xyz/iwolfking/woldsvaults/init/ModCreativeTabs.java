package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModCreativeTabs {
    public static final CreativeModeTab WOLDS_VAULTS = new CreativeModeTab(WoldsVaults.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ModItems.WOLD_STAR);
        }
    };
}
