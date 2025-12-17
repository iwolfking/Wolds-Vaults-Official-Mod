package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.HashMap;
import java.util.Map;

public class TrinketPouchConfig extends Config {
    @Expose
    public Map<ResourceLocation, TrinketPouchConfigEntry> TRINKET_POUCH_CONFIGS;

    @Override
    public String getName() {
        return "trinket_pouch";
    }

    @Override
    protected void reset() {
        TRINKET_POUCH_CONFIGS = new HashMap<>();
        TRINKET_POUCH_CONFIGS.put(WoldsVaults.id("standard"), new TrinketPouchConfigEntry("Standard Trinket Pouch", Map.of("red_trinket", 1, "blue_trinket", 1, "green_trinket", 1), TextColor.fromLegacyFormat(ChatFormatting.WHITE)));
        TRINKET_POUCH_CONFIGS.put(WoldsVaults.id("heavy"), new TrinketPouchConfigEntry("Heavy Trinket Pouch", Map.of("red_trinket", 2,  "green_trinket", 1), TextColor.fromLegacyFormat(ChatFormatting.RED)));
        TRINKET_POUCH_CONFIGS.put(WoldsVaults.id("light"), new TrinketPouchConfigEntry("Light Trinket Pouch", Map.of("blue_trinket", 2,  "green_trinket", 1), TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
        TRINKET_POUCH_CONFIGS.put(WoldsVaults.id("explorer"), new TrinketPouchConfigEntry("Explorer's Trinket Pouch", Map.of("green_trinket", 2,  "blue_trinket", 1), TextColor.fromLegacyFormat(ChatFormatting.GREEN)));
    }

    public static class TrinketPouchConfigEntry {
        @Expose
        public String NAME;

        @Expose
        public Map<String, Integer> SLOT_ENTRIES;

        @Expose
        public TextColor COLOR;

        public TrinketPouchConfigEntry(String NAME, Map<String, Integer> SLOT_ENTRIES, TextColor COLOR) {
            this.NAME = NAME;
            this.SLOT_ENTRIES = SLOT_ENTRIES;
            this.COLOR = COLOR;
        }
    }
}
