package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.VaultAltarConfig;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.TimeTrialCrystalObjective;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.competition.TimeTrialCompetition;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;
import java.util.Optional;

@Mixin(value = VaultAltarConfig.class, remap = false)
public abstract class MixinVaultAltarConfig {

    @Shadow
    public List<VaultAltarConfig.Interface> INTERFACES;

    @Inject(method = "getOutput", at = @At("HEAD"), cancellable = true)
    private void setTimeTrialObjective(CallbackInfoReturnable<Optional<ItemStack>> cir, @Local(argsOnly = true) ItemStack input) {
        if(input.getItem().equals(ModItems.CRYSTAL_SEAL_TIME_TRIAL)) {
                if(TimeTrialCompetition.get().getCurrentObjective() != null) {
                    ItemStack output = null;
                    for(VaultAltarConfig.Interface element : this.INTERFACES) {
                        if (element.matchesInput(input)) {
                            output = Optional.of(element.getOutput()).orElse(new ItemStack(ModItems.VAULT_CRYSTAL));
                        }
                    }

                    if(output == null) {
                        return;
                    }

                    CrystalData data = CrystalData.read(output);
                    if(data.getObjective() instanceof TimeTrialCrystalObjective timeTrialObjective) {
                        timeTrialObjective.setObjective(ModConfigs.TIME_TRIAL_COMPETITION.OBJECTIVE_ENTRIES.get(TimeTrialCompetition.get().getCurrentObjective()));
                    }

                    if(data.getProperties() instanceof CapacityCrystalProperties capacityCrystalProperties) {
                        capacityCrystalProperties.setVolume(0);
                        capacityCrystalProperties.setSize(0);
                    }

                    data.write(output);
                    cir.setReturnValue(Optional.of(output));
                }
        }
    }
}
