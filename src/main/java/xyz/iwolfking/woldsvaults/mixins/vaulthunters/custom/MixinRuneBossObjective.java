package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.RuneBossObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RuneBossObjective.class, remap = false)
public class MixinRuneBossObjective {
    @Inject(method = "initServer", at = @At("HEAD"))
    private void addNormalized(VirtualWorld world, Vault vault, CallbackInfo ci) {
        //NormalizedHelper.handleAddingNormalizedToVault(vault, world.getLevel());
    }

//    @Inject(method = "lambda$initServer$3", at = @At(value = "INVOKE", target = "Liskallia/vault/item/BossRuneItem;getModifier(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;"), cancellable = true)
//    private static void capMaximumRunes(VirtualWorld world, Vault vault, ScavengerAltarConsumeEvent.Data data, CallbackInfo ci, @Local BossRunePillarTileEntity pillarTileEntity) {
//        if(pillarTileEntity.getRuneCount() >= pillarTileEntity.getRuneMinimum() * 5) {
//            if(!GameruleHelper.isEnabled(ModGameRules.UNLIMITED_RUNE_BOSS, world.getLevel())) {
//                ci.cancel();
//            }
//        }
//    }
}
