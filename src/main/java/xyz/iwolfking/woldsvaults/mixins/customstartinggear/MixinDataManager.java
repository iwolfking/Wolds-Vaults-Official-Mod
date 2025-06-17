package xyz.iwolfking.woldsvaults.mixins.customstartinggear;

import com.brandon3055.csg.DataManager;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashSet;
import java.util.Set;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "customstartinggear")
    }
)
@Mixin(value = DataManager.class, remap = false)
public class MixinDataManager {
    @Redirect(method = "givePlayerStartGear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;clearContent()V", remap = true))
    private static void onlyDeleteDefaultItems(Inventory inventory) {
        Set<String> defaultItems = new HashSet<>();
        defaultItems.add("patchouli:guide_book");
        defaultItems.add("the_vault:quest_book");

        boolean defaultInventory = true;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var is = inventory.getItem(i);
            if (!is.isEmpty()) {
                var rl = is.getItem().getRegistryName();
                if (rl == null || !defaultItems.contains(rl.toString())) {
                    WoldsVaults.LOGGER.warn("CustomStartingGear: Not clearing inventory because it contains non-default item {}.", rl);
                    defaultInventory = false;
                    break;
                }
            }
        }
        if (defaultInventory){
            inventory.clearContent();
        }
    }
}
