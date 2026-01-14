package xyz.iwolfking.woldsvaults.config.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class WoldsVaultsConfig
{
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> showVanillaVaultMap;
        public final ForgeConfigSpec.ConfigValue<Boolean> hideXaerosMinimapInVaults;
        public final ForgeConfigSpec.ConfigValue<Boolean> playVaultMusic;
        public final ForgeConfigSpec.ConfigValue<Boolean> weaponsShouldntBeBetter;

        public final ForgeConfigSpec.ConfigValue<OutputFormat> tooltipOutputFormat;

        public Client(ForgeConfigSpec.Builder builder)
        {
            this.showVanillaVaultMap = builder.comment("Whether to show the built-in Vault Map in the HUD").define("showVanillaVaultMap", false);
            this.hideXaerosMinimapInVaults = builder.comment("Whether to hide Xaero's Minimap while in Vaults.").define("hideXaerosMinimapInVaults", false);
            this.playVaultMusic = builder.comment("Whether to play special tracks while inside the Vault").define("playVaultMusic", false);
            this.weaponsShouldntBeBetter = builder.comment("Whether to enable Better Combat or not (default: false)").define("weaponsShouldntBeBetter", false);
            builder.push("Mining Speedometer");
            this.tooltipOutputFormat = builder.comment("The Output Format for the speedometer jade tooltip")
                    .defineEnum("Jade Output Format", OutputFormat.NEXT_MINING_SPEED_BREAKPOINT);
            builder.pop();
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
        //public final ForgeConfigSpec.ConfigValue<Boolean> weaponsShouldntBeBetter;
        public final ForgeConfigSpec.ConfigValue<Integer> soulFlameLevelAllowance;

        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("Gameplay Settings");
            this.enableMoteRecipes= builder.comment("Controls whether Mote of Purity, Sanctity, and Clarity should work in the Crystal Workbench. (default: false)")
                    .define("enableMoteRecipes", false);
            this.crystalReinforcementMaxCapacityAdded = builder.comment("The max capacity that can be added to a tool with Resonating Reinforcements. (default: 20)").define("resonatingReinforcementMaxCapacityAdded", 20);
            this.disableWanderingWispSpawning = builder.comment("Controls whether or not wandering wisps should spawn (default: true)").define("disableWanderingWispSpawning", true);
            //this.weaponsShouldntBeBetter = builder.comment("Whether Better Combat should be disabled for weapons or not (note: this is a client-side setting, don't change on servers) (default: false)").define("weaponsShouldntBeBetter", false);
            this.soulFlameLevelAllowance = builder.comment("How much a difference in levels should be allowed for Soul Flames before not raising stacks (default: 3)").define("soulFlameLevelAllowance", 3);
            builder.pop();
            builder.push("Developer Settings");
            this.enableDebugMode= builder.comment("Don't recommend turning on unless asked, enables debug messages for development. (default: false)")
                    .define("enableDebugMode", false);
            builder.pop();
        }
    }

    public static class Server {

        public final ForgeConfigSpec.ConfigValue<Integer> dollDismantlerEnergyStorage;
        public final ForgeConfigSpec.ConfigValue<Integer> dollDismantlerEnergyConsumption;
        public final ForgeConfigSpec.ConfigValue<Integer> dollDismantlerEnergyTransfer;
        public final ForgeConfigSpec.ConfigValue<Integer> dollDismantlerExtractionSpeed;
        public final ForgeConfigSpec.ConfigValue<Integer> crateCrackerEnergyStorage;
        public final ForgeConfigSpec.ConfigValue<Integer> crateCrackerEnergyConsumption;
        public final ForgeConfigSpec.ConfigValue<Integer> crateCrackerEnergyTransfer;
        public final ForgeConfigSpec.ConfigValue<Integer> crateCrackerExtractionSpeed;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableNormalGatewayPearls;
        public final ForgeConfigSpec.ConfigValue<Integer> scannableScannerCooldown;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableServerKiller;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Doll Mixer Settings");
            this.dollDismantlerEnergyStorage = builder.comment("Controls how much energy the Doll Dismantler can store inside the block. (default: 1000)").defineInRange("dollDismantlerEnergyStorage", 1000, 0, Integer.MAX_VALUE);
            this.dollDismantlerEnergyConsumption = builder.comment("Controls how much energy the Doll Dismantler uses while blending. (default: 16)").defineInRange("dollDismantlerEnergyConsumption", 16, 0, Integer.MAX_VALUE);
            this.dollDismantlerEnergyTransfer = builder.comment("Controls how much energy the Doll Dismantler can receive/extract (default: 100)").defineInRange("dollDismantlerEnergyTransfer", 100, 0, Integer.MAX_VALUE);
            this.dollDismantlerExtractionSpeed = builder.comment("Controls how many stacks per tick the doll dismantler extracts (default: 5)").defineInRange("dollDismantlerExtractionSpeed", 5, 1, 100);
            builder.pop();
            builder.push("Crate Cracker Settings");
            this.crateCrackerEnergyStorage = builder.comment("Controls how much energy the Crate Cracker can store inside the block. (default: 1000)").defineInRange("crateCrackerEnergyStorage", 1000, 0, Integer.MAX_VALUE);
            this.crateCrackerEnergyConsumption = builder.comment("Controls how much energy the Crate Cracker uses while extracting. (default: 16)").defineInRange("crateCrackerEnergyConsumption", 16, 0, Integer.MAX_VALUE);
            this.crateCrackerEnergyTransfer = builder.comment("Controls how much energy the Crate Cracker can receive/extract (default: 100)").defineInRange("crateCrackerEnergyTransfer", 100, 0, Integer.MAX_VALUE);
            this.crateCrackerExtractionSpeed = builder.comment("Controls how many stacks per tick the Crate Cracker extracts (default: 5)").defineInRange("crateCrackerExtractionSpeed", 5, 1, 100);
            builder.pop();
            builder.push("Gameplay Settings");
            this.enableNormalGatewayPearls = builder.comment("Whether Gateway Pearls should be able to be used normally (right-clicking to use them on ground) (default: false)").define("enableNormalGatewayPearls", false);
            this.scannableScannerCooldown = builder.comment("How long the cooldown for Scannable scanner should be, in ticks (default: 1200)").define("scannableScannerCooldown", 1200);
            builder.pop();
            builder.push("Admin Settings");
            this.enableServerKiller = builder.comment("Whether to enable Server Killer that will automatically shut down server after 30 seconds, useful if your server is hanging on shutdown. (default: false)").define("enableServerKiller", false);
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

    public enum OutputFormat {
        TOTAL_MINING_SPEED_WITHBASE,
        TOTAL_MINING_SPEED_WITHOUTBASE,
        ADDITIONAL_MINING_SPEED,
        NEXT_MINING_SPEED_BREAKPOINT
    }
}