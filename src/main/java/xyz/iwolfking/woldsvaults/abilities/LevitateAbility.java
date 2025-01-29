package xyz.iwolfking.woldsvaults.abilities;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.init.ModParticles;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.HoldManaAbility;
import iskallia.vault.skill.base.SkillContext;
import java.util.Optional;

//import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import xyz.iwolfking.woldsvaults.init.ModEffects;

public class LevitateAbility extends HoldManaAbility {
    private int levitateStrength;

    public LevitateAbility(
            int unlockLevel,
            int learnPointCost,
            int regretPointCost,
            int cooldownTicks,
            float manaCostPerSecond,
            int levitateStrength
    ) {
        super(unlockLevel, learnPointCost, regretPointCost, cooldownTicks, manaCostPerSecond);
        this.levitateStrength = levitateStrength;
    }

    public LevitateAbility() {}
    public float getLevitateSpeed() {
        return ( this.levitateStrength+1 ) * 0.9f;
    }
    public int getLevitateStrength() {
        return this.levitateStrength;
    }

    protected void doParticles(SkillContext context) {
        context.getSource()
                .as(ServerPlayer.class)
                .ifPresent(
                        player -> ((ServerLevel)player.level).sendParticles(ModParticles.NOVA_CLOUD.get(), player.getX(), player.getY(), player.getZ(), 2, 0.2, 0.5, 0.2, 0.05)
                );
    }

    @Override
    public Ability.TickResult doActiveTick(SkillContext context) {
        return context.getSource().as(ServerPlayer.class).map(player -> {

            Ability.TickResult result = super.doActiveTick(context);
            if (result != Ability.TickResult.COOLDOWN) {
                MobEffectInstance newEffect = new MobEffectInstance(ModEffects.LEVITATEII, 100, levitateStrength, false, false, true);
                player.addEffect(newEffect);
                doParticles(context);
            }

            return result;

        }).orElse(Ability.TickResult.PASS);
    }

    @Override
    public boolean onKeyUp(SkillContext context) {
            context.getSource().as(ServerPlayer.class).map(player -> {
                player.removeEffect(ModEffects.LEVITATEII);
                return true;
            });
        return super.onKeyUp(context);
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.INT_SEGMENTED_7.writeBits(Integer.valueOf(this.levitateStrength), buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.levitateStrength = Adapters.INT_SEGMENTED_7.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.INT.writeNbt(Integer.valueOf(this.levitateStrength)).ifPresent(tag -> nbt.put("levitateStrength", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.levitateStrength = Adapters.INT.readNbt(nbt.get("levitateStrength")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.INT.writeJson(Integer.valueOf(this.levitateStrength)).ifPresent(element -> json.add("levitateStrength", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.levitateStrength = Adapters.INT.readJson(json.get("levitateStrength")).orElse(0);
    }
}