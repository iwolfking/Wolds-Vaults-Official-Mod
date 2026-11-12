package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.events.HyperVaultEvents;

@Mixin(value = VaultBossEntity.class, remap = false)
public class MixinVaultBossEntity {
    /**
     * Lets Chilled land on a vault boss inside a hyper vault; its level is reduced on the way in by
     * {@code MixinLivingEntityHyperBossChill}. Glacial Shatter and No AI stay rejected.
     */
    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$allowHyperBossChill(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect.getEffect() == ModEffects.CHILLED && HyperVaultEvents.isInHyperVault((VaultBossEntity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
