package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.api.util.DelayedExecutionHelper;
import xyz.iwolfking.woldsvaults.entities.projectiles.CustomFangEntity;

import java.util.Optional;

public class EvokerFangsAbility extends InstantManaAbility {
    private double radius;
    private float damageMultiplier;
    private float baseDamage;
    private float executeThreshold;


    @Override
    protected Ability.ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            ServerLevel level = (ServerLevel) player.level;
            double realRadius = AreaOfEffectHelper.adjustAreaOfEffect(player, this, (float) this.radius);

            float playerAttackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float finalDamage = (playerAttackDamage * this.damageMultiplier) + baseDamage;

            for (double d = 1.0; d <= realRadius; d += 1.5) {
                final double currentDist = d;

                int delay = (int) (d * 2);

                DelayedExecutionHelper.schedule(level, delay, () -> {
                    spawnFangRing(player, level, currentDist, finalDamage);
                });
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);

            this.putOnCooldown(context);
            return Ability.ActionResult.successCooldownImmediate();
        }).orElse(Ability.ActionResult.fail());
    }

    private void spawnFangRing(ServerPlayer player, ServerLevel level, double dist, float damage) {

        int count = (int) (Math.PI * 2 * dist / 1.5);
        for (int i = 0; i < count; i++) {
            double angle = i * (Math.PI * 2 / count);
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            double y = player.getY();

            CustomFangEntity fangs = new CustomFangEntity(level, x, y, z, player.getYRot(), player, damage, this.executeThreshold, 0, 0,false, false);
            level.addFreshEntity(fangs);
        }
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

    public float getExecuteThreshold() {
        return executeThreshold;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.DOUBLE.writeBits(this.radius, buffer);
        Adapters.FLOAT.writeBits(this.damageMultiplier, buffer);
        Adapters.FLOAT.writeBits(this.baseDamage, buffer);
        Adapters.FLOAT.writeBits(this.executeThreshold, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.radius = Adapters.DOUBLE.readBits(buffer).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
        this.executeThreshold = Adapters.FLOAT.readBits(buffer).orElse(0.0F);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.DOUBLE.writeNbt(this.radius).ifPresent(tag -> nbt.put("radius", tag));
            Adapters.FLOAT.writeNbt(this.damageMultiplier).ifPresent(tag -> nbt.put("damageMultiplier", tag));
            Adapters.FLOAT.writeNbt(this.baseDamage).ifPresent(tag -> nbt.put("baseDamage", tag));
            Adapters.FLOAT.writeNbt(this.executeThreshold).ifPresent(tag -> nbt.put("executeThreshold", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.radius = Adapters.DOUBLE.readNbt(nbt.get("radius")).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readNbt(nbt.get("damageMultiplier")).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readNbt(nbt.get("baseDamage")).orElse(0.0F);
        this.executeThreshold = Adapters.FLOAT.readNbt(nbt.get("executeThreshold")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.DOUBLE.writeJson(this.radius).ifPresent(element -> json.add("radius", element));
            Adapters.FLOAT.writeJson(this.damageMultiplier).ifPresent(element -> json.add("damageMultiplier", element));
            Adapters.FLOAT.writeJson(this.baseDamage).ifPresent(element -> json.add("baseDamage", element));
            Adapters.FLOAT.writeJson(this.executeThreshold).ifPresent(element -> json.add("executeThreshold", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.radius = Adapters.DOUBLE.readJson(json.get("radius")).orElse(0.0);
        this.damageMultiplier = Adapters.FLOAT.readJson(json.get("damageMultiplier")).orElse(0.0F);
        this.baseDamage = Adapters.FLOAT.readJson(json.get("baseDamage")).orElse(0.0F);
        this.executeThreshold = Adapters.FLOAT.readJson(json.get("executeThreshold")).orElse(0.0F);
    }
}