package xyz.iwolfking.woldsvaults.integration.vhapi.loaders;

import xyz.iwolfking.vhapi.api.LoaderRegistry;
import xyz.iwolfking.vhapi.api.events.VHAPIProcessorsEvent;
import xyz.iwolfking.vhapi.api.loaders.box.WeightedProductEntryConfigLoader;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.core.EtchedLayoutConfigLoader;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.objectives.enchanted_elixir.EnchantedElixirEventLoader;
import xyz.iwolfking.woldsvaults.integration.vhapi.loaders.recipes.AugmentRecipesLoader;
import xyz.iwolfking.woldsvaults.integration.vhapi.VHAPIModules;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

public class WoldDataLoaders {

    public static final WeightedProductEntryConfigLoader AUGMENT_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader INSCRIPTION_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader OMEGA_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader VAULTAR_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader CATALYST_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader GEM_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader SUPPLY_BOX_LOADER;
    public static final WeightedProductEntryConfigLoader ENIGMA_EGG_LOADER;
    public static final AugmentRecipesLoader AUGMENT_RECIPES_LOADER;
    public static final EtchedLayoutConfigLoader ETCHED_LAYOUT_CONFIG_LOADER;

    static {
        AUGMENT_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.AUGMENT_BOX.POOL;
        }, "augment_box");

        INSCRIPTION_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.INSCRIPTION_BOX.POOL;
        }, "inscription_box");

        OMEGA_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.OMEGA_BOX.POOL;
        }, "omega_box");

        VAULTAR_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.VAULTAR_BOX.POOL;
        }, "vaultar_box");

        CATALYST_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.VAULTAR_BOX.POOL;
        }, "catalyst_box");

        GEM_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.GEM_BOX.POOL;
        }, "gem_box");

        SUPPLY_BOX_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.SUPPLY_BOX.POOL;
        }, "supply_box");

        ENIGMA_EGG_LOADER = new WeightedProductEntryConfigLoader(() -> {
            return ModConfigs.ENIGMA_EGG.POOL;
        }, "enigma_egg");
        AUGMENT_RECIPES_LOADER = new AugmentRecipesLoader();
        ETCHED_LAYOUT_CONFIG_LOADER = new EtchedLayoutConfigLoader();
    }



    public static void initProcessors(VHAPIProcessorsEvent.End event) {
        LoaderRegistry.addConfigProcessor(AUGMENT_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(INSCRIPTION_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(OMEGA_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(VAULTAR_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(CATALYST_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(GEM_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(SUPPLY_BOX_LOADER);
        LoaderRegistry.addConfigProcessor(ENIGMA_EGG_LOADER);
        LoaderRegistry.addConfigProcessor(AUGMENT_RECIPES_LOADER);
        LoaderRegistry.addConfigProcessor(ETCHED_LAYOUT_CONFIG_LOADER);
        VHAPIModules.init();
    }

}
