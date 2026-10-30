package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.options.types.ObjectiveHudSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ObjectiveHudSettings.class, remap = false)
public class MixinObjectiveHudSettings {
    @Inject(method = "createDefault", at = @At(value = "TAIL"))
    private static void addWoldsObjectives(CallbackInfoReturnable<ObjectiveHudSettings> cir, @Local(name = "settings") ObjectiveHudSettings settings) {
        settings.getAllSettings().put("alchemy", ObjectiveHudSettings.ObjectiveSettings.createDefault());
        settings.getAllSettings().put("zealot", ObjectiveHudSettings.ObjectiveSettings.createDefault());
        settings.getAllSettings().put("corrupted", ObjectiveHudSettings.ObjectiveSettings.createDefault());
        settings.getAllSettings().put("survival", ObjectiveHudSettings.ObjectiveSettings.createDefault());
        settings.getAllSettings().put("brutal_bosses", ObjectiveHudSettings.ObjectiveSettings.createDefault());
        settings.getAllSettings().put("hyper", ObjectiveHudSettings.ObjectiveSettings.createDefault());
    }
}
