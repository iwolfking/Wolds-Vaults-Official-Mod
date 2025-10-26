package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.VaultFireball;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultFireball.class, remap = false)
public abstract class MixinVaultFireball {
    @Shadow
    public abstract void explode(Vec3 pos);

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultFireball;createBouncingFireball(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Liskallia/vault/entity/entity/VaultFireball;", ordinal = 0, remap = false), remap = true)
    private void fireVolleyBouncesExplode(HitResult result, CallbackInfo ci) {
        explode(result.getLocation());
    }
}
