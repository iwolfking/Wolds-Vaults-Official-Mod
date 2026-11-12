package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.ClassicPortalLogic;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.BailObjective;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.objective.FindExitObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;

import java.util.Optional;

public class ExtractionCrystalObjective extends WoldCrystalObjective {
    public ExtractionCrystalObjective() {
    }

    @Override
    public Optional<Integer> getColor(float time) {
        return Optional.of(0xB44BE2);
    }

    @Override
    public void configure(Vault vault, RandomSource random, String sigil) {
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            objectives.add(ExtractionObjective.of()
                    .add(FindExitObjective.create(ClassicPortalLogic.EXIT))
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("EXTRACTION"), "extraction", level, true)));
            objectives.add(BailObjective.create(true, ClassicPortalLogic.EXIT));
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
    }

    @Override
    ResourceLocation getObjectiveId() {
        return ModCustomVaultObjectiveEntries.EXTRACTION.getRegistryName();
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return Optional.of(new CompoundTag());
    }

    @Override
    public void readNbt(CompoundTag nbt) {
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return Optional.of(new JsonObject());
    }

    @Override
    public void readJson(JsonObject json) {
    }
}
