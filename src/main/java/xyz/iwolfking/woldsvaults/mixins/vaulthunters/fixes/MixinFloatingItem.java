package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.entity.entity.FloatingItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FloatingItemEntity.class, remap = false)
public abstract class MixinFloatingItem extends ItemEntity {
    public MixinFloatingItem(EntityType<? extends ItemEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void removeMagnetTag(EntityType type, Level world, CallbackInfo ci) {
        this.removeTag("PreventMagnetMovement");
    }
}
