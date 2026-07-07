package xyz.iwolfking.woldsvaults.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.block.VaultCrateBlock;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.AwardCrateObjective;
import iskallia.vault.core.vault.objective.DeathObjective;
import iskallia.vault.core.vault.objective.Objectives;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.init.ModCustomVaultObjectiveEntries;

import javax.annotation.Nullable;
import java.util.Optional;

public class HyperVaultCrystalObjective extends WoldCrystalObjective {
    protected float hyperStatFactor = HyperVaultObjective.HYPER_STAT_FACTOR;

    public HyperVaultCrystalObjective() {
    }

    @Override
    public void configure(Vault vault, RandomSource random, @Nullable String sigil) {
        int level = vault.get(Vault.LEVEL).get();
        vault.ifPresent(Vault.OBJECTIVES, objectives -> {
            objectives.add(HyperVaultObjective.of()
                    .add(AwardCrateObjective.ofConfig(VaultCrateBlock.Type.valueOf("HYPER"), "hyper", level, true)));
            // Deliberately no BailObjective: the exit pillar is the only way out alive.
            objectives.add(DeathObjective.create(true));
            objectives.set(Objectives.KEY, CrystalData.OBJECTIVE.getType(this));
        });
        VaultModifierUtils.addModifier(vault, VaultMod.id("locked"), 1);      // no exit portal
        VaultModifierUtils.addModifier(vault, VaultMod.id("impossible"), 1);  // difficulty lock
        VaultModifierUtils.addModifier(vault, xyz.iwolfking.woldsvaults.WoldsVaults.id("hyper"), 1);
        addSigilStacks(vault, sigil);
    }

    /** Hyper uses its own sigil→challenge-stack table (3/6/12/15/20), not SigilUtils' default. */
    private static void addSigilStacks(Vault vault, @Nullable String sigil) {
        if (sigil == null || vault.get(Vault.MODIFIERS).hasModifier(VaultMod.id("challenged"))) {
            return;
        }
        int stacks = switch (sigil.toLowerCase()) {
            case "adept" -> 3;
            case "expert" -> 6;
            case "master" -> 12;
            case "veteran" -> 15;
            case "legend" -> 20;
            default -> 0;
        };
        if (stacks > 0) {
            VaultModifierUtils.addModifier(vault, VaultMod.id("challenge_stack"), stacks);
            VaultModifierUtils.addModifier(vault, VaultMod.id("challenged"), 1);
        }
    }

    @Override
    ResourceLocation getObjectiveId() {
        return ModCustomVaultObjectiveEntries.HYPER.getRegistryName();
    }

    @Override
    public Optional<Integer> getColor(float time) {
        return Optional.ofNullable(ChatFormatting.GOLD.getColor());
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        CompoundTag nbt = new CompoundTag();
        Adapters.FLOAT.writeNbt(this.hyperStatFactor).ifPresent(tag -> nbt.put("hyper_stat_factor", tag));
        return Optional.of(nbt);
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        this.hyperStatFactor = Adapters.FLOAT.readNbt(nbt.get("hyper_stat_factor")).orElse(HyperVaultObjective.HYPER_STAT_FACTOR);
    }

    @Override
    public Optional<JsonObject> writeJson() {
        JsonObject json = new JsonObject();
        Adapters.FLOAT.writeJson(this.hyperStatFactor).ifPresent(tag -> json.add("hyper_stat_factor", tag));
        return Optional.of(json);
    }

    @Override
    public void readJson(JsonObject json) {
        this.hyperStatFactor = Adapters.FLOAT.readJson(json.get("hyper_stat_factor")).orElse(HyperVaultObjective.HYPER_STAT_FACTOR);
    }
}
