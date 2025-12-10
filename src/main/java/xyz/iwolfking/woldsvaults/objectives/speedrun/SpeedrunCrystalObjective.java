package xyz.iwolfking.woldsvaults.objectives.speedrun;

import com.google.gson.JsonObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.BailObjective;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.core.vault.objective.TrackSpeedrunObjective;
import iskallia.vault.core.vault.objective.VictoryObjective;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.objectives.BrutalBossesObjective;

import java.util.List;
import java.util.Optional;

public class SpeedrunCrystalObjective extends CrystalObjective {
    protected IntRoll target;
    protected IntRoll wave;
    protected float objectiveProbability;
    public SpeedrunCrystalObjective() {
    }

    public SpeedrunCrystalObjective(IntRoll target, IntRoll wave, float objectiveProbability) {
        this.target = target;
        this.wave = wave;
        this.objectiveProbability = objectiveProbability;
    }

//    @Override
//    public void configure(Vault vault, RandomSource random) {
//        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
//            objectives.add(BrutalBossesObjective.of(this.target.get(random), () -> this.wave.get(random), this.objectiveProbability).add(VictoryObjective.of(300)));
//            objectives.add(DeathObjective.create(false));
//            objectives.add(TrackSpeedrunObjective.create());
//            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
//            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
//        });
//    }
//
//    @Override
//    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
//        tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("Brutal Bosses Speedrun")).withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()))));
//    }

    public Optional<Integer> getColor(float time) {
        return Optional.ofNullable(ChatFormatting.AQUA.getColor());
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.INT_ROLL.writeNbt(this.target).ifPresent(target -> nbt.put("target", target));
        Adapters.INT_ROLL.writeNbt(this.wave).ifPresent(wave -> nbt.put("wave", wave));
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent(tag -> nbt.put("objective_probability", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.target = Adapters.INT_ROLL.readNbt(nbt.getCompound("target")).orElse(null);
        this.wave = Adapters.INT_ROLL.readNbt(nbt.getCompound("wave")).orElse(IntRoll.ofConstant(3));
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.INT_ROLL.writeJson(this.target).ifPresent(target -> json.add("target", target));
        Adapters.INT_ROLL.writeJson(this.wave).ifPresent(wave -> json.add("wave", wave));
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent(tag -> json.add("objective_probability", tag));
        return Optional.of(json);
    }

    @Override
    public void readJson(JsonObject json) {
        this.target = Adapters.INT_ROLL.readJson(json.getAsJsonObject("target")).orElse(null);
        this.wave = Adapters.INT_ROLL.readJson(json.getAsJsonObject("wave")).orElse(IntRoll.ofConstant(3));
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
    }
}
