package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.BossRunePillarTileEntity;
import iskallia.vault.core.event.common.ScavengerAltarConsumeEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.RuneBossObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;
import xyz.iwolfking.woldsvaults.api.helper.NormalizedHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

@Mixin(value = RuneBossObjective.class, remap = false)
public class MixinRuneBossObjective {
    @Inject(method = "initServer", at = @At("HEAD"))
    private void addNormalized(VirtualWorld world, Vault vault, CallbackInfo ci) {
        //NormalizedHelper.handleAddingNormalizedToVault(vault, world.getLevel());
    }

    @Inject(method = "lambda$initServer$3", at = @At(value = "INVOKE", target = "Liskallia/vault/item/BossRuneItem;getModifier(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;"), cancellable = true)
    private static void capMaximumRunes(VirtualWorld world, Vault vault, ScavengerAltarConsumeEvent.Data data, CallbackInfo ci, @Local BossRunePillarTileEntity pillarTileEntity) {
        if(pillarTileEntity.getRuneCount() >= pillarTileEntity.getRuneMinimum() * 5) {
            if(!GameruleHelper.isEnabled(ModGameRules.UNLIMITED_RUNE_BOSS, world.getLevel())) {
                ci.cancel();
            }
        }
    }
}
