package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.integration.IntegrationMinimap;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import xaero.common.settings.ModOptions;
import xaero.common.settings.ModSettings;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "xaerominimap")
    }
)
@Mixin(value = IntegrationMinimap.class, remap = false)
public class MixinIntegrationMinimap { // fix not being able to disable minimap outside of vaults

    @Redirect(method = "lambda$onClientTick$0", at = @At(value = "INVOKE", target = "Lxaero/common/settings/ModSettings;getOptionValue(Lxaero/common/settings/ModOptions;)Ljava/lang/Object;", ordinal = 0),
    slice = @Slice(
            from = @At(value = "FIELD", target = "Lxaero/common/settings/ModOptions;MINIMAP:Lxaero/common/settings/ModOptions;"),
            to = @At(value = "FIELD", target = "Lxaero/common/settings/ModOptions;SIZE:Lxaero/common/settings/ModOptions;")
        )
    )
    private static Object dontDisableMinimapInVault(ModSettings instance, ModOptions par1EnumOptions) {
        if(WoldsVaultsConfig.CLIENT.showVanillaVaultMap.get()) {
            return true;
        }
        else {
            if(instance.getOptionValue(ModOptions.MINIMAP).equals(false)) {
                instance.setOptionValue(ModOptions.MINIMAP, true);
            }
            return false;
        }
    }

    @Redirect(method = "lambda$onClientTick$0", at = @At(value = "INVOKE", target = "Lxaero/common/settings/ModSettings;getOptionValue(Lxaero/common/settings/ModOptions;)Ljava/lang/Object;", ordinal = 1),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lxaero/common/settings/ModOptions;SIZE:Lxaero/common/settings/ModOptions;")
        ))
    private static Object dontEnableMinimapInOverworld(ModSettings instance, ModOptions par1EnumOptions) {
        return true;
    }
}
