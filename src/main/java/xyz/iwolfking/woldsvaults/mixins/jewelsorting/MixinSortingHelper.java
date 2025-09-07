package xyz.iwolfking.woldsvaults.mixins.jewelsorting;

import iskallia.vault.gear.data.VaultGearData;
import lv.id.bonne.vaulthunters.jewelsorting.utils.SortingHelper;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;
import java.util.Optional;


@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "vault_hunters_jewel_sorting")
    }
)
@Mixin(value = SortingHelper.class, remap = false)
public class MixinSortingHelper {
    @Inject(method = "compareVaultGear", at = @At("HEAD"), cancellable = true)
    private static void sortMapTiers(String leftName, VaultGearData leftData, String rightName, VaultGearData rightData,
                                     List<SortingHelper.GearOptions> sortingOrder, boolean ascending, CallbackInfoReturnable<Integer> cir){
        Optional<Integer> leftTier = leftData.getFirstValue(ModGearAttributes.MAP_TIER);
        Optional<Integer> rightTier = rightData.getFirstValue(ModGearAttributes.MAP_TIER);
        var returnValue = (leftTier.orElse(0)).compareTo(rightTier.orElse(0));
        if (returnValue != 0) {
            cir.setReturnValue(ascending ? returnValue : -returnValue );
        }
    }
}
