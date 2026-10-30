package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.entity.entity.VaultFireball;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.entities.projectiles.VaultMeteor;

import java.util.Optional;
import java.util.Random;

public class MeteorStormAbility extends InstantManaAbility {

    private double radius;
    private int meteorCount;
    private float damageMultiplier;

    public MeteorStormAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost, double radius, int meteorCount, float damageMultiplier) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.radius = radius;
        this.meteorCount = meteorCount;
        this.damageMultiplier = damageMultiplier;
    }

    public MeteorStormAbility() {
    }

    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            ServerLevel level = (ServerLevel) player.level;
            Random random = level.getRandom();
            
            float realRadius = AreaOfEffectHelper.adjustAreaOfEffect(player, this, (float)getRadius());
            boolean allowPvP = VaultUtils.getVault(level).map(VaultUtils::isPvPVault).orElse(false);

            for (int i = 0; i < meteorCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 2 * realRadius;
                double offsetZ = (random.nextDouble() - 0.5) * 2 * realRadius;
                
                Vec3 spawnPos = player.position().add(offsetX, 15.0, offsetZ);
                
                VaultMeteor meteor = new VaultMeteor(level, player, 25, 1.5F);
                meteor.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                meteor.setAllowPvP(allowPvP);

                meteor.shoot(0, -1, 0, 1.5F, 1.0F); 
                
                meteor.setType(VaultFireball.FireballType.BASE);

                level.addFreshEntity(meteor);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.8F);

            this.putOnCooldown(context);
            return ActionResult.successCooldownImmediate();
        }).orElse(ActionResult.fail());
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.DOUBLE.writeBits(this.radius, buffer);
        Adapters.INT.writeBits(this.meteorCount, buffer);
        Adapters.FLOAT.writeBits(this.damageMultiplier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.radius = Adapters.DOUBLE.readBits(buffer).orElse(5.0);
        this.meteorCount = Adapters.INT.readBits(buffer).orElse(10);
        this.damageMultiplier = Adapters.FLOAT.readBits(buffer).orElse(1.0F);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.DOUBLE.writeNbt(this.radius).ifPresent(tag -> nbt.put("radius", tag));
            Adapters.INT.writeNbt(this.meteorCount).ifPresent(tag -> nbt.put("meteorCount", tag));
            Adapters.FLOAT.writeNbt(this.damageMultiplier).ifPresent(tag -> nbt.put("damageMultiplier", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.radius = Adapters.DOUBLE.readNbt(nbt.get("radius")).orElse(5.0);
        this.meteorCount = Adapters.INT.readNbt(nbt.get("meteorCount")).orElse(10);
        this.damageMultiplier = Adapters.FLOAT.readNbt(nbt.get("damageMultiplier")).orElse(1.0F);
    }

    public double getRadius() { return radius; }
}