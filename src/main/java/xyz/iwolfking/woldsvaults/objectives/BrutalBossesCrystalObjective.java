package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.BailObjective;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.objective.Objectives;
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

import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

public class BrutalBossesCrystalObjective extends CrystalObjective {
    protected IntRoll target;
    protected IntRoll wave;
    protected float objectiveProbability;

    public BrutalBossesCrystalObjective() {
    }

    public BrutalBossesCrystalObjective(IntRoll target, IntRoll wave, float objectiveProbability) {
        this.target = target;
        this.wave = wave;
        this.objectiveProbability = objectiveProbability;
    }

    @Override
    public void configure(Vault vault, RandomSource random) {
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            IntSupplier limitedWave = () -> random.nextInt(3) + 1;

            int obelisks = random.nextInt(3) + 3;

            objectives.add(BrutalBossesObjective.of(obelisks, limitedWave, this.objectiveProbability)
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.BOSS, "brutal_bosses", level, true))
                    .add(VictoryObjective.of(300)));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("Slay the Brutal Bosses")).withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()))));
    }

    public Optional<Integer> getColor(float time) {
        return Optional.ofNullable(ChatFormatting.RED.getColor());
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
