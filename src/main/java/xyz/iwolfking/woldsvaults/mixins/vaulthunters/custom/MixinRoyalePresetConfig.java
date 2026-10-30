package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.config.RoyalePresetConfig;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchRoyale;

@Mixin(value = RoyalePresetConfig.class, remap = false)
public abstract class MixinRoyalePresetConfig {

    @Inject(method = "apply", at = @At("TAIL"))
    private static void setTrinketPouch(ServerPlayer player, String presetKey, int vaultLevel, Map<String, Integer> preset, CallbackInfo ci) {
        PouchRoyale.equipAfterSnapshot(player);
    }
}
