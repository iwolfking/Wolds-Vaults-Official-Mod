package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.config.sigil.SigilConfig;
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
import xyz.iwolfking.scalingbingoseals.util.INamedObjective;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ScalingBallisticBingoCrystalObjective extends CrystalObjective implements INamedObjective {
    private float objectiveProbability;
    private int sealCount;

    public static int DEFAULT_WIDTH = 6;
    public static int DEFAULT_HEIGHT = 6;

    public ScalingBallisticBingoCrystalObjective(float objectiveProbability, int sealCount) {
        this.objectiveProbability = objectiveProbability;
        this.sealCount = sealCount;
    }

    public ScalingBallisticBingoCrystalObjective() {
        this.sealCount = 1;
    }

    @Override
    public Optional<Integer> getColor(float v) {
        return Optional.of(4821183);
    }

    @Override
    public void configure(Vault vault, RandomSource random, @Nullable String sigil) {
        int level = vault.get(Vault.LEVEL).get();
        Optional<SigilConfig.LevelEntry> entry = SigilConfig.getConfig(sigil).map((config) -> config.getLevel(level));
        vault.ifPresent(Vault.OBJECTIVES, (objectives) -> {
            ModConfigs.BALLISTIC_BINGO_CONFIG.generate(entry.map(SigilConfig.LevelEntry::getBingoPool).orElse(VaultMod.id("default")), level).ifPresent((task) -> objectives.add(BallisticBingoObjective.of(task, getWidth(), getHeight()).add(GridGatewayObjective.of(this.objectiveProbability).add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("BALLISTIC_BINGO"), "ballistic_bingo", level, true)).add(VictoryObjective.of(300)))));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
        tooltip.add((new TextComponent("Objective: ")).append((new TextComponent(getHeight() + "x" + getWidth() + " Ballistic Bingo")).withStyle(Style.EMPTY.withColor((Integer)this.getColor(time).orElseThrow()))));
    }

    public int getHeight() {
        return DEFAULT_HEIGHT + sealCount - 1;
    }

    public int getWidth() {
        return DEFAULT_WIDTH + sealCount - 1;
    }

    public int getSealCount() {
        return Math.min(this.sealCount, 6);
    }

    public float getObjectiveProbability() {
        return this.objectiveProbability;
    }

    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(this.objectiveProbability).ifPresent((tag) -> {
            nbt.put("objective_probability", tag);
        });
        Adapters.INT.writeNbt(this.sealCount).ifPresent(tag -> {
            nbt.put("sealCount", tag);
        });
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
        this.sealCount = Adapters.INT.readNbt(nbt.get("sealCount")).orElse(1);
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent((tag) -> {
            json.add("objective_probability", tag);
        });
        Adapters.INT.writeJson(this.sealCount).ifPresent((tag) -> {
            json.add("sealCount", tag);
        });
        return Optional.of(json);
    }

    public String getCrystalName() {
        return getHeight() + "x" + getWidth() + " Ballistic Bingo Crystal";
    }

    public void readJson(JsonObject json) {
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
        this.sealCount = Adapters.INT.readJson(json.get("sealCount")).orElse(1);
    }
}
