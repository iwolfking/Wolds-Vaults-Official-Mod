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
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import xyz.iwolfking.scalingbingoseals.util.INamedObjective;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;
import xyz.iwolfking.woldsvaults.objectives.lib.ScalingObjective;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ScalingBallisticBingoCrystalObjective extends WoldCrystalObjective implements INamedObjective, ScalingObjective {
    private float objectiveProbability;
    private int sealCount;
    protected boolean blackout;


    public static int DEFAULT_WIDTH = 5;
    public static int DEFAULT_HEIGHT = 5;

    public ScalingBallisticBingoCrystalObjective(float objectiveProbability, int sealCount, boolean blackout) {
        this.objectiveProbability = objectiveProbability;
        this.sealCount = sealCount;
        this.blackout = blackout;
    }

    public ScalingBallisticBingoCrystalObjective(float objectiveProbability, int sealCount) {
        this(objectiveProbability, sealCount, false);
    }
    public ScalingBallisticBingoCrystalObjective(int sealCount, boolean blackout) {
        this.sealCount = sealCount;
        this.blackout = blackout;
    }

    public ScalingBallisticBingoCrystalObjective(boolean blackout) {
        this(1, blackout);
    }

    public ScalingBallisticBingoCrystalObjective() {
        this(false);
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
            ModConfigs.BALLISTIC_BINGO_CONFIG.generate(entry.map(SigilConfig.LevelEntry::getBingoPool).orElse(VaultMod.id("default")), level).ifPresent((task) -> objectives.add(BallisticBingoObjective.of(task, getWidth(), getHeight(), blackout).add(GridGatewayObjective.of(this.objectiveProbability).add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("BALLISTIC_BINGO"), "ballistic_bingo", level, true)).add(VictoryObjective.of(300)))));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    public ScalingBallisticBingoCrystalObjective increaseBy(int extraSeals) {
        return new ScalingBallisticBingoCrystalObjective(this.objectiveProbability, Math.min(sealCount + extraSeals, getMaxSealCount()));
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
        MutableComponent objectiveTooltip = new TranslatableComponent("util.woldsvaults.objective_text")
                .withStyle(ChatFormatting.WHITE);

        if(blackout) {
            objectiveTooltip.append("Blackout ").withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()));
        }

        var size = new TextComponent(getHeight() + "x" + getWidth() + " ").withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()));
        Component objectiveName = new TranslatableComponent(
                "objective." + getObjectiveId().toString().replace(":", "."))
                .withStyle(Style.EMPTY.withColor(this.getColor(time).orElseThrow()));

        tooltip.add(objectiveTooltip.append(size).append(objectiveName));
    }

    public int getHeight() {
        return DEFAULT_HEIGHT + sealCount - 1;
    }

    public int getWidth() {
        return DEFAULT_WIDTH + sealCount - 1;
    }

    public int getSealCount() {
        return Math.min(this.sealCount, getMaxSealCount());
    }

    public int getMaxSealCount() {
        return 8;
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
        Adapters.BOOLEAN.writeNbt(this.blackout).ifPresent((tag) -> nbt.put("blackout", tag));
        return Optional.of(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.objectiveProbability = Adapters.FLOAT.readNbt(nbt.get("objective_probability")).orElse(0.0F);
        this.sealCount = Adapters.INT.readNbt(nbt.get("sealCount")).orElse(1);
        this.blackout = Adapters.BOOLEAN.readNbt(nbt.get("blackout")).orElse(false);
    }

    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.objectiveProbability).ifPresent((tag) -> {
            json.add("objective_probability", tag);
        });
        Adapters.INT.writeJson(this.sealCount).ifPresent((tag) -> {
            json.add("sealCount", tag);
        });
        Adapters.BOOLEAN.writeJson(this.blackout).ifPresent((tag) -> json.add("blackout", tag));
        return Optional.of(json);
    }

    public String getCrystalName() {
        return getHeight() + "x" + getWidth() + " Ballistic Bingo Crystal";
    }

    public void readJson(JsonObject json) {
        this.objectiveProbability = Adapters.FLOAT.readJson(json.get("objective_probability")).orElse(0.0F);
        this.sealCount = Adapters.INT.readJson(json.get("sealCount")).orElse(1);
        this.blackout = Adapters.BOOLEAN.readJson(json.get("blackout")).orElse(false);
    }

    @Override
    ResourceLocation getObjectiveId() {
        return ModCustomVaultObjectiveEntries.BALLISTIC_BINGO.getRegistryName();
    }
}
