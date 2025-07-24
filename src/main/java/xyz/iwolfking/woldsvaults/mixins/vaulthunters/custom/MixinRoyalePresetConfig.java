package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.RoyalePresetConfig;
import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;

import java.util.Map;

@Mixin(value = RoyalePresetConfig.class, remap = false)
public abstract class MixinRoyalePresetConfig {

    @Inject(method = "apply", at = @At("TAIL"))
    private static void setTrinketPouch(ServerPlayer player, String presetKey, Map<String, Integer> preset, CallbackInfo ci) {
        IntegrationCurios.setCurioItemStack(player, TrinketPouchItem.create(WoldsVaults.id("prismatic"), true), "trinket_pouch", 0);
    }
}
