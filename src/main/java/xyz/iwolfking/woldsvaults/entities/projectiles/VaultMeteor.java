package xyz.iwolfking.woldsvaults.entities.projectiles;

import iskallia.vault.entity.entity.VaultFireball;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class VaultMeteor extends VaultFireball {

    private float customDamage = 0.0F;
    private float customRadius = 3.0F;

    public VaultMeteor(EntityType<? extends VaultFireball> type, Level level) {
        super(type, level);
    }

    public VaultMeteor(Level level, LivingEntity shooter, float damage, float radius) {
        super(level, shooter);
        this.customDamage = damage;
        this.customRadius = radius;
    }

    public VaultMeteor(ServerLevel level, ServerPlayer player, float damage, float radius) {
        super(level, player);
        this.customDamage = damage;
        this.customRadius = radius;
    }

    @Override
    public float getDamage() {
        return this.customDamage;
    }

    @Override
    public float getRadius() {
        return this.customRadius;
    }
}