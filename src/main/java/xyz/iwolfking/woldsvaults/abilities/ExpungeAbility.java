package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.entity.entity.EffectCloudEntity;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModSounds;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.util.calc.EffectDurationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import xyz.iwolfking.woldsvaults.api.helper.WoldAttributeHelper;

import java.util.Optional;

public class ExpungeAbility extends InstantManaAbility {
    private float radiusMultiplier;
    private float durationMultiplier;

    public float getRadiusMultiplier() {
        return radiusMultiplier;
    }


    public float getDurationMultiplier() {
        return durationMultiplier;
    }



    public ExpungeAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost, float radiusMultiplier, float durationMultiplier) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.radiusMultiplier = radiusMultiplier;
        this.durationMultiplier = durationMultiplier;
    }


    public ExpungeAbility() {

    }



    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            WoldAttributeHelper.withSnapshot(
                    player,
                    true,
                    (playerEntity, snapshot) -> snapshot.getAttributeValue(ModGearAttributes.EFFECT_CLOUD_WHEN_HIT, VaultGearAttributeTypeMerger.asList())
                            .forEach(cloud -> {
                                MobEffect effect = cloud.getPrimaryEffect();
                                if (effect != null) {
                                    EffectCloudEntity cloudEntity = new EffectCloudEntity(playerEntity.getLevel(), playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
                                    cloud.apply(cloudEntity);
                                    cloudEntity.setRadius(AreaOfEffectHelper.adjustAreaOfEffect(playerEntity, null, cloudEntity.getRadius() * this.getRadiusMultiplier()));
                                    cloudEntity.setOwner(playerEntity);
                                    cloudEntity.setDuration(EffectDurationHelper.adjustEffectDurationFloor(playerEntity, cloudEntity.getDuration() * this.getDurationMultiplier()));
                                    playerEntity.getLevel().addFreshEntity(cloudEntity);
                                }
                            })
            );

            WoldAttributeHelper.withSnapshot(
                    player,
                    true,
                    (playerEntity, snapshot) -> snapshot.getAttributeValue(ModGearAttributes.EFFECT_CLOUD, VaultGearAttributeTypeMerger.asList())
                            .forEach(cloud -> {
                                MobEffect effect = cloud.getPrimaryEffect();
                                if (effect != null) {
                                    EffectCloudEntity cloudEntity = new EffectCloudEntity(playerEntity.getLevel(), playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
                                    cloud.apply(cloudEntity);
                                    cloudEntity.setRadius(AreaOfEffectHelper.adjustAreaOfEffect(playerEntity, null, cloudEntity.getRadius()));
                                    cloudEntity.setOwner(playerEntity);
                                    cloudEntity.setDuration(EffectDurationHelper.adjustEffectDurationFloor(playerEntity, cloudEntity.getDuration()));
                                    playerEntity.getLevel().addFreshEntity(cloudEntity);
                                }
                            })
            );

            player.getLevel().playSound(null, player.getOnPos(), ModSounds.OVERGROWN_ZOMBIE_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
            this.putOnCooldown(context);
            return ActionResult.successCooldownDeferred();
        }).orElse(ActionResult.fail());
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.FLOAT.writeBits(this.radiusMultiplier,buffer);
        Adapters.FLOAT.writeBits(this.durationMultiplier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.radiusMultiplier = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.durationMultiplier = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.FLOAT.writeNbt(this.durationMultiplier).ifPresent(tag -> nbt.put("durationMultiplier", tag));
            Adapters.FLOAT.writeNbt(this.radiusMultiplier).ifPresent(tag -> nbt.put("radiusMultiplier", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        Adapters.FLOAT.writeNbt(this.durationMultiplier).ifPresent(tag -> nbt.put("durationMultiplier", tag));
        Adapters.FLOAT.writeNbt(this.radiusMultiplier).ifPresent(tag -> nbt.put("radiusMultiplier", tag));
        super.readNbt(nbt);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.FLOAT.writeJson(this.durationMultiplier).ifPresent(element -> json.add("durationMultiplier", element));
            Adapters.FLOAT.writeJson(this.radiusMultiplier).ifPresent(element -> json.add("radiusMultiplier", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.durationMultiplier = Adapters.FLOAT.readJson(json.get("durationMultiplier")).orElse(1.0F);
        this.radiusMultiplier = Adapters.FLOAT.readJson(json.get("radiusMultiplier")).orElse(1.0F);
    }
}
