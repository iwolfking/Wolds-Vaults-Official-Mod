package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.etching.EtchingSet;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.util.calc.CooldownHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CooldownHelper.class, remap = false)
public class MixinCooldownHelper {
    @Redirect(method = "adjustCooldown", at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;hasEtching(Liskallia/vault/etching/EtchingSet;)Z"))
    private static boolean disableRiftEtchingHandling(AttributeSnapshot instance, EtchingSet<?> set) {
        return false;
    }
}
