package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.entity.entity.VaultDecoyProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VaultDecoyProjectile.class, remap = false)
public interface VaultDecoyProjectileInvoker {
    @Invoker("spawnDecoy")
    void woldsvaults$spawnDecoy(ServerLevel level, Vec3 hitLocation);
}
