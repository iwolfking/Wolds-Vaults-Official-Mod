package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.etching.EtchingHelper;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.calc.EffectDurationHelper;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModEtchingGearAttributes;

import java.util.Optional;
import java.util.UUID;

public class SneakyGetawayAbility extends InstantManaAbility {
    private int durationTicks;
    private float size;
    private float speedPercentAdded;
    public SneakyGetawayAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost, int durationTicks, float size, float speedPercentAdded) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.durationTicks = durationTicks;
        this.size = size;
        this.speedPercentAdded = speedPercentAdded;
    }
    public SneakyGetawayAbility(){
    }

    public int getDurationTicks(LivingEntity entity) {
        return EffectDurationHelper.adjustEffectDurationFloor(entity, this.getUnmodifiedDurationTicks());
    }

    public int getUnmodifiedDurationTicks() {
        return this.durationTicks;
    }

    public float getSize() {return this.size;}

    public float getSpeedPercentAdded() {return this.speedPercentAdded;}


    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            if (player.hasEffect(ModEffects.SNEAKY_GETAWAY)) {
                return ActionResult.fail();
            } else {
                int duration = this.getDurationTicks(player);
                MobEffectInstance newEffect = new MobEffectInstance(ModEffects.SNEAKY_GETAWAY, duration, 0, false, false, true);
                player.addEffect(newEffect);
                return ActionResult.successCooldownDeferred();
            }
        }).orElse(ActionResult.fail());
    }
    //File saving stuff
    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT_SEGMENTED_7.writeBits(this.durationTicks, buffer);
        Adapters.FLOAT.writeBits(this.size,buffer);
        Adapters.FLOAT.writeBits(this.speedPercentAdded, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
        this.size = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.speedPercentAdded = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT_SEGMENTED_7.writeNbt(this.durationTicks).ifPresent(tag -> nbt.put("durationTicks",tag));
            Adapters.FLOAT.writeNbt(this.size).ifPresent(tag -> nbt.put("size", tag));
            Adapters.FLOAT.writeNbt(this.speedPercentAdded).ifPresent(tag -> nbt.put("speedPercentAdded", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readNbt(nbt.get("durationTicks")).orElse(0);
        this.size = Adapters.FLOAT.readNbt(nbt.get("size")).orElse(1.0F);
        this.speedPercentAdded = Adapters.FLOAT.readNbt(nbt.get("speedPercentAdded")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT_SEGMENTED_7.writeJson(this.durationTicks).ifPresent(element -> json.add("durationTicks",element));
            Adapters.FLOAT.writeJson(this.size).ifPresent(element -> json.add("size", element));
            Adapters.FLOAT.writeJson(this.speedPercentAdded).ifPresent(element -> json.add("speedPercentAdded", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readJson(json.get("durationTicks")).orElse(0);
        this.size = Adapters.FLOAT.readJson(json.get("size")).orElse(1.0F);
        this.speedPercentAdded = Adapters.FLOAT.readJson(json.get("speedPercentAdded")).orElse(0.0F);
    }

    public static class SneakyGetawayEffect extends MobEffect {
        private final ScaleType scaleType;
        private static final UUID SNEAKY_GETAWAY_SPEED_ADDITION_UUID = UUID.fromString("42171e94-3ecd-4ca2-ac3f-2aa4c7cb125b");
        public SneakyGetawayEffect(MobEffectCategory mobEffectCategory, int i, ScaleType scaleType, ResourceLocation id) {
            super(mobEffectCategory, i);
            this.scaleType = scaleType;
            this.setRegistryName(id);
        }

        @Override
        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                //get data from the skill tree
                float size = 1.0f;
                float speedPercentAdded = 0.0f;
                AbilityTree abilities = PlayerAbilitiesData.get((ServerLevel)player.level).getAbilities(player);
                for (SneakyGetawayAbility ability : abilities.getAll(SneakyGetawayAbility.class, Skill::isUnlocked)) {
                    size = ability.getSize();
                    speedPercentAdded = ability.getSpeedPercentAdded();
                }
                //set scale
                ScaleData scaleData = scaleType.getScaleData(player);
                scaleData.setScale(size);
                AttributeInstance speedAtt = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAtt != null && size != 0) {
                    //add to the speed
                    speedAtt.addTransientModifier(new AttributeModifier(SNEAKY_GETAWAY_SPEED_ADDITION_UUID,"SneakyGetawaySpeedAddition",speedPercentAdded, AttributeModifier.Operation.MULTIPLY_BASE));
                }

            }
        }

        @Override
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                //reset scale
                ScaleData scaleData = scaleType.getScaleData(player);
                scaleData.setTargetScale(1.0F);
                scaleData.setScaleTickDelay(scaleData.getScaleTickDelay());
                //reset speed
                AttributeInstance speedAtt = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAtt != null) {
                    speedAtt.removeModifier(SNEAKY_GETAWAY_SPEED_ADDITION_UUID);
                }
                //continue cooldown
                PlayerAbilitiesData.setAbilityOnCooldown(player, SneakyGetawayAbility.class);
            }
        }

        public static float getSneakyEtchingDodgeChance(LivingEntity entity) {
            if(entity instanceof ServerPlayer player) {
                VaultGearAttributeInstance<Float> sneakyEtchingAttribute = EtchingHelper.getEtchings(player, ModEtchingGearAttributes.SNEAKY_GETAWAY_NINJA).stream().findFirst().orElse(null);
                return sneakyEtchingAttribute != null ? sneakyEtchingAttribute.getValue() : 0F;
            }

            return 0F;
        }

    }
}
