package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.gear.crafting.recipe.ToolForgeRecipe;
import iskallia.vault.gear.data.ToolGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.ToolItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.PrestigePowerHelper;
import xyz.iwolfking.woldsvaults.prestige.ToolCapacityPrestigePower;

import java.util.List;

@Mixin(value = ToolForgeRecipe.class, remap = false)
public class MixinToolForgeRecipe {
    @Inject(method = "createOutput", at = @At("RETURN"), cancellable = true)
    private void addToolCapacityFromPrestige(List<OverSizedItemStack> consumed, ServerPlayer crafter, int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        List<ToolCapacityPrestigePower> toolCapacityPrestigePowers = PrestigePowerHelper.getPrestigePowersOfType(crafter, ToolCapacityPrestigePower.class);
        if(toolCapacityPrestigePowers.isEmpty()) {
            return;
        }

        ItemStack toolStack = cir.getReturnValue();
        if(toolStack.getItem() instanceof ToolItem) {
            VaultGearData gear = ToolGearData.read(toolStack);
            int toolCapacity = 0;
            if(gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).isPresent()) {
                toolCapacity = gear.getFirstValue(ModGearAttributes.TOOL_CAPACITY).get();
            }

            for(ToolCapacityPrestigePower power : toolCapacityPrestigePowers) {
                gear.createOrReplaceAttributeValue(ModGearAttributes.TOOL_CAPACITY, toolCapacity + power.getCapacityIncrease());
            }

            gear.write(toolStack);
            cir.setReturnValue(toolStack);
        }
    }
}
