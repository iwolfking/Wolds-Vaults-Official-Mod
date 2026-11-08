package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.augments.effect.TickingEntity;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class MixinLevelTickingEntity {
    @Shadow
    @Final
    public boolean isClientSide;

    @Inject(method = "guardEntityTick", at = @At("HEAD"))
    private <T extends Entity> void woldsvaults$pushTicking(Consumer<T> tick, T entity, CallbackInfo ci) {
        if (!this.isClientSide) {
            TickingEntity.push(entity);
        }
    }

    @Inject(method = "guardEntityTick", at = @At("RETURN"))
    private <T extends Entity> void woldsvaults$popTicking(Consumer<T> tick, T entity, CallbackInfo ci) {
        if (!this.isClientSide) {
            TickingEntity.pop();
        }
    }
}
