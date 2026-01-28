package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.VaultMod;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.model.NullCrystalModel;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.objective.RaidCrystalObjective;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.PoolCrystalThemeAccessor;

import java.util.Optional;

@Mixin(value = NullCrystalModel.class, remap = false)
public class MixinNullCrystalModel {
    @WrapOperation(method = "resolve", at = @At(value = "INVOKE", target = "Liskallia/vault/item/crystal/objective/CrystalObjective;getColor(F)Ljava/util/Optional;"))
    private Optional<Integer> brutalColor(CrystalObjective instance, float time, Operation<Optional<Integer>> original, @Local(argsOnly = true) CrystalData crystal) {
        if (instance instanceof RaidCrystalObjective && crystal.getTheme() instanceof PoolCrystalTheme themePool) {
            ResourceLocation id = ((PoolCrystalThemeAccessor)themePool).getId();
            if (id != null) {
                if (id.equals(VaultMod.id("infinite_raid_hard"))) {
                    return Optional.of(0x660000);
                }
            }
        }
        return original.call(instance, time);
    }
}
