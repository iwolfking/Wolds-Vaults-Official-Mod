package xyz.iwolfking.woldsvaults.config.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class WoldsVaultsConfig
{
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> showVanillaVaultHud;
        public final ForgeConfigSpec.ConfigValue<Boolean> showVanillaVaultMap;
        public Client(ForgeConfigSpec.Builder builder)
        {
            this.showVanillaVaultHud = builder.comment("Whether to show the built-in Vault Inventory HUD").define("showVanillaVaultHud", false);
            this.showVanillaVaultMap = builder.comment("Whether to show the built-in Vault Map in the HUD").define("showVanillaVaultMap", false);
        }
    }
    public static class Common
    {
        //Objectives
        //public final ForgeConfigSpec.ConfigValue<Boolean> disableVanillaUnsupportedElixirEvents;

        public final ForgeConfigSpec.ConfigValue<Boolean> enableMoteRecipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableDebugMode;
        public final ForgeConfigSpec.ConfigValue<Integer> crystalReinforcementMaxCapacityAdded;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableWanderingWispSpawning;
        public final ForgeConfigSpec.ConfigValue<Boolean> weaponsShouldntBeBetter;
        public final ForgeConfigSpec.ConfigValue<Integer> soulFlameLevelAllowance;
        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("Gameplay Settings");
            this.enableMoteRecipes= builder.comment("Controls whether Mote of Purity, Sanctity, and Clarity should work in the Crystal Workbench. (default: false)")
                    .define("enableMoteRecipes", false);
            this.crystalReinforcementMaxCapacityAdded = builder.comment("The max capacity that can be added to a tool with Resonating Reinforcements. (default: 20)").define("resonatingReinforcementMaxCapacityAdded", 20);
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

    public static class Server {

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Balance Settings");
        }
    }

    public static final Common COMMON;
    public static final Client CLIENT;
    public static final Server SERVER;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    static //constructor
    {
        Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();

        Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();

        Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverSpecPair.getLeft();
        SERVER_SPEC = serverSpecPair.getRight();
    }

    public static String getConfigString(String categoryName, String keyName) {
        ForgeConfigSpec.ConfigValue<String> value = COMMON_SPEC.getValues().get(Arrays.asList(categoryName, keyName));
        return value.get();
    }
}