package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.jei_sync;

import iskallia.vault.client.gui.framework.element.CraftingSelectorElement;
import mezz.jei.common.Internal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftingSelectorElement.CraftingSelector.class, remap = false)
public class MixinCraftingSelectElementSelector {
    @Inject(at = @At("RETURN"), method = "getEntries", cancellable = true, remap = false)
    public void filterSearch(CallbackInfoReturnable<List<CraftingSelectorElement.CraftingEntry>> cir) {
        if (Internal.getRuntime().isEmpty()) {
            return;
        }

        String[] filters = Internal.getRuntime().get().getIngredientFilter().getFilterText().toLowerCase().split(" ");

        List<CraftingSelectorElement.CraftingEntry> entries = new ArrayList<>(cir.getReturnValue());
        cir.setReturnValue(entries);
        entries.removeIf(entry -> {
            ItemStack itemStack = entry.getDisplayStack();
            String name = itemStack.getDisplayName().getString();
            for (String filter : filters) {
                if (!name.toLowerCase().contains(filter)) {
                    return true;
                }
            }
            return false;
        });
    }
}
