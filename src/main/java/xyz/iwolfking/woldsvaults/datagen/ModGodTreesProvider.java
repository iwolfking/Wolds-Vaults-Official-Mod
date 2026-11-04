package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.data.core.ConfigData;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeGuiStylesConfig;
import xyz.iwolfking.woldsvaults.datagen.gods.GodNodeEffectDefaults;
import xyz.iwolfking.woldsvaults.datagen.gods.GodTreeBuilder;
import xyz.iwolfking.woldsvaults.datagen.gods.GodTreeDefaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractWoldsTableProvider;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Emits every god's constellation as three generated tables: {@code gods/trees/<god>} (topology),
 * {@code gods/styles/<god>} (layout, screen pixels with +y down) and {@code gods/effects/<god>} (what each node
 * does). Before anything is written the four gods are run through {@link GodNodeRegistry#load}, the same check the
 * game makes, so a tree the game would reject fails datagen instead.
 */
public class ModGodTreesProvider extends AbstractWoldsTableProvider {

    public ModGodTreesProvider(DataGenerator generator) {
        super(generator, "gods");
    }

    @Override
    public void registerConfigs() {
        Map<VaultGod, GodTreeConfig> trees = new EnumMap<>(VaultGod.class);
        Map<VaultGod, GodTreeGuiStylesConfig> styles = new EnumMap<>(VaultGod.class);
        Map<VaultGod, GodNodeEffectsConfig> effects = new EnumMap<>(VaultGod.class);
        for (VaultGod god : VaultGod.values()) {
            String name = god.getName().toLowerCase(Locale.ROOT);
            GodTreeBuilder builder = GodTreeDefaults.forGod(name);
            if (builder == null) {
                throw new IllegalStateException("No god tree is defined for " + name);
            }
            trees.put(god, ConfigData.CONFIG_LOADER_GSON.fromJson(builder.buildTree(), GodTreeConfig.class));
            styles.put(god, ConfigData.CONFIG_LOADER_GSON.fromJson(builder.buildStyles(), GodTreeGuiStylesConfig.class));
            GodNodeEffectsConfig godEffects = new GodNodeEffectsConfig();
            godEffects.effects = GodNodeEffectDefaults.effects(name);
            effects.put(god, godEffects);
        }
        GodNodeRegistry.load(trees, styles, effects);
        for (VaultGod god : VaultGod.values()) {
            String name = god.getName().toLowerCase(Locale.ROOT);
            this.table("trees/" + name, trees.get(god));
            this.table("styles/" + name, styles.get(god));
            this.table("effects/" + name, effects.get(god));
        }
    }
}
