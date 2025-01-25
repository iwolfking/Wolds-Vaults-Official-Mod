package xyz.iwolfking.woldsvaults.config.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class WoldsVaultsConfig
{
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> hideEmojisOnCrystalModifiers;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.push("Display Settings");
            builder.push("Emojiful");
            this.hideEmojisOnCrystalModifiers = builder.comment("Whether Emojis should be hidden next to Vault Crystal Modifiers. (default: false)").define("hideEmojisOnVaultModifiers", false);
            builder.pop();
            builder.pop();
        }
    }
    public static class Common
    {
        //Features
        //Items
        //Gear
        public final ForgeConfigSpec.ConfigValue<Boolean> enableVaultTrident;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableVaultBattlestaff;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableVaultPlushie;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableVaultLootSack;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableRunningShoesTrinket;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableMinersHeadlampTrinket;

        //Objectives
        //public final ForgeConfigSpec.ConfigValue<Boolean> disableVanillaUnsupportedElixirEvents;

        public final ForgeConfigSpec.ConfigValue<Boolean> disableFlightInVaults;
        public final ForgeConfigSpec.ConfigValue<Boolean> normalizedModifierEnabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableMoteRecipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> displayItemBordersInTerminals;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableDebugMode;
        public final ForgeConfigSpec.ConfigValue<Integer> crystalReinforcementMaxCapacityAdded;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableWanderingWispSpawning;
        public final ForgeConfigSpec.ConfigValue<Boolean> weaponsShouldntBeBetter;
        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("Features");
            builder.push("Items");
            this.enableVaultBattlestaff = builder.comment("Whether to register and enable Vault Battlestaff or not").define("enableVaultBattlestaff", true);
            this.enableVaultTrident = builder.comment("Whether to register and enable Vault Trident or not").define("enableVaultTrident", true);
            this.enableVaultPlushie = builder.comment("Whether to register and enable Vault Plushie or not").define("enableVaultPlushie", true);
            this.enableVaultLootSack = builder.comment("Whether to register and enable Vault Loot Sack or not").define("enableVaultLootSack", true);
            this.enableRunningShoesTrinket = builder.comment("Whether to enable the Running Shoes trinket or not").define("enableRunningShoes", true);
            this.enableMinersHeadlampTrinket = builder.comment("Whether to enable the Miner's Headlamp trinket or not").define("enableMinersHeadlamp", true);
            builder.pop();
            builder.pop();
            builder.push("Vault Settings");
            this.disableFlightInVaults= builder.comment("Controls whether Creative flight should be blocked while inside a vault. (default: true)")
                    .define("disableFlightInVaults", true);
            this.normalizedModifierEnabled = builder.comment("Whether the \"Normalized\" Modifier should be enabled and functional (default: true)")
                    .define("normalizedModifierEnabled", true);
            builder.pop();
            builder.push("Gameplay Settings");
            this.enableMoteRecipes= builder.comment("Controls whether Mote of Purity, Sanctity, and Clarity should work in the Crystal Workbench. (default: false)")
                    .define("enableMoteRecipes", false);
            this.crystalReinforcementMaxCapacityAdded = builder.comment("The max capacity that can be added to a tool with Crystal Reinforcements. (default: 20)").define("crystalReinforcementMaxCapacityAdded", 20);
            this.disableWanderingWispSpawning = builder.comment("Controls whether or not wandering wisps should spawn (default: true)").define("disableWanderingWispSpawning", true);
            this.weaponsShouldntBeBetter = builder.comment("Whether Better Combat should be disabled for weapons or not (default: false)").define("weaponsShouldntBeBetter", false);
            builder.pop();
            builder.push("Compatability Settings");
            builder.push("Item Borders");
            this.displayItemBordersInTerminals= builder.comment("Whether Item Borders from the Item Borders mod should show in terminals like Refined Storage, AE2, etc., this will also cause them to show on the hot-bar. (default: true)")
                    .define("displayItemBordersInTerminals", true);
            builder.pop();
            builder.pop();
            builder.push("Developer Settings");
            this.enableDebugMode= builder.comment("Don't recommend turning on unless asked, enables debug messages for development. (default: false)")
                    .define("enableDebugMode", false);
            builder.pop();
        }
    }

    public static final Common COMMON;
    public static final Client CLIENT;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static //constructor
    {
        Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();

        Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }

    public static String getConfigString(String categoryName, String keyName) {
        ForgeConfigSpec.ConfigValue<String> value = COMMON_SPEC.getValues().get(Arrays.asList(categoryName, keyName));
        return value.get();
    }
}