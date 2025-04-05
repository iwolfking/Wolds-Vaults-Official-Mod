package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.ScavengerConfig;
import iskallia.vault.gear.VaultGearRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VaultGearRarityColorConfig extends Config {

    @Expose
    public Map<VaultGearRarity, TextColor> GEAR_RARITY_COLOR_MAP = new HashMap<>();


    @Override
    public String getName() {
        return "gear_rarity_color_config";
    }


    @Override
    protected void reset() {
        GEAR_RARITY_COLOR_MAP = new HashMap<>();
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.SCRAPPY, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.COMMON, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.RARE, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.EPIC, TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE));
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.OMEGA, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
        GEAR_RARITY_COLOR_MAP.put(VaultGearRarity.UNIQUE, TextColor.fromRgb(-1213660));
    }

    public TextColor getColorSafe(VaultGearRarity rarity) {
        return GEAR_RARITY_COLOR_MAP.getOrDefault(rarity, TextColor.fromLegacyFormat(ChatFormatting.WHITE));
    }
}
