package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.ScavengerBingoCrystalObjective;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ScavengerBingoCrystalObjectiveAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.SealEntryAccessor;
import xyz.iwolfking.woldsvaults.objectives.ScalingScavengerBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.lib.ScalingObjective;

import java.util.Map;

@Mixin(value = VaultCrystalConfig.class, remap = false)
public class MixinVaultCrystalConfig {
    @Shadow private Map<ResourceLocation, LevelEntryList<VaultCrystalConfig.SealEntry>> SEALS;

    @Inject(method = "lambda$applySeal$5", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/CrystalData;getObjective()Liskallia/vault/item/crystal/objective/CrystalObjective;"), cancellable = true)
    private static void cancelMaxSeal(ItemStack input, CrystalData crystal, ItemStack output, VaultCrystalConfig.SealEntry entry, CallbackInfoReturnable<Boolean> cir) {
        if(crystal.getObjective() instanceof ScalingObjective crystalObj && ((SealEntryAccessor)entry).getObjective().getClass() == crystal.getObjective().getClass()) {
            if (crystalObj.getSealCount() >= crystalObj.getMaxSealCount()) {
                cir.setReturnValue(false);
            }
        }
    }

    @WrapOperation(method = "lambda$applySeal$5", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/CrystalData;setObjective(Liskallia/vault/item/crystal/objective/CrystalObjective;)V"))
    private static void modifyScalingBingoObjective(CrystalData crystalData, CrystalObjective sealObjective, Operation<Void> original) {
        if (crystalData.getObjective() instanceof ScalingObjective scalingObjective && sealObjective.getClass() == crystalData.getObjective().getClass()) {
            crystalData.setObjective(scalingObjective.increaseBy(1));
            return;
        }

        if(sealObjective instanceof ScavengerBingoCrystalObjective) { // convert normal collector into scaling
            if(crystalData.getObjective() instanceof ScavengerBingoCrystalObjective scavBingoObjective) {
                var newHeight = ((ScavengerBingoCrystalObjectiveAccessor)scavBingoObjective).getHeight() + 1;
                var extraSeals = newHeight - (ScalingScavengerBingoCrystalObjective.DEFAULT_HEIGHT - 1);
                ScalingScavengerBingoCrystalObjective newObjective = new ScalingScavengerBingoCrystalObjective(((ScavengerBingoCrystalObjectiveAccessor)scavBingoObjective).getObjectiveProbability(), extraSeals);
                crystalData.setObjective(newObjective);
                return;
            }

            if(crystalData.getObjective() instanceof ScalingScavengerBingoCrystalObjective scalingBingoCrystalObjective) {
                crystalData.setObjective(scalingBingoCrystalObjective.increaseBy(1));
                return;
            }
        }

        original.call(crystalData, sealObjective);
    }
}
