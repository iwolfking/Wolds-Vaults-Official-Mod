package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.Restrictions;
import iskallia.vault.research.StageManager;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Set;

@Mixin(value = StageManager.class, remap = false)
public class MixinStageManager {
    @WrapOperation(method = "onItemCrafted", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;restrictedBy(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/research/Restrictions$Type;)Ljava/lang/String;"))
    private static String allowCraftingDyedBackpacksProper(ResearchTree instance, ItemStack item, Restrictions.Type restrictionType, Operation<String> original, @Local(name = "craftingMatrix") Container craftingMatrix) {
        if(restrictionType.equals(Restrictions.Type.CRAFTABILITY)) {
            if(item.getItem() instanceof BackpackItem) {

                if(item.hasTag()) {

                    if(item.getTag() != null && (item.getTag().contains("clothColor") || item.getTag().contains("borderColor"))) {

                        if(craftingMatrix.hasAnyOf(Set.of(item.getItem()))) {
                            return null;
                        }
                    }
                }
            }
        }

        return original.call(instance, item, restrictionType);
    }
}
