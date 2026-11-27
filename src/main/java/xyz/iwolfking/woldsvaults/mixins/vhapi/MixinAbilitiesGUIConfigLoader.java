package xyz.iwolfking.woldsvaults.mixins.vhapi;

import iskallia.vault.config.AbilitiesGUIConfig;
import iskallia.vault.init.ModConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.loaders.skills.AbilitiesGUIConfigLoader;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbilitiesGUIConfigLookupAccessor;

import java.util.Map;

@Mixin(value = AbilitiesGUIConfigLoader.class, remap = false)
public class MixinAbilitiesGUIConfigLoader {

    /**
     * Rebuilds the specialization icon lookup after the merged styles are in place, so
     * {@code AbilitiesGUIConfig.getIcon} finds the specializations added through VHAPI.
     */
    @Inject(method = "afterConfigsLoad", at = @At("TAIL"))
    private void woldsvaults$rebuildIconLookup(VaultConfigEvent.End event, CallbackInfo ci) {
        AbilitiesGUIConfig config = ModConfigs.ABILITIES_GUI;
        if (config == null || config.getStyles() == null) {
            WoldLogger.error("Ability GUI styles are not loaded; the ability icon lookup was not rebuilt");
            return;
        }
        Map<String, AbilitiesGUIConfig.SpecializationStyle> lookup =
                ((AbilitiesGUIConfigLookupAccessor) config).woldsvaults$getSpecializationStyleLookup();
        lookup.clear();
        for (AbilitiesGUIConfig.AbilityStyle style : config.getStyles().values()) {
            if (style.getSpecializationStyles() != null) {
                lookup.putAll(style.getSpecializationStyles());
            }
        }
    }
}
