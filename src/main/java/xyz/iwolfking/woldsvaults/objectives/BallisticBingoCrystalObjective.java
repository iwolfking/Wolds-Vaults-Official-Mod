package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.BailObjective;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.objective.GridGatewayObjective;
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
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BallisticBingoCrystalObjective extends WoldCrystalObjective {
    protected float objectiveProbability;

    public BallisticBingoCrystalObjective() {
    }

    public BallisticBingoCrystalObjective(float objectiveProbability) {
        this.objectiveProbability = objectiveProbability;
    }

    @Override
    public void configure(Vault vault, RandomSource random, @Nullable String sigil) {
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            ModConfigs.BALLISTIC_BINGO_CONFIG.generate(VaultMod.id("default"), level).ifPresent(task ->
                objectives.add(BallisticBingoObjective.of(task).add(GridGatewayObjective.of(this.objectiveProbability)
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("BALLISTIC_BINGO"), "bingo", level, true))
                    .add(VictoryObjective.of(300)))));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    public Optional<Integer> getColor(float time) {
        return Optional.of(3093081);
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent(tag -> nbt.put("objective_probability", tag));
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
        return ModCustomVaultObjectiveEntries.BALLISTIC_BINGO.getRegistryName();
    }
}
