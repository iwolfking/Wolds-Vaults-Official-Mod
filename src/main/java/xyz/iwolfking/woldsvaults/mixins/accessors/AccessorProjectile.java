package xyz.iwolfking.woldsvaults.mixins.accessors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface AccessorProjectile {
    @Invoker("onHit")
    void woldsvaults$onHit(HitResult hit);

    @Invoker("canHitEntity")
    boolean woldsvaults$canHitEntity(Entity target);
}
