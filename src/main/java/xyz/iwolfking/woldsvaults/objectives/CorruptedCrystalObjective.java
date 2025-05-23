package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.*;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.util.ComponentUtils;
import java.util.List;
import java.util.Optional;


public class CorruptedCrystalObjective extends CrystalObjective {
    protected IntRoll target;
    protected IntRoll secondTarget;
    protected float objectiveProbability;

    public CorruptedCrystalObjective(IntRoll target, IntRoll secondTarget, float objectiveProbability) {
        this.target = target;
        this.secondTarget = secondTarget;
        this.objectiveProbability = objectiveProbability;
    }

    public CorruptedCrystalObjective() {
    }

    @Override
    public Optional<Integer> getColor(float v) {
        return Optional.of(1710361); // Black color
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time) {
        MutableComponent cmp1 = ComponentUtils.corruptComponent(new TextComponent("Objective: "));
        tooltip.add((cmp1)
                .append(new TextComponent("Corrupted")
                        .withStyle(
                                Style.EMPTY.withColor(7995392).withObfuscated(true)
                        )
                )
        );
    }

    @Override
    public void configure(Vault vault, RandomSource random) {
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, (objectives) -> {
            objectives.add(CorruptedObjective.of(this.target.get(random), this.secondTarget.get(random), this.objectiveProbability, ModConfigs.CORRUPTED_OBJECTIVE.getModifierPool(level))
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("CORRUPTED"), "corrupted", level, true))
                    .add(VictoryObjective.of(20*15)));

            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(false));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }


    /* Read/Write methods */
    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.INT_ROLL.writeNbt(this.target).ifPresent((target) -> nbt.put("target", target));
        Adapters.INT_ROLL.writeNbt(this.secondTarget).ifPresent((target) -> nbt.put("second_target", target));
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent((tag) -> nbt.put("objective_probability", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.target = Adapters.INT_ROLL.readNbt(nbt.getCompound("target")).orElse(IntRoll.ofConstant(30));
        this.secondTarget = Adapters.INT_ROLL.readNbt(nbt.getCompound("second_target")).orElse(IntRoll.ofConstant(25));
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.INT_ROLL.writeJson(this.target).ifPresent((target) -> json.add("target", target));
        Adapters.INT_ROLL.writeJson(this.secondTarget).ifPresent((target) -> json.add("second_target", target));
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent((tag) -> json.add("objective_probability", tag));
        return Optional.of(json);
    }
    @Override
    public void readJson(JsonObject json) {
        this.target = Adapters.INT_ROLL.readJson(json.getAsJsonObject("target")).orElse(IntRoll.ofConstant(30));
        this.secondTarget = Adapters.INT_ROLL.readJson(json.getAsJsonObject("second_target")).orElse(IntRoll.ofConstant(35));
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
    }
}