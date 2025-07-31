package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.*;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class AlchemyCrystalObjective extends CrystalObjective {
    protected float objectiveProbability;
    protected float requiredProgress;

    public AlchemyCrystalObjective(float objectiveProbability, float requiredProgress) {
        this.objectiveProbability = objectiveProbability;
        this.requiredProgress = requiredProgress;
    }

    public AlchemyCrystalObjective() {
    }

    @Override
    public Optional<Integer> getColor(float v) {
        return Optional.of(0xB68CFF);
    }

    @Override
    public void configure(Vault vault, RandomSource random) {
        int level = vault.get(Vault.LEVEL).get();

        vault.ifPresent(Vault.OBJECTIVES, objectives -> {

            objectives.add(AlchemyObjective.of(this.objectiveProbability, level, requiredProgress)
                    .add(FindExitObjective.create(ClassicPortalLogic.EXIT))
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("ALCHEMY"), "alchemy", level, true)));

            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        tooltip.add((new TextComponent("Objective: ")).append((new TextComponent("Alchemy")).withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()))));
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent(tag -> nbt.put("objective_probability", tag));
        Adapters.FLOAT.writeNbt(this.requiredProgress).ifPresent(tag -> nbt.put("required_progress", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.5F);
        this.requiredProgress = Adapters.FLOAT.readNbt(nbt.get("required_progress")).orElse(2.5F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent(tag -> json.add("objective_probability", tag));
        Adapters.FLOAT.writeJson(this.requiredProgress).ifPresent(tag -> json.add("required_progress", tag));
        return Optional.of(json);
    }

    @Override
    public void readJson(JsonObject json) {
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.5F);
        this.requiredProgress = Adapters.FLOAT.readJson(json.get("required_progress")).orElse(2.5F);
    }
}
