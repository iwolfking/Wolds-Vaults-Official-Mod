package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.item.gear.EtchingItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.List;

@Mixin(value = EtchingItem.class, remap = false)
public class MixinEtchingItem {
//    @WrapOperation(method = "appendHoverText", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"), remap = true)
//    private boolean addWoldsWeaponsToTooltip(List<String> instance, Collection<? extends String> es, Operation<Boolean> original, @Local(name = "type") String type) {
//        if(type.equals("Offensive")) {
//            instance.add("Battlestaff");
//            instance.add("Trident");
//            instance.add("Vaultarang");
//        }
//        else if(type.equals("Utility")) {
//            instance.add("Plushie");
//            instance.add("Loot Sack");
//        }
//
//        return original.call(instance, es);
//    }
}
