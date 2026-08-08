package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.woldsvaults.api.util.DelayedExecutionHelper;
import xyz.iwolfking.woldsvaults.entities.projectiles.CustomFangEntity;

import java.util.Optional;

public class EvokerFangsMawAbility extends InstantManaAbility {

    private double radius;
    private float damageMultiplier;
    private float baseDamage;
    private float heartFragmentChance;
    private int effectAmplifier;


    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            ServerLevel level = (ServerLevel) player.level;

            double range = AreaOfEffectHelper.adjustAreaOfEffect(player, this, (float) this.radius) * 1.5;
            Vec3 look = player.getLookAngle().normalize();

            float playerAttackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float finalDamage = (playerAttackDamage * this.damageMultiplier) + baseDamage;

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, 1.2F, 0.7F);

            for (double d = 1.5; d <= range; d += 1.0) {
                final double distance = d;
                int delay = (int) (d * 1);

                DelayedExecutionHelper.schedule(level, delay, () -> {
                    Vec3 spawnPos = player.position().add(look.scale(distance));

                    for (int side = -1; side <= 1; side++) {
                        Vec3 perpendicular = new Vec3(-look.z, 0, look.x).scale(side * 0.5);
                        Vec3 finalPos = spawnPos.add(perpendicular);

                        CustomFangEntity fangs = new CustomFangEntity(level, finalPos.x, finalPos.y, finalPos.z, player.getYRot(), player, finalDamage, heartFragmentChance, getEffectDuration(player), effectAmplifier, true, false);
                        level.addFreshEntity(fangs);
                    }
                });
            }

            this.putOnCooldown(context);
            return ActionResult.successCooldownImmediate();
        }).orElse(ActionResult.fail());
    }

    public int getEffectDuration(ServerPlayer player) {
        return EffectDurationHelper.adjustEffectDurationFloor(player, 180);
    }

    public double getRadius() {
        return radius;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getHeartFragmentChance() {
        return heartFragmentChance;
    }

    public int getEffectAmplifier() {
        return effectAmplifier;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.DOUBLE.writeBits(this.radius, buffer);
        Adapters.FLOAT.writeBits(this.damageMultiplier, buffer);
        Adapters.FLOAT.writeBits(this.baseDamage, buffer);
        Adapters.FLOAT.writeBits(this.heartFragmentChance, buffer);
        Adapters.INT.writeBits(this.effectAmplifier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.radius = Adapters.DOUBLE.readBits(buffer).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.heartFragmentChance = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.effectAmplifier = Adapters.INT.readBits(buffer).orElse(0);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.DOUBLE.writeNbt(this.radius).ifPresent(tag -> nbt.put("radius", tag));
            Adapters.FLOAT.writeNbt(this.damageMultiplier).ifPresent(tag -> nbt.put("damageMultiplier", tag));
            Adapters.FLOAT.writeNbt(this.baseDamage).ifPresent(tag -> nbt.put("baseDamage", tag));
            Adapters.FLOAT.writeNbt(this.heartFragmentChance).ifPresent(tag -> nbt.put("heartFragmentChance", tag));
            Adapters.INT.writeNbt(this.effectAmplifier).ifPresent(tag -> nbt.put("effectAmplifier", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.radius = Adapters.DOUBLE.readNbt(nbt.get("radius")).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readNbt(nbt.get("damageMultiplier")).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readNbt(nbt.get("baseDamage")).orElse(0.0F);
        this.heartFragmentChance = Adapters.FLOAT.readNbt(nbt.get("heartFragmentChance")).orElse(0.0F);
        this.effectAmplifier = Adapters.INT.readNbt(nbt.get("effectAmplifier")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.DOUBLE.writeJson(this.radius).ifPresent(element -> json.add("radius", element));
            Adapters.FLOAT.writeJson(this.damageMultiplier).ifPresent(element -> json.add("damageMultiplier", element));
            Adapters.FLOAT.writeJson(this.baseDamage).ifPresent(element -> json.add("baseDamage", element));
            Adapters.FLOAT.writeJson(this.heartFragmentChance).ifPresent(element -> json.add("heartFragmentChance", element));
            Adapters.INT.writeJson(this.effectAmplifier).ifPresent(element -> json.add("effectAmplifier", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.radius = Adapters.DOUBLE.readJson(json.get("radius")).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readJson(json.get("damageMultiplier")).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readJson(json.get("baseDamage")).orElse(0.0F);
        this.heartFragmentChance = Adapters.FLOAT.readJson(json.get("heartFragmentChance")).orElse(0.0F);
        this.effectAmplifier = Adapters.INT.readJson(json.get("effectAmplifier")).orElse(0);
    }
}