package xyz.iwolfking.woldsvaults.integration.vhapi.loaders.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.iwolfking.vhapi.api.events.VaultConfigEvent;
import xyz.iwolfking.vhapi.api.lib.core.processors.IRecipeSyncer;
import xyz.iwolfking.vhapi.api.loaders.lib.core.VaultConfigProcessor;
import xyz.iwolfking.woldsvaults.config.recipes.augment.AugmentRecipesConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

public class AugmentRecipesLoader extends VaultConfigProcessor<AugmentRecipesConfig> implements IRecipeSyncer {
    public AugmentRecipesLoader() {
        super(new AugmentRecipesConfig(), "vault_recipes/augment");
    }

    @Override
    public void afterConfigsLoad(VaultConfigEvent.End event) {

        for (ResourceLocation key : this.CUSTOM_CONFIGS.keySet()) {
            AugmentRecipesConfig config = this.CUSTOM_CONFIGS.get(key);
            if (key.getPath().contains("overwrite")) {
                ModConfigs.AUGMENT_RECIPES = config;
            } else {
                ModConfigs.AUGMENT_RECIPES.getConfigRecipes().addAll(config.getConfigRecipes());
            }
        }

        this.syncRecipes();
        super.afterConfigsLoad(event);
    }

    public void syncRecipes() {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) {
            srv.getPlayerList().getPlayers().forEach(player -> ModConfigs.AUGMENT_RECIPES.syncTo(ModConfigs.AUGMENT_RECIPES, player));
        }
    }
}
