package xyz.iwolfking.woldsvaults.objectives;

import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.*;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Optional;
import java.util.function.IntSupplier;

public class CompoundCrystalObjective extends WoldCrystalObjective{
    @Override
    ResourceLocation getObjectiveId() {
        return WoldsVaults.id("compound_objective");
    }

    @Override
    public Optional<Integer> getColor(float v) {
        return Optional.of(3093151);
    }

    @Override
    public void configure(Vault vault, RandomSource random, @Nullable String sigil) {
        int level = (vault.get(Vault.LEVEL)).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            objectives.add(ZealotObjective.of(10, 0).add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("ZEALOT"), "zealot", level, true)));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }
}
