package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.array.ArrayAdapter;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.*;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.TickTimer;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SurvivalCrystalObjective extends WoldCrystalObjective {
    protected float objectiveProbability;
    protected int target;
    protected List<String> waveGroups = new ArrayList<>();

    public SurvivalCrystalObjective(float objectiveProbability, int target, List<String> waveGroups) {
        this.objectiveProbability = objectiveProbability;
        this.target = target;
        this.waveGroups = waveGroups;
    }

    public SurvivalCrystalObjective() {
    }

    @Override
    public Optional<Integer> getColor(float v) {
        return Optional.of(0x334324);
    }

    @Override
    public void configure(Vault vault, RandomSource random, String sigil) {
        int level = vault.get(Vault.LEVEL).get();

        vault.ifPresent(Vault.OBJECTIVES, objectives -> {

            objectives.add(SurvivalObjective.of(this.objectiveProbability, target, waveGroups)
                    .add(FindExitObjective.create(ClassicPortalLogic.EXIT))
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("SURVIVAL"), "survival", level, true)));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    ResourceLocation getObjectiveId() {
        return ModCustomVaultObjectiveEntries.SURVIVAL.getRegistryName();
    }

    public static final ArrayAdapter<String> GROUPS = Adapters.ofArray(String[]::new, Adapters.UTF_8);

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent(tag -> nbt.put("objective_probability", tag));
        Adapters.INT.writeNbt(this.target).ifPresent(tag -> nbt.put("target", tag));
        nbt.put("waveGroups", (Tag)GROUPS.writeNbt((String[])this.waveGroups.toArray(String[]::new)).orElseThrow());
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.5F);
        this.target = Adapters.INT.readNbt(nbt.get("target")).orElse(10);
        this.waveGroups = Arrays.stream((String[])GROUPS.readNbt(nbt.get("waveGroups")).orElse(new String[0])).collect(Collectors.toList());
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent(tag -> json.add("objective_probability", tag));
        Adapters.INT.writeJson(this.target).ifPresent(tag -> json.add("target", tag));
        json.add("waveGroups", (JsonElement)GROUPS.writeJson((String[])this.waveGroups.toArray(String[]::new)).orElseThrow());
        return Optional.of(json);
    }

    @Override
    public void readJson(JsonObject json) {
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.5F);
        this.target = Adapters.INT.readJson(json.get("target")).orElse(10);
        this.waveGroups = Arrays.stream((String[])GROUPS.readJson(json.get("waveGroups")).orElse(new String[0])).collect(Collectors.toList());
    }
}
