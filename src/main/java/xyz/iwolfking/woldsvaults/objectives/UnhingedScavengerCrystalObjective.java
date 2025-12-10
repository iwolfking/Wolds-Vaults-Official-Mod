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
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;

import java.util.List;
import java.util.Optional;

public class UnhingedScavengerCrystalObjective extends WoldCrystalObjective {
    public UnhingedScavengerCrystalObjective(float objectiveProbability) {
        this.objectiveProbability = objectiveProbability;
    }

    protected float objectiveProbability;

    public UnhingedScavengerCrystalObjective() {
    }

    @Override
    public void configure(Vault vault, RandomSource random) {
        int level = vault.get(Vault.LEVEL).get();

        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            objectives.add(
                UnhingedScavengerObjective.of(this.objectiveProbability, UnhingedScavengerObjective.Config.DEFAULT)
                    .add(
                        AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("UNHINGED_SCAVENGER"), "unhinged_scavenger", level, true))
                    .add(VictoryObjective.of(300)));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }


    public void addText(List<Component> tooltip, TooltipFlag flag, float time) {
        tooltip.add((new TextComponent("Objective: "))
            .append(new TranslatableComponent("vault_objective.woldsvaults.unhinged_scavenger"))
                .withStyle(Style.EMPTY.withColor(getColor(time).orElseThrow())));
    }


    public Optional<Integer> getColor(float time) {
        return Optional.of(Integer.valueOf(3771264));
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(Float.valueOf(this.objectiveProbability))
            .ifPresent(tag -> nbt.put("objective_probability", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent(tag -> json.add("objective_probability", tag));
        return Optional.of(json);
    }


    @Override
    public void readJson(JsonObject json) {
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
    }

    @Override
    ResourceLocation getObjectiveId() {
        return ModCustomVaultObjectiveEntries.UNHINGED_SCAVENGER_HUNT.getRegistryName();
    }
}
