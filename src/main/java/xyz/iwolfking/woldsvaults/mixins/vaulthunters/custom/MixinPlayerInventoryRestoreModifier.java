package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.PlayerInventoryRestoreModifier;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;

@Mixin(value = PlayerInventoryRestoreModifier.class, remap = false)
public class MixinPlayerInventoryRestoreModifier {
    @Inject(method = "doVaultRevive", at = @At("HEAD"))
    private static void restoreMaxHealthFromSecondChanceOnRevive(ServerPlayer player, Vault vault, PlayerInventoryRestoreModifier modifier, CallbackInfo ci) {
        HealthReductionHelper.restoreReduction(player, WoldConstants.SECOND_CHANCE_HEALTH_REDUCTION_UUID);
    }

    @Inject(method = "doPhoenixRevive", at = @At("HEAD"))
    private static void restoreMaxHealthFromSecondChanceOnPhoenixRevive(ServerPlayer player, Vault vault, int phoenixGearAttribute, CallbackInfo ci) {
        HealthReductionHelper.restoreReduction(player, WoldConstants.SECOND_CHANCE_HEALTH_REDUCTION_UUID);
    }
}
