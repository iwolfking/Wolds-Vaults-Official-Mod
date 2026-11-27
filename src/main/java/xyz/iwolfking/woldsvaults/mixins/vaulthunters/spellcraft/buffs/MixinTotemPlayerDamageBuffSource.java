package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.buffs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.entity.TotemPlayerDamageTileEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.spellcraft.buff.LingeringBuffs;

/**
 * Damage totem: each player the totem buffs on its tick is noted with the totem, so its etching glyphs come from it.
 */
@Mixin(value = TotemPlayerDamageTileEntity.class, remap = false)
public class MixinTotemPlayerDamageBuffSource {
    @WrapOperation(method = "updatePlayerEffect", at = @At(value = "INVOKE",
            target = "Liskallia/vault/skill/ability/effect/TotemPlayerDamageAbility$TotemPlayerDamageEffect;addTo(Lnet/minecraft/world/entity/LivingEntity;F)V"))
    private void woldsvaults$noteCover(LivingEntity entity, float amount, Operation<Void> original) {
        if (entity instanceof ServerPlayer player) {
            LingeringBuffs.noteCover(player, (TotemPlayerDamageTileEntity) (Object) this);
        }
        original.call(entity, amount);
    }
}
