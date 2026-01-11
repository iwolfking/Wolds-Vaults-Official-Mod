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
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            IntSupplier limitedWave = () -> random.nextInt(3) + 1;

            int obelisks = random.nextInt(3) + 3;

            objectives.add(BrutalBossesObjective.of(obelisks, limitedWave, 0.25F)
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("BRUTAL_BOSSES"), "brutal_bosses", level, true))
                    .add(VictoryObjective.of(300)));
//            ElixirObjective elixirObjective = ElixirObjective.create().withTargetMultiplier(1.0F);
//            objectives.add(elixirObjective.add(LodestoneObjective.of(0.25F).add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.ELIXIR, "elixir", level, true)).add(VictoryObjective.of(300))));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }
}
