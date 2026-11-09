package xyz.iwolfking.woldsvaults.talent.dual_wield;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.net.BitBuffer;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TickingSkill;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectGrantDualWieldTalent extends DualWieldTalent implements TickingSkill {

    private MobEffect effect;
    private int amplifier;

    public EffectGrantDualWieldTalent(int unlockLevel, int learnPointCost, int regretPointCost,
                                     MobEffect effect, int amplifier) {
        super(unlockLevel, learnPointCost, regretPointCost);
        this.effect = effect;
        this.amplifier = amplifier;
    }

    public EffectGrantDualWieldTalent() {
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    @Override
    public void onTick(SkillContext context) {
        if (!this.isUnlocked()) {
            return;
        }

        context.getSource().as(ServerPlayer.class).ifPresent(player -> {
            if (!DualWieldTalent.isDualWielding(player)) {
                if (player.hasEffect(this.effect)) {
                    player.removeEffect(this.effect);
                }
                return;
            }

            player.addEffect(new MobEffectInstance(this.effect, 40, this.amplifier, false, false, true));
        });
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        super.writeBits(buffer);
        Adapters.EFFECT.writeBits(this.effect, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(this.amplifier, buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        super.readBits(buffer);
        this.effect = Adapters.EFFECT.readBits(buffer).orElseThrow();
        this.amplifier = Adapters.INT_SEGMENTED_3.readBits(buffer).orElseThrow();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            Adapters.EFFECT.writeNbt(this.effect).ifPresent(tag -> nbt.put("effect", tag));
            Adapters.INT.writeNbt(this.amplifier).ifPresent(tag -> nbt.put("amplifier", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        this.effect = Adapters.EFFECT.readNbt(nbt.get("effect")).orElseThrow(() -> new IllegalStateException("Unknown effect in " + nbt));
        this.amplifier = Adapters.INT.readNbt(nbt.get("amplifier")).orElse(0);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return super.writeJson().map(json -> {
            Adapters.EFFECT.writeJson(this.effect).ifPresent(element -> json.add("effect", element));
            Adapters.INT.writeJson(this.amplifier).ifPresent(element -> json.add("amplifier", element));
            return json;
        });
    }

    @Override
    public void readJson(JsonObject json) {
        super.readJson(json);
        this.effect = Adapters.EFFECT.readJson(json.get("effect")).orElseThrow(() -> new IllegalStateException("Unknown effect in " + json));
        this.amplifier = Adapters.INT.readJson(json.get("amplifier")).orElse(0);
    }
}