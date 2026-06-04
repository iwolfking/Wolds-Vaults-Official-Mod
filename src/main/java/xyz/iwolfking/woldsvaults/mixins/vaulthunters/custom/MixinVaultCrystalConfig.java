package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.config.entry.LevelEntryList;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.ScavengerBingoCrystalObjective;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.ScavengerBingoCrystalObjectiveAccessor;
import xyz.iwolfking.woldsvaults.objectives.ScalingBallisticBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.ScalingScavengerBingoCrystalObjective;
import xyz.iwolfking.woldsvaults.objectives.ScalingUnhingedScavengerBingoCrystalObjective;

import java.util.Map;

@Mixin(value = VaultCrystalConfig.class, remap = false)
public class MixinVaultCrystalConfig {
    @Shadow private Map<ResourceLocation, LevelEntryList<VaultCrystalConfig.SealEntry>> SEALS;

    @WrapOperation(method = "lambda$applySeal$5", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/CrystalData;setObjective(Liskallia/vault/item/crystal/objective/CrystalObjective;)V"))
    private static void modifyScalingBingoObjective(CrystalData instance, CrystalObjective objective, Operation<Void> original) {
        if(objective instanceof ScalingBallisticBingoCrystalObjective) {
            if(instance.getObjective() instanceof ScalingBallisticBingoCrystalObjective scalingBingoCrystalObjective) {
                ScalingBallisticBingoCrystalObjective newObjective = new ScalingBallisticBingoCrystalObjective(scalingBingoCrystalObjective.getObjectiveProbability(), scalingBingoCrystalObjective.getSealCount() + 1);
                instance.setObjective(newObjective);
                return;
            }
        }

        if(objective instanceof ScavengerBingoCrystalObjective) {
            if(instance.getObjective() instanceof ScavengerBingoCrystalObjective scavBingoObjective) {
                var newHeight = ((ScavengerBingoCrystalObjectiveAccessor)scavBingoObjective).getHeight() + 1;
                var extraSeals = newHeight - (ScalingScavengerBingoCrystalObjective.DEFAULT_HEIGHT - 1);
                ScalingScavengerBingoCrystalObjective newObjective = new ScalingScavengerBingoCrystalObjective(((ScavengerBingoCrystalObjectiveAccessor)scavBingoObjective).getObjectiveProbability(), extraSeals);
                instance.setObjective(newObjective);
                return;
            }

            if(instance.getObjective() instanceof ScalingScavengerBingoCrystalObjective scalingBingoCrystalObjective) {
                ScalingScavengerBingoCrystalObjective newObjective = new ScalingScavengerBingoCrystalObjective(scalingBingoCrystalObjective.getObjectiveProbability(), scalingBingoCrystalObjective.getSealCount() + 1);
                instance.setObjective(newObjective);
                return;
            }
        }

        if(objective instanceof ScalingUnhingedScavengerBingoCrystalObjective) {
            if(instance.getObjective() instanceof ScalingUnhingedScavengerBingoCrystalObjective scalingBingoCrystalObjective) {
                ScalingUnhingedScavengerBingoCrystalObjective newObjective = new ScalingUnhingedScavengerBingoCrystalObjective(scalingBingoCrystalObjective.getObjectiveProbability(), scalingBingoCrystalObjective.getSealCount() + 1);
                instance.setObjective(newObjective);
                return;
            }
        }


        original.call(instance, objective);
    }
}
