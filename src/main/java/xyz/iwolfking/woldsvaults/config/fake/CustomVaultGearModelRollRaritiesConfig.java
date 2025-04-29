package xyz.iwolfking.woldsvaults.config.fake;

import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomVaultGearModelRollRaritiesConfig {

    public static final Map<String, List<String>> BATTLESTAFF_MODEL_ROLLS = new HashMap<>();
    public static final Map<String, List<String>> TRIDENT_MODEL_ROLLS = new HashMap<>();
    public static final Map<String, List<String>> PLUSHIE_MODEL_ROLLS = new HashMap<>();
    public static final Map<String, List<String>> LOOT_SACKS_MODEL_ROLLS = new HashMap<>();
    public static final Map<String, List<String>> RANG_MODEL_ROLLS = new HashMap<>();
    public static final Map<String, List<String>> MAP_MODEL_ROLLS = new HashMap<>();

    static {
        BATTLESTAFF_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), Battlestaffs.REGISTRY
                .getIds().stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toList()));
        TRIDENT_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), Tridents.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        PLUSHIE_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), Plushies.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        LOOT_SACKS_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), LootSacks.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        RANG_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), Rangs.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
        MAP_MODEL_ROLLS.put(VaultGearRarity.SCRAPPY.name(), Maps.REGISTRY.getIds().stream()
                .map(ResourceLocation::toString).collect(Collectors.toList()));
    }


}
