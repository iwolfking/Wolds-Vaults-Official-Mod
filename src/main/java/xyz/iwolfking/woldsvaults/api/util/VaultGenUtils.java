package xyz.iwolfking.woldsvaults.api.util;


import iskallia.vault.core.util.RegionPos;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.ArchitectObjective;
import iskallia.vault.core.world.generator.layout.*;
import iskallia.vault.item.crystal.layout.preset.PoolKeyTemplatePreset;
import iskallia.vault.item.crystal.layout.preset.StructurePreset;

public class VaultGenUtils {
    public static boolean isInscriptionRoom(Vault vault, RegionPos regionPos) {
        if (vault == null || regionPos == null) return false;

        ArchitectObjective architectObj = WoldVaultUtils.getObjective(vault, ArchitectObjective.class);
        if(architectObj != null) {
            StructurePreset preset = architectObj.get(ArchitectObjective.PRESET);
            if (preset == null) return false;

            return preset.get(regionPos)
                    .map(p -> p instanceof PoolKeyTemplatePreset)
                    .orElse(false);
        }

        return false;
    }
}
