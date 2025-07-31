package xyz.iwolfking.woldsvaults;

import com.mojang.logging.LogUtils;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.PlayerGreedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LoadingModList;
import org.slf4j.Logger;
import xyz.iwolfking.vhapi.api.registry.gear.CustomVaultGearRegistryEntry;
import xyz.iwolfking.vhapi.api.registry.objective.CustomObjectiveRegistryEntry;
import xyz.iwolfking.woldsvaults.api.WoldDataLoaders;
import xyz.iwolfking.woldsvaults.config.forge.WoldsVaultsConfig;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredThemesData;
import xyz.iwolfking.woldsvaults.data.recipes.CachedInfuserRecipeData;
import xyz.iwolfking.woldsvaults.events.LivingEntityEvents;
import xyz.iwolfking.woldsvaults.events.RegisterCommandEventHandler;
import xyz.iwolfking.woldsvaults.events.client.ClientSetupEvents;
import xyz.iwolfking.woldsvaults.init.*;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.api.lib.PlayerGreedDataExtension;
import xyz.iwolfking.woldsvaults.models.AdditionalModels;
import xyz.iwolfking.woldsvaults.network.NetworkHandler;
import xyz.iwolfking.woldsvaults.objectives.data.BrutalBossesRegistry;
import xyz.iwolfking.woldsvaults.objectives.data.EnchantedEventsRegistry;
import xyz.iwolfking.woldsvaults.objectives.speedrun.SpeedrunCrystalObjective;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("woldsvaults")
public class WoldsVaults {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "woldsvaults";
    public static boolean BETTER_COMBAT_PRESENT = true;
    public WoldsVaults() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WoldsVaultsConfig.COMMON_SPEC, "woldsvaults-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, WoldsVaultsConfig.CLIENT_SPEC, "woldsvaults-client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WoldsVaultsConfig.SERVER_SPEC, "woldsvaults-server.toml");
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.register(new ModRecipeSerializers());

        modEventBus.addGenericListener(CustomObjectiveRegistryEntry.class, ModCustomVaultObjectiveEntries::registerCustomObjectives);
        modEventBus.addGenericListener(CustomVaultGearRegistryEntry.class, ModCustomVaultGearEntries::registerGearEntries);

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onLevelLoad);
        MinecraftForge.EVENT_BUS.addListener(RegisterCommandEventHandler::woldsvaults_registerCommandsEvent);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModCatalystModels.registerModels();
        ModInscriptionModels.registerModels();
        ModFTBQuestsTaskTypes.init();
        MinecraftForge.EVENT_BUS.addListener(WoldDataLoaders::initProcessors);
    }

    private void setup(final FMLCommonSetupEvent event) {
        if(WoldsVaultsConfig.COMMON.enableDebugMode.get()) {
            LOGGER.warn("Debug mode is enabled! Please disable this in woldsvaults-common.toml to prevent unnecessary log spam!");
            LOGGER.debug("Initializing FMLCommonSetup events!");
        }
        ModNetwork.init();

        LivingEntityEvents.init();
        new AdditionalModels();
        ModVaultFilterAttributes.initAttributes();
        ModGameRules.initialize();
        NetworkHandler.onCommonSetup();
        CrystalData.OBJECTIVE.register("brb_speedrun", SpeedrunCrystalObjective.class, SpeedrunCrystalObjective::new);
        BETTER_COMBAT_PRESENT = LoadingModList.get().getModFileById("bettercombat") != null;
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EnchantedEventsRegistry.addEvents();
        BrutalBossesRegistry.init();
    }

    public void onLevelLoad(WorldEvent.Load event) {
        if(CachedInfuserRecipeData.shouldCache()) {
            if(event.getWorld() instanceof Level) {
                CachedInfuserRecipeData.cacheCatalysts((Level) event.getWorld());
                CachedInfuserRecipeData.cacheIngredients((Level) event.getWorld());
            }
        }

    }

    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        ModConfigs.AUGMENT_RECIPES.syncTo(ModConfigs.AUGMENT_RECIPES, (ServerPlayer) event.getPlayer());
        ModConfigs.MOD_BOX_RECIPES_CONFIG.syncTo(ModConfigs.MOD_BOX_RECIPES_CONFIG, (ServerPlayer) event.getPlayer());
        ModConfigs.WEAVING_RECIPES_CONFIG.syncTo(ModConfigs.WEAVING_RECIPES_CONFIG, (ServerPlayer) event.getPlayer());
        DiscoveredThemesData.get(((ServerPlayer) event.getPlayer()).getLevel()).syncTo((ServerPlayer) event.getPlayer());
        DiscoveredRecipesData.get(((ServerPlayer) event.getPlayer()).getLevel()).syncTo((ServerPlayer) event.getPlayer());
        PlayerGreedData greedData = PlayerGreedData.get(((ServerPlayer) event.getPlayer()).server);
        ((PlayerGreedDataExtension)greedData).syncTo((ServerPlayer) event.getPlayer());
    }






    public static ResourceLocation id(String name) {
        return new ResourceLocation("woldsvaults", name);
    }

    public static String sId(String name) {
        return "woldsvaults:" + name;
    }
}
