package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.item.CompanionParticleTrailItem;
import iskallia.vault.skill.ability.effect.spi.core.InstantManaAbility;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;
import xyz.iwolfking.woldsvaults.init.ModEffects;

import java.util.Optional;

public class ColossusAbility extends InstantManaAbility {
    private int durationTicks;
    private float size;
    private float additionalResistance;
    public ColossusAbility(int unlockLevel, int learnPointCost, int regretPointCost, int cooldownTicks, float manaCost, int durationTicks, float size, float additionalResistance) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCost);
        this.durationTicks = durationTicks;
        this.size = size;
        this.additionalResistance = additionalResistance;
    }
    public ColossusAbility() {
    }
    public int getDurationTicks() {
        return this.durationTicks;
    }
    public float getSize() { return this.size;}
    public float getAdditionalResistance() {return this.additionalResistance;}
    @Override
    protected ActionResult doAction(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {
            if (player.hasEffect(ModEffects.COLOSSUS)) {
                return ActionResult.fail();
            } else {
                int duration = this.getDurationTicks();
                MobEffectInstance newEffect = new MobEffectInstance(ModEffects.COLOSSUS, duration, 0, false, false, true);
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
        Adapters.FLOAT.writeBits(this.additionalResistance, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
        this.size = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.additionalResistance = Adapters.FLOAT.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT_SEGMENTED_7.writeNbt(this.durationTicks).ifPresent(tag -> nbt.put("durationTicks",tag));
            Adapters.FLOAT.writeNbt(this.size).ifPresent(tag -> nbt.put("size", tag));
            Adapters.FLOAT.writeNbt(this.additionalResistance).ifPresent(tag -> nbt.put("additionalResistance", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readNbt(nbt.get("durationTicks")).orElse(0);
        this.size = Adapters.FLOAT.readNbt(nbt.get("size")).orElse(1.0F);
        this.additionalResistance = Adapters.FLOAT.readNbt(nbt.get("additionalResistance")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT_SEGMENTED_7.writeJson(this.durationTicks).ifPresent(element -> json.add("durationTicks",element));
            Adapters.FLOAT.writeJson(this.size).ifPresent(element -> json.add("size", element));
            Adapters.FLOAT.writeJson(this.additionalResistance).ifPresent(element -> json.add("additionalResistance", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.durationTicks = Adapters.INT_SEGMENTED_7.readJson(json.get("durationTicks")).orElse(0);
        this.size = Adapters.FLOAT.readJson(json.get("size")).orElse(1.0F);
        this.additionalResistance = Adapters.FLOAT.readJson(json.get("additionalResistance")).orElse(0.0F);
    }
    @Mod.EventBusSubscriber(
            modid = "woldsvaults",
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static class ColossusEffect extends MobEffect {
        private final ScaleType scaleType;

        public ColossusEffect(MobEffectCategory mobEffectCategory, int i, ScaleType scaleType, ResourceLocation id) {
            super(mobEffectCategory, i);
            this.scaleType = scaleType;
            this.setRegistryName(id);
        }

        @Override
        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {

            if (entity instanceof ServerPlayer player) {
                //get data from the skill tree
                float size = 1.0f;
                //float additionalResistance = 0.0f;
                AbilityTree abilities = PlayerAbilitiesData.get((ServerLevel)player.level).getAbilities(player);
                for (ColossusAbility ability : abilities.getAll(ColossusAbility.class, Skill::isUnlocked)) {
                    size = ability.getSize();
                    //additionalResistance = ability.getAdditionalResistance();
                }
                //set scale
                ScaleData scaleData = scaleType.getScaleData(player);
                scaleData.setTargetScale(size);
                scaleData.setScaleTickDelay(scaleData.getScaleTickDelay());

            }
        }


        @Override
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                //reset scale
                ScaleData scaleData = scaleType.getScaleData(player);
                scaleData.setTargetScale(1.0F);
                scaleData.setScaleTickDelay(scaleData.getScaleTickDelay());
                //continue cooldown
                PlayerAbilitiesData.setAbilityOnCooldown(player, ColossusAbility.class);
            }

        }


        //No KB for affected
        @SubscribeEvent
        public static void onLivingKnockback(LivingKnockBackEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (player.hasEffect(ModEffects.COLOSSUS)) {
                    event.setStrength(0);
                }
            }
        }
        //mixined into applying resistance
        public static float getColossusResistance(LivingEntity entity) {
            float resistancePercentage = 0.0f;
            if (entity instanceof ServerPlayer player) {
                if (!player.hasEffect(ModEffects.COLOSSUS)) {
                    return resistancePercentage;
                }
                AbilityTree abilities = PlayerAbilitiesData.get((ServerLevel) player.level).getAbilities(player);
                for (ColossusAbility ability : abilities.getAll(ColossusAbility.class, Skill::isUnlocked)) {
                    resistancePercentage += ability.getAdditionalResistance();
                }
                return resistancePercentage;
            }
            return resistancePercentage;
        }

        public static float getColossusAOE(LivingEntity entity) {
            float aoePercentage = 0.0f;
            if (entity instanceof ServerPlayer player) {
                if (!player.hasEffect(ModEffects.COLOSSUS)) {
                    return aoePercentage;
                }
                AbilityTree abilities = PlayerAbilitiesData.get((ServerLevel) player.level).getAbilities(player);
                for (ColossusAbility ability : abilities.getAll(ColossusAbility.class, Skill::isUnlocked)) {
                    aoePercentage += ability.getAdditionalResistance();
                }
                return aoePercentage;
            }
            return aoePercentage;
        }
    }
}

