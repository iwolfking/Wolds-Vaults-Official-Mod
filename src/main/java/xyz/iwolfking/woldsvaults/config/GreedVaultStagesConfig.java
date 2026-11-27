package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The greed stages, generated as {@code vault_configs/greed/stages/vault_stages.json}. Each stage, keyed by its
 * number, has the medallion rank it unlocks at and its pairs: the normal palette or pool id VH reads and the stage id
 * a vault at or above that rank reads instead. A normal id may be paired by several stages; the highest stage the
 * vault has reached wins.
 */
public class GreedVaultStagesConfig extends Config implements WoldsTable {

    public static class Stage {
        @Expose public int unlockRank;
        @Expose public Map<String, String> palettes;
        @Expose public Map<String, String> pools;
    }

    @Expose public Map<String, Stage> stages;

    @Override
    public String getName() {
        return "greed" + File.separator + "vault_stages";
    }

    /**
     * Null when the table can be installed. Refuses stages out of unlock order, malformed ids, and any stage id that
     * is also a normal id, so the redirect stays one-way.
     */
    @Nullable
    @Override
    public String validate() {
        if (this.stages == null || this.stages.isEmpty()) {
            return "it lists no stages";
        }
        Set<String> normalPalettes = new HashSet<>();
        Set<String> normalPools = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        int lastNumber = 0;
        int lastRank = -1;
        for (Map.Entry<String, Stage> entry : this.stages.entrySet()) {
            int number;
            try {
                number = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                return "stage key " + entry.getKey() + " is not a number";
            }
            Stage stage = entry.getValue();
            if (stage == null || stage.palettes == null || stage.pools == null) {
                return "stage " + number + " is missing its palette or pool pairs";
            }
            if (number <= lastNumber || stage.unlockRank <= lastRank) {
                return "stage " + number + " is out of order: stages must rise in number and unlock rank";
            }
            lastNumber = number;
            lastRank = stage.unlockRank;
            String problem = collect(number, stage.palettes, normalPalettes, stageIds);
            if (problem == null) {
                problem = collect(number, stage.pools, normalPools, stageIds);
            }
            if (problem != null) {
                return problem;
            }
        }
        for (String stageId : stageIds) {
            if (normalPalettes.contains(stageId) || normalPools.contains(stageId)) {
                return "stage id " + stageId + " is also a normal id";
            }
        }
        return null;
    }

    @Nullable
    private static String collect(int number, Map<String, String> pairs, Set<String> normals, Set<String> stageIds) {
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            if (ResourceLocation.tryParse(pair.getKey()) == null || pair.getValue() == null || ResourceLocation.tryParse(pair.getValue()) == null) {
                return "stage " + number + " has a malformed pair " + pair.getKey() + " -> " + pair.getValue();
            }
            normals.add(pair.getKey());
            if (pair.getKey().equals(pair.getValue())) {
                return "stage " + number + " pairs " + pair.getKey() + " with itself";
            }
            stageIds.add(pair.getValue());
        }
        return null;
    }

    @Override
    protected void reset() {
    }
}
