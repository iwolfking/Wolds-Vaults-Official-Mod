package xyz.iwolfking.woldsvaults.config.forge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class WoldsVaultsConfig
{
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> syncJEISearchForWorkstations;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.push("Display Settings");
            builder.push("JEI Sync");
            this.syncJEISearchForWorkstations = builder.comment("Whether workstations should have their inventory filtered with JEI search (default: true)").define("syncJEISearchForWorkstations", true);
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

        //Objectives
        //public final ForgeConfigSpec.ConfigValue<Boolean> disableVanillaUnsupportedElixirEvents;

        public final ForgeConfigSpec.ConfigValue<Boolean> disableFlightInVaults;
        public final ForgeConfigSpec.ConfigValue<Boolean> normalizedModifierEnabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableMoteRecipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableDebugMode;
        public final ForgeConfigSpec.ConfigValue<Integer> crystalReinforcementMaxCapacityAdded;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableWanderingWispSpawning;
        public final ForgeConfigSpec.ConfigValue<Boolean> weaponsShouldntBeBetter;
        public final ForgeConfigSpec.ConfigValue<Integer> soulFlameLevelAllowance;
        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("Features");
            builder.push("Items");
            this.enableVaultBattlestaff = builder.comment("Whether to register and enable Vault Battlestaff or not").define("enableVaultBattlestaff", true);
            this.enableVaultTrident = builder.comment("Whether to register and enable Vault Trident or not").define("enableVaultTrident", true);
            this.enableVaultPlushie = builder.comment("Whether to register and enable Vault Plushie or not").define("enableVaultPlushie", true);
            this.enableVaultLootSack = builder.comment("Whether to register and enable Vault Loot Sack or not").define("enableVaultLootSack", true);
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
            this.weaponsShouldntBeBetter = builder.comment("Whether Better Combat should be disabled for weapons or not (note: this is a client-side setting, don't change on servers) (default: false)").define("weaponsShouldntBeBetter", false);
            this.soulFlameLevelAllowance = builder.comment("How much a difference in levels should be allowed for Soul Flames before not raising stacks (default: 3)").define("soulFlameLevelAllowance", 3);
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