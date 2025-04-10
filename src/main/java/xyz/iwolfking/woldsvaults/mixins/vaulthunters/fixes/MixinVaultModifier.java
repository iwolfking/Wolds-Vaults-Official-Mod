package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModGearAttributes;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;

@Mixin(value = VaultModifier.class, remap = false)
public class MixinVaultModifier {
    @Shadow @Final protected VaultModifier.Display display;
    private static final boolean IS_EMOJIFUL_INSTALLED;

    /**
     * @author iwolfking
     * @reason Strip emojis if emojiful isn't installed
     */
    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    public void getDisplayName(CallbackInfoReturnable<String> cir) {
        if((!IS_EMOJIFUL_INSTALLED || WoldsVaultsConfig.CLIENT.hideEmojisOnCrystalModifiers.get()) && this.display.getName().contains(":")) {
            cir.setReturnValue(this.display.getName().replaceAll(":.*: *", ""));
        }
    }

    static {
        IS_EMOJIFUL_INSTALLED = LoadingModList.get().getModFileById("emojiful") != null;
    }
}
