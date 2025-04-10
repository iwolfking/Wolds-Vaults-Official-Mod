package xyz.iwolfking.woldsvaults.config.fake;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomVaultGearModelRollRaritiesConfig {

    public static final Map<VaultGearRarity, List<String>> BATTLESTAFF_MODEL_ROLLS = new HashMap<>();
    public static final Map<VaultGearRarity, List<String>> TRIDENT_MODEL_ROLLS = new HashMap<>();
    public static final Map<VaultGearRarity, List<String>> PLUSHIE_MODEL_ROLLS = new HashMap<>();
    public static final Map<VaultGearRarity, List<String>> LOOT_SACKS_MODEL_ROLLS = new HashMap<>();
    public static final Map<VaultGearRarity, List<String>> RANG_MODEL_ROLLS = new HashMap<>();
    public static final Map<VaultGearRarity, List<String>> MAP_MODEL_ROLLS = new HashMap<>();

    static {
        BATTLESTAFF_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, Battlestaffs.REGISTRY
                .getIds().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList()));
        TRIDENT_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, Tridents.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        PLUSHIE_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, Plushies.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        LOOT_SACKS_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, LootSacks.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        RANG_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, Rangs.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        MAP_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY, (List<String>) Maps.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
    }


}
